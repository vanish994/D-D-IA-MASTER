package com.ddia.master

import kotlin.math.abs
import kotlin.random.Random

/**
 * Camada autoritativa de ações de combate.
 *
 * A IA pode propor uma Intent, mas só esta camada altera o estado. Distância,
 * turno ativo, PV, usos e armas vêm do estado do jogo — não do texto do Mestre.
 * Coordenadas representam quadrados de 5 pés; diagonais custam 5 pés por quadrado.
 */
object TurnRules {
    enum class ActionCost { NONE, ACTION, BONUS_ACTION, REACTION }
    enum class RestType { SHORT, LONG }
    enum class RefreshOn { SHORT_REST, LONG_REST, NEVER }
    enum class IntentType { ATTACK, MOVE, USE_RESOURCE, END_TURN }

    data class Position(val x: Int, val y: Int)

    data class WeaponProfile(
        val id: String,
        val name: String,
        val attackBonus: Int,
        val damageDice: String,
        val damageModifier: Int = 0,
        /** Alcance máximo em pés. Para arma corpo a corpo, normalmente 5. */
        val rangeFeet: Int = 5
    ) {
        init {
            require(id.isNotBlank() && name.isNotBlank())
            require(rangeFeet > 0)
        }
    }

    data class LimitedResource(
        val id: String,
        val maximum: Int,
        val remaining: Int,
        val actionCost: ActionCost = ActionCost.NONE,
        val refreshOn: RefreshOn = RefreshOn.LONG_REST
    ) {
        init {
            require(id.isNotBlank())
            require(maximum >= 0 && remaining in 0..maximum)
        }
    }

    data class Combatant(
        val id: String,
        val name: String,
        val currentHp: Int,
        val maximumHp: Int,
        val armorClass: Int,
        val position: Position,
        val speedFeet: Int = 30,
        val weapons: Map<String, WeaponProfile> = emptyMap(),
        val resources: Map<String, LimitedResource> = emptyMap(),
        val conditions: Set<String> = emptySet()
    ) {
        init {
            require(id.isNotBlank() && name.isNotBlank())
            require(maximumHp > 0 && currentHp in 0..maximumHp)
            require(armorClass > 0 && speedFeet >= 0)
            require(weapons.all { (key, weapon) -> key == weapon.id })
            require(resources.all { (key, resource) -> key == resource.id })
        }

        val isAlive: Boolean get() = currentHp > 0
    }

    data class TurnUsage(
        val actionUsed: Boolean = false,
        val bonusActionUsed: Boolean = false,
        val reactionUsed: Boolean = false,
        val movementUsedFeet: Int = 0
    )

    data class CombatState(
        val combatants: Map<String, Combatant>,
        val initiativeOrder: List<String>,
        val activeTurnIndex: Int = 0,
        val round: Int = 1,
        val usage: Map<String, TurnUsage> = emptyMap()
    ) {
        init {
            require(initiativeOrder.isNotEmpty())
            require(initiativeOrder.distinct().size == initiativeOrder.size)
            require(initiativeOrder.all { it in combatants })
            require(activeTurnIndex in initiativeOrder.indices)
            require(round >= 1)
        }

        val activeActorId: String get() = initiativeOrder[activeTurnIndex]
    }

    data class ActionIntent(
        val type: IntentType,
        val actorId: String,
        val targetId: String? = null,
        val weaponId: String? = null,
        val destination: Position? = null,
        val resourceId: String? = null
    )

    data class ActionOutcome(
        val accepted: Boolean,
        val message: String,
        val state: CombatState,
        val attack: RulesEngine.AttackResult? = null,
        val movementFeet: Int = 0
    )

    fun newCombat(combatants: List<Combatant>, initiativeOrder: List<String> = combatants.map { it.id }): CombatState {
        require(combatants.map { it.id }.distinct().size == combatants.size) { "IDs de combatente duplicados." }
        require(combatants.isNotEmpty()) { "O combate precisa de pelo menos um combatente." }
        val byId = combatants.associateBy { it.id }
        require(initiativeOrder.toSet() == byId.keys && initiativeOrder.size == byId.size) {
            "A ordem de iniciativa deve incluir cada combatente exatamente uma vez."
        }
        return CombatState(
            combatants = byId,
            initiativeOrder = initiativeOrder,
            usage = byId.keys.associateWith { TurnUsage() }
        )
    }

    fun execute(
        state: CombatState,
        intent: ActionIntent,
        random: Random = Random.Default
    ): ActionOutcome {
        val actor = state.combatants[intent.actorId]
            ?: return rejected(state, "O personagem informado não existe.")
        if (!actor.isAlive) return rejected(state, "Um personagem com 0 PV não pode agir.")
        if (intent.type != IntentType.END_TURN && intent.actorId != state.activeActorId) {
            return rejected(state, "Não é o turno deste personagem.")
        }
        val usage = state.usage[intent.actorId] ?: TurnUsage()

        return when (intent.type) {
            IntentType.ATTACK -> executeAttack(state, intent, actor, usage, random)
            IntentType.MOVE -> executeMove(state, intent, actor, usage)
            IntentType.USE_RESOURCE -> executeResource(state, intent, actor, usage)
            IntentType.END_TURN -> endTurn(state, intent.actorId)
        }
    }

    private fun executeAttack(
        state: CombatState,
        intent: ActionIntent,
        actor: Combatant,
        usage: TurnUsage,
        random: Random
    ): ActionOutcome {
        if (usage.actionUsed) return rejected(state, "A ação principal deste turno já foi usada.")
        val targetId = intent.targetId ?: return rejected(state, "Escolha um alvo para atacar.")
        val target = state.combatants[targetId] ?: return rejected(state, "O alvo não existe.")
        if (targetId == actor.id) return rejected(state, "Este ataque não pode ter o próprio personagem como alvo.")
        if (!target.isAlive) return rejected(state, "O alvo já está fora de combate.")
        val weaponId = intent.weaponId ?: return rejected(state, "Escolha uma arma equipada.")
        val weapon = actor.weapons[weaponId] ?: return rejected(state, "Essa arma não está equipada.")
        val distance = gridDistanceFeet(actor.position, target.position)
        if (distance > weapon.rangeFeet) {
            return rejected(state, "Alvo fora do alcance: $distance pés; alcance da arma: ${weapon.rangeFeet} pés.")
        }

        val attack = RulesEngine.attack(
            attackBonus = weapon.attackBonus,
            targetArmorClass = target.armorClass,
            damageDice = weapon.damageDice,
            damageModifier = weapon.damageModifier,
            random = random
        )
        val updatedActor = actor
        val updatedTarget = if (attack.hit) target.copy(
            currentHp = RulesEngine.applyDamage(target.currentHp, attack.damage)
        ) else target
        val nextUsage = usage.copy(actionUsed = true)
        val nextState = state.copy(
            combatants = state.combatants + (actor.id to updatedActor) + (target.id to updatedTarget),
            usage = state.usage + (actor.id to nextUsage)
        )
        val message = if (attack.hit) {
            "${actor.name} acerta ${target.name} e causa ${attack.damage} de dano${if (attack.critical) " (crítico)" else ""}."
        } else "${actor.name} erra ${target.name}."
        return ActionOutcome(true, message, nextState, attack)
    }

    private fun executeMove(
        state: CombatState,
        intent: ActionIntent,
        actor: Combatant,
        usage: TurnUsage
    ): ActionOutcome {
        val destination = intent.destination ?: return rejected(state, "Informe o destino do movimento.")
        val distance = gridDistanceFeet(actor.position, destination)
        if (distance == 0) return rejected(state, "O personagem já está nessa posição.")
        if (usage.movementUsedFeet + distance > actor.speedFeet) {
            val remaining = (actor.speedFeet - usage.movementUsedFeet).coerceAtLeast(0)
            return rejected(state, "Movimento excede o deslocamento restante de $remaining pés.")
        }
        if (state.combatants.values.any { it.id != actor.id && it.isAlive && it.position == destination }) {
            return rejected(state, "O quadrado de destino está ocupado.")
        }
        val moved = actor.copy(position = destination)
        val nextUsage = usage.copy(movementUsedFeet = usage.movementUsedFeet + distance)
        return ActionOutcome(
            accepted = true,
            message = "${actor.name} se move $distance pés.",
            state = state.copy(
                combatants = state.combatants + (actor.id to moved),
                usage = state.usage + (actor.id to nextUsage)
            ),
            movementFeet = distance
        )
    }

    private fun executeResource(
        state: CombatState,
        intent: ActionIntent,
        actor: Combatant,
        usage: TurnUsage
    ): ActionOutcome {
        val resourceId = intent.resourceId ?: return rejected(state, "Informe o recurso que deseja usar.")
        val resource = actor.resources[resourceId] ?: return rejected(state, "Esse recurso não pertence ao personagem.")
        if (resource.remaining <= 0) return rejected(state, "Não há usos restantes desse recurso.")
        when (resource.actionCost) {
            ActionCost.ACTION -> if (usage.actionUsed) return rejected(state, "A ação principal deste turno já foi usada.")
            ActionCost.BONUS_ACTION -> if (usage.bonusActionUsed) return rejected(state, "A ação bônus deste turno já foi usada.")
            ActionCost.REACTION -> if (usage.reactionUsed) return rejected(state, "A reação já foi usada.")
            ActionCost.NONE -> Unit
        }
        val nextResource = resource.copy(remaining = resource.remaining - 1)
        val nextUsage = when (resource.actionCost) {
            ActionCost.ACTION -> usage.copy(actionUsed = true)
            ActionCost.BONUS_ACTION -> usage.copy(bonusActionUsed = true)
            ActionCost.REACTION -> usage.copy(reactionUsed = true)
            ActionCost.NONE -> usage
        }
        val updatedActor = actor.copy(resources = actor.resources + (resourceId to nextResource))
        return ActionOutcome(
            true,
            "Recurso $resourceId usado; restam ${nextResource.remaining}.",
            state.copy(
                combatants = state.combatants + (actor.id to updatedActor),
                usage = state.usage + (actor.id to nextUsage)
            )
        )
    }

    private fun endTurn(state: CombatState, actorId: String): ActionOutcome {
        if (actorId != state.activeActorId) return rejected(state, "Só o personagem ativo pode encerrar o turno.")
        val aliveIds = state.initiativeOrder.filter { state.combatants[it]?.isAlive == true }
        if (aliveIds.isEmpty()) return rejected(state, "Não há combatentes ativos.")
        val currentPosition = aliveIds.indexOf(actorId)
        val wrapped = currentPosition == aliveIds.lastIndex
        val nextId = aliveIds[(currentPosition + 1) % aliveIds.size]
        val nextIndex = state.initiativeOrder.indexOf(nextId)
        val resetUsage = state.usage + (nextId to TurnUsage())
        return ActionOutcome(
            true,
            "Turno encerrado. Agora é a vez de ${state.combatants.getValue(nextId).name}.",
            state.copy(
                activeTurnIndex = nextIndex,
                round = if (wrapped) state.round + 1 else state.round,
                usage = resetUsage
            )
        )
    }

    fun rest(combatant: Combatant, type: RestType): Combatant {
        val updated = combatant.resources.mapValues { (_, resource) ->
            val refresh = when (type) {
                RestType.SHORT -> resource.refreshOn == RefreshOn.SHORT_REST
                RestType.LONG -> resource.refreshOn == RefreshOn.SHORT_REST ||
                    resource.refreshOn == RefreshOn.LONG_REST
            }
            if (refresh) resource.copy(remaining = resource.maximum) else resource
        }
        return combatant.copy(resources = updated)
    }

    fun gridDistanceFeet(from: Position, to: Position): Int =
        maxOf(abs(from.x - to.x), abs(from.y - to.y)) * 5

    private fun rejected(state: CombatState, message: String) =
        ActionOutcome(accepted = false, message = message, state = state)
}
