package com.ddia.master

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class TurnRulesTest {
    private val sword = TurnRules.WeaponProfile(
        id = "sword",
        name = "Espada",
        attackBonus = 100,
        damageDice = "1d6",
        rangeFeet = 5
    )

    private fun combat(): TurnRules.CombatState = TurnRules.newCombat(
        listOf(
            TurnRules.Combatant(
                id = "hero",
                name = "Aventureiro",
                currentHp = 12,
                maximumHp = 12,
                armorClass = 16,
                position = TurnRules.Position(0, 0),
                weapons = mapOf("sword" to sword),
                resources = mapOf(
                    "flash" to TurnRules.LimitedResource(
                        id = "flash", maximum = 2, remaining = 1,
                        actionCost = TurnRules.ActionCost.BONUS_ACTION,
                        refreshOn = TurnRules.RefreshOn.LONG_REST
                    )
                )
            ),
            TurnRules.Combatant(
                id = "enemy",
                name = "Inimigo",
                currentHp = 20,
                maximumHp = 20,
                armorClass = 12,
                position = TurnRules.Position(1, 0)
            )
        ),
        listOf("hero", "enemy")
    )

    @Test fun attackUsesActionAndAppliesDamageToState() {
        val initial = combat()
        val result = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(
                type = TurnRules.IntentType.ATTACK,
                actorId = "hero",
                targetId = "enemy",
                weaponId = "sword"
            ),
            Random(7)
        )

        assertTrue(result.accepted)
        assertNotNull(result.attack)
        assertTrue(result.attack!!.hit)
        assertTrue(result.state.combatants.getValue("enemy").currentHp < 20)
        assertTrue(result.state.usage.getValue("hero").actionUsed)
        assertEquals(20, initial.combatants.getValue("enemy").currentHp)
    }

    @Test fun rejectedSecondAttackDoesNotChangeState() {
        val initial = combat()
        val first = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(TurnRules.IntentType.ATTACK, "hero", "enemy", "sword"),
            Random(7)
        )
        val second = TurnRules.execute(
            first.state,
            TurnRules.ActionIntent(TurnRules.IntentType.ATTACK, "hero", "enemy", "sword"),
            Random(8)
        )

        assertTrue(first.accepted)
        assertFalse(second.accepted)
        assertEquals(first.state, second.state)
    }

    @Test fun cannotAttackTargetOutsideWeaponRange() {
        val initial = combat()
        val movedEnemy = initial.combatants.getValue("enemy").copy(position = TurnRules.Position(3, 0))
        val farState = initial.copy(combatants = initial.combatants + ("enemy" to movedEnemy))
        val result = TurnRules.execute(
            farState,
            TurnRules.ActionIntent(TurnRules.IntentType.ATTACK, "hero", "enemy", "sword")
        )

        assertFalse(result.accepted)
        assertEquals(farState, result.state)
    }

    @Test fun movementCannotExceedSpeedOrEnterOccupiedSquare() {
        val initial = combat()
        val tooFar = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(
                type = TurnRules.IntentType.MOVE,
                actorId = "hero",
                destination = TurnRules.Position(7, 0)
            )
        )
        val occupied = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(
                type = TurnRules.IntentType.MOVE,
                actorId = "hero",
                destination = TurnRules.Position(1, 0)
            )
        )

        assertFalse(tooFar.accepted)
        assertFalse(occupied.accepted)
        assertEquals(initial, tooFar.state)
        assertEquals(initial, occupied.state)
    }

    @Test fun resourceCannotBeUsedMoreOftenThanAvailable() {
        val initial = combat()
        val first = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(
                type = TurnRules.IntentType.USE_RESOURCE,
                actorId = "hero",
                resourceId = "flash"
            )
        )
        val second = TurnRules.execute(
            first.state,
            TurnRules.ActionIntent(
                type = TurnRules.IntentType.USE_RESOURCE,
                actorId = "hero",
                resourceId = "flash"
            )
        )

        assertTrue(first.accepted)
        assertEquals(0, first.state.combatants.getValue("hero").resources.getValue("flash").remaining)
        assertFalse(second.accepted)
        assertEquals(first.state, second.state)
    }

    @Test fun longRestRestoresLongRestResourcesButShortRestDoesNot() {
        val hero = combat().combatants.getValue("hero").copy(
            resources = mapOf(
                "short" to TurnRules.LimitedResource("short", 2, 0, refreshOn = TurnRules.RefreshOn.SHORT_REST),
                "long" to TurnRules.LimitedResource("long", 3, 1, refreshOn = TurnRules.RefreshOn.LONG_REST),
                "never" to TurnRules.LimitedResource("never", 1, 0, refreshOn = TurnRules.RefreshOn.NEVER)
            )
        )

        val afterShort = TurnRules.rest(hero, TurnRules.RestType.SHORT)
        assertEquals(2, afterShort.resources.getValue("short").remaining)
        assertEquals(1, afterShort.resources.getValue("long").remaining)
        assertEquals(0, afterShort.resources.getValue("never").remaining)

        val afterLong = TurnRules.rest(afterShort, TurnRules.RestType.LONG)
        assertEquals(2, afterLong.resources.getValue("short").remaining)
        assertEquals(3, afterLong.resources.getValue("long").remaining)
        assertEquals(0, afterLong.resources.getValue("never").remaining)
    }

    @Test fun endTurnAdvancesInitiativeAndResetsNextCombatantsUsage() {
        val initial = combat()
        val used = TurnRules.execute(
            initial,
            TurnRules.ActionIntent(TurnRules.IntentType.USE_RESOURCE, "hero", resourceId = "flash")
        )
        val next = TurnRules.execute(
            used.state,
            TurnRules.ActionIntent(TurnRules.IntentType.END_TURN, "hero")
        )

        assertTrue(next.accepted)
        assertEquals("enemy", next.state.activeActorId)
        assertEquals(TurnRules.TurnUsage(), next.state.usage.getValue("enemy"))
    }

    @Test fun abilityModifierAndProficiencyBoundariesAreCorrect() {
        assertEquals(-1, RulesEngine.abilityModifier(9))
        assertEquals(0, RulesEngine.abilityModifier(10))
        assertEquals(3, RulesEngine.proficiencyBonus(5))
        assertEquals(6, RulesEngine.proficiencyBonus(17))
    }
}
