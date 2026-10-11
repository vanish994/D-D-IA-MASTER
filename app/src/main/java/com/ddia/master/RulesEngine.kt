package com.ddia.master

import kotlin.random.Random

/**
 * Núcleo de regras do RPG.
 *
 * Esta camada mantém as mecânicas separadas da interface. As regras serão
 * ampliadas por módulos (personagem, combate, exploração, condições e magia).
 * Não contém texto copiado do livro: implementa operações mecânicas.
 */
object RulesEngine {
    enum class Ability { STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA }
    enum class RollMode { NORMAL, ADVANTAGE, DISADVANTAGE }

    data class Roll(
        val dice: List<Int>,
        val chosen: Int,
        val modifier: Int,
        val total: Int
    )

    data class CheckResult(
        val roll: Roll,
        val dc: Int,
        val success: Boolean
    )

    data class AttackResult(
        val roll: Roll,
        val targetArmorClass: Int,
        val hit: Boolean,
        val critical: Boolean,
        val damageRolls: List<Int>,
        val damageModifier: Int,
        val damage: Int
    )

    /** Modificador de atributo padrão: (valor - 10) dividido por 2, arredondado para baixo. */
    fun abilityModifier(score: Int): Int = Math.floorDiv(score - 10, 2)

    /** Bônus de Proficiência por nível de personagem, níveis 1 a 20. */
    fun proficiencyBonus(level: Int): Int {
        require(level in 1..20) { "O nível deve estar entre 1 e 20." }
        return 2 + (level - 1) / 4
    }

    fun rollDie(sides: Int, random: Random = Random.Default): Int {
        require(sides >= 2) { "Um dado precisa ter pelo menos 2 lados." }
        return random.nextInt(1, sides + 1)
    }

    /** Vantagem/desvantagem: rola dois d20 e usa o maior/menor. */
    fun rollD20(
        modifier: Int = 0,
        mode: RollMode = RollMode.NORMAL,
        random: Random = Random.Default
    ): Roll {
        val dice = when (mode) {
            RollMode.NORMAL -> listOf(rollDie(20, random))
            RollMode.ADVANTAGE, RollMode.DISADVANTAGE ->
                listOf(rollDie(20, random), rollDie(20, random))
        }
        val chosen = when (mode) {
            RollMode.NORMAL -> dice[0]
            RollMode.ADVANTAGE -> dice.maxOrNull()!!
            RollMode.DISADVANTAGE -> dice.minOrNull()!!
        }
        return Roll(dice, chosen, modifier, chosen + modifier)
    }

    fun abilityCheck(
        score: Int,
        dc: Int,
        proficient: Boolean = false,
        level: Int = 1,
        mode: RollMode = RollMode.NORMAL,
        circumstanceModifier: Int = 0,
        random: Random = Random.Default
    ): CheckResult {
        val bonus = abilityModifier(score) +
            (if (proficient) proficiencyBonus(level) else 0) +
            circumstanceModifier
        val roll = rollD20(bonus, mode, random)
        return CheckResult(roll, dc, roll.total >= dc)
    }

    fun savingThrow(
        score: Int,
        dc: Int,
        proficient: Boolean = false,
        level: Int = 1,
        mode: RollMode = RollMode.NORMAL,
        circumstanceModifier: Int = 0,
        random: Random = Random.Default
    ): CheckResult = abilityCheck(
        score, dc, proficient, level, mode, circumstanceModifier, random
    )

    /**
     * Jogada de ataque. Um 20 natural acerta criticamente; um 1 natural erra.
     * Em acerto crítico, dobra-se a quantidade de dados de dano, não os bônus fixos.
     */
    fun attack(
        attackBonus: Int,
        targetArmorClass: Int,
        damageDice: String = "1d6",
        damageModifier: Int = 0,
        mode: RollMode = RollMode.NORMAL,
        random: Random = Random.Default
    ): AttackResult {
        val roll = rollD20(attackBonus, mode, random)
        val critical = roll.chosen == 20
        val hit = critical || (roll.chosen != 1 && roll.total >= targetArmorClass)
        val parsed = parseDamageExpression(damageDice)
        val count = if (critical) parsed.first * 2 else parsed.first
        val rolls = if (hit) List(count) { rollDie(parsed.second, random) } else emptyList()
        val fixedModifier = parsed.third + damageModifier
        val damage = if (hit) (rolls.sum() + fixedModifier).coerceAtLeast(0) else 0
        return AttackResult(roll, targetArmorClass, hit, critical, rolls, fixedModifier, damage)
    }

    /** Aceita expressões simples como 1d8, 2d6+3 ou 1d4-1. */
    fun rollDamage(expression: String, random: Random = Random.Default): Int {
        val (count, sides, modifier) = parseDamageExpression(expression)
        return (List(count) { rollDie(sides, random) }.sum() + modifier).coerceAtLeast(0)
    }

    fun heal(currentHp: Int, maximumHp: Int, amount: Int): Int {
        require(maximumHp >= 0 && amount >= 0)
        return (currentHp + amount).coerceIn(0, maximumHp)
    }

    fun applyDamage(currentHp: Int, amount: Int): Int {
        require(amount >= 0)
        return (currentHp - amount).coerceAtLeast(0)
    }

    private fun parseDamageExpression(expression: String): Triple<Int, Int, Int> {
        val normalized = expression.lowercase().replace(" ", "")
        val match = Regex("^(\\d+)d(\\d+)([+-]\\d+)?$").matchEntire(normalized)
            ?: throw IllegalArgumentException("Dado de dano inválido: $expression")
        val count = match.groupValues[1].toInt()
        val sides = match.groupValues[2].toInt()
        val modifier = match.groupValues[3].takeIf { it.isNotEmpty() }?.toInt() ?: 0
        require(count in 1..100 && sides >= 2) { "Expressão de dados fora do limite." }
        return Triple(count, sides, modifier)
    }
}
