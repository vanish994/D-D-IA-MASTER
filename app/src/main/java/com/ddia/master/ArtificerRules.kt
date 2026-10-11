package com.ddia.master

/**
 * Dados mecânicos da classe Artífice para a edição revisada de D&D.
 *
 * Fonte de referência: Eberron: Forge of the Artificer (Wizards of the Coast, 2025).
 * Este arquivo registra valores e comportamentos para o motor; não reproduz o texto
 * descritivo do livro. Habilidades específicas serão implementadas em módulos próprios.
 * ATENÇÃO: a tabela numérica inicial é provisória, baseada no playtest oficial de 2025;
 * deve ser conferida com a versão publicada de Eberron: Forge of the Artificer antes
 * de declarar a progressão final como validada.
 */
object ArtificerRules {
    const val CLASS_NAME = "Artífice"
    const val HIT_DIE = 8
    val PRIMARY_ABILITY = RulesEngine.Ability.INTELLIGENCE

    enum class Subclass(val displayName: String) {
        ALCHEMIST("Alquimista"),
        ARMORER("Armeiro"),
        ARTILLERIST("Artilheiro"),
        BATTLE_SMITH("Ferreiro de Batalha"),
        CARTOGRAPHER("Cartógrafo")
    }

    enum class Skill(val displayName: String) {
        ARCANA("Arcanismo"),
        HISTORY("História"),
        INVESTIGATION("Investigação"),
        MEDICINE("Medicina"),
        NATURE("Natureza"),
        PERCEPTION("Percepção"),
        SLEIGHT_OF_HAND("Prestidigitação")
    }

    enum class Tool(val displayName: String) {
        THIEVES_TOOLS("Ferramentas de ladrão"),
        TINKERS_TOOLS("Ferramentas de inventor"),
        ARTISANS_TOOLS("Ferramentas de artesão à escolha")
    }

    enum class StartingEquipment {
        KIT_A,
        GOLD_ONLY
    }

    data class LevelProgression(
        val level: Int,
        val features: List<String>,
        val magicPlansKnown: Int,
        val magicItemsReplicated: Int,
        val cantripsKnown: Int,
        val spellsPrepared: Int,
        /** Índice 0 = espaços de 1º círculo; índice 4 = espaços de 5º círculo. */
        val spellSlots: List<Int>
    )

    data class CoreTraits(
        val hitDie: Int = HIT_DIE,
        val savingThrowProficiencies: Set<RulesEngine.Ability> = setOf(
            RulesEngine.Ability.CONSTITUTION,
            RulesEngine.Ability.INTELLIGENCE
        ),
        val skillChoices: List<Skill> = Skill.entries,
        val skillsToChoose: Int = 2,
        val weaponProficiencies: Set<String> = setOf("Armas simples"),
        val toolProficiencies: Set<Tool> = setOf(
            Tool.THIEVES_TOOLS,
            Tool.TINKERS_TOOLS,
            Tool.ARTISANS_TOOLS
        ),
        val armorTraining: Set<String> = setOf("Armadura leve", "Armadura média", "Escudos")
    )

    data class CharacterChoices(
        val level: Int,
        val intelligence: Int,
        val constitution: Int,
        val chosenSkills: Set<Skill>,
        val artisanToolName: String,
        val equipment: StartingEquipment,
        val subclass: Subclass? = null
    )

    data class DerivedStats(
        val proficiencyBonus: Int,
        val hitPointMaximumAtLevelOne: Int,
        val spellSaveDc: Int,
        val spellAttackBonus: Int,
        val savingThrowProficiencies: Set<RulesEngine.Ability>,
        val chosenSkills: Set<Skill>
    )

    /**
     * Esqueleto provisório da progressão. Conferir cada nível com o livro final de 2025
     * antes de tratar estes valores como definitivos. Os efeitos das características
     * serão implementados em módulos próprios, sem depender da interface.
     */
    private val progression = listOf(
        LevelProgression(1, listOf("Conjuração", "Magia do Inventor"), 0, 0, 2, 2, listOf(2, 0, 0, 0, 0)),
        LevelProgression(2, listOf("Replicar Item Mágico"), 4, 2, 2, 3, listOf(2, 0, 0, 0, 0)),
        LevelProgression(3, listOf("Subclasse de Artífice"), 4, 2, 2, 4, listOf(3, 0, 0, 0, 0)),
        LevelProgression(4, listOf("Melhoria de Atributo"), 4, 2, 2, 5, listOf(3, 0, 0, 0, 0)),
        LevelProgression(5, listOf("Característica de Subclasse"), 4, 2, 2, 6, listOf(4, 2, 0, 0, 0)),
        LevelProgression(6, listOf("Ajuste de Item Mágico"), 5, 3, 2, 6, listOf(4, 2, 0, 0, 0)),
        LevelProgression(7, listOf("Lampejo de Genialidade"), 5, 3, 2, 7, listOf(4, 3, 0, 0, 0)),
        LevelProgression(8, listOf("Melhoria de Atributo"), 5, 3, 2, 7, listOf(4, 3, 0, 0, 0)),
        LevelProgression(9, listOf("Característica de Subclasse"), 5, 3, 2, 9, listOf(4, 3, 2, 0, 0)),
        LevelProgression(10, listOf("Especialista em Itens Mágicos"), 6, 4, 3, 9, listOf(4, 3, 2, 0, 0)),
        LevelProgression(11, listOf("Item que Armazena Magia"), 6, 4, 3, 10, listOf(4, 3, 3, 0, 0)),
        LevelProgression(12, listOf("Melhoria de Atributo"), 6, 4, 3, 10, listOf(4, 3, 3, 0, 0)),
        LevelProgression(13, emptyList(), 6, 4, 3, 11, listOf(4, 3, 3, 1, 0)),
        LevelProgression(14, listOf("Virtuose em Itens Mágicos"), 7, 5, 4, 11, listOf(4, 3, 3, 1, 0)),
        LevelProgression(15, listOf("Característica de Subclasse"), 7, 5, 4, 12, listOf(4, 3, 3, 2, 0)),
        LevelProgression(16, listOf("Melhoria de Atributo"), 7, 5, 4, 12, listOf(4, 3, 3, 2, 0)),
        LevelProgression(17, emptyList(), 7, 5, 4, 14, listOf(4, 3, 3, 3, 1)),
        LevelProgression(18, listOf("Mestre dos Itens Mágicos"), 8, 6, 4, 14, listOf(4, 3, 3, 3, 1)),
        LevelProgression(19, listOf("Dádiva Épica"), 8, 6, 4, 15, listOf(4, 3, 3, 3, 2)),
        LevelProgression(20, listOf("Alma do Artifício"), 8, 6, 4, 15, listOf(4, 3, 3, 3, 2))
    )

    fun coreTraits(): CoreTraits = CoreTraits()

    fun progression(level: Int): LevelProgression {
        require(level in 1..20) { "O nível de Artífice deve estar entre 1 e 20." }
        return progression[level - 1]
    }

    fun allProgression(): List<LevelProgression> = progression.toList()

    fun hitPointsAtLevelOne(constitutionScore: Int): Int =
        HIT_DIE + RulesEngine.abilityModifier(constitutionScore)

    fun spellSaveDc(intelligenceScore: Int, level: Int): Int =
        8 + RulesEngine.abilityModifier(intelligenceScore) + RulesEngine.proficiencyBonus(level)

    fun spellAttackBonus(intelligenceScore: Int, level: Int): Int =
        RulesEngine.abilityModifier(intelligenceScore) + RulesEngine.proficiencyBonus(level)

    fun deriveStats(choices: CharacterChoices): DerivedStats {
        require(choices.level in 1..20) { "O nível deve estar entre 1 e 20." }
        require(choices.intelligence in 1..30 && choices.constitution in 1..30) {
            "Os valores de Inteligência e Constituição devem estar entre 1 e 30."
        }
        require(choices.chosenSkills.size == 2) { "Escolha exatamente duas perícias de Artífice." }
        require(choices.chosenSkills.all { it in Skill.entries }) {
            "Uma ou mais perícias escolhidas não pertencem à lista do Artífice."
        }
        require(choices.artisanToolName.isNotBlank()) {
            "Informe o tipo de ferramenta de artesão escolhido."
        }
        if (choices.level >= 3) {
            require(choices.subclass != null) { "Escolha uma subclasse a partir do nível 3." }
        }
        return DerivedStats(
            proficiencyBonus = RulesEngine.proficiencyBonus(choices.level),
            hitPointMaximumAtLevelOne = hitPointsAtLevelOne(choices.constitution),
            spellSaveDc = spellSaveDc(choices.intelligence, choices.level),
            spellAttackBonus = spellAttackBonus(choices.intelligence, choices.level),
            savingThrowProficiencies = coreTraits().savingThrowProficiencies,
            chosenSkills = choices.chosenSkills
        )
    }

    fun startingEquipmentOptions(): Map<StartingEquipment, List<String>> = mapOf(
        StartingEquipment.KIT_A to listOf(
            "Armadura de couro batido",
            "Adaga",
            "Ferramentas de ladrão",
            "Ferramentas de inventor",
            "Pacote de explorador de masmorras",
            "16 PO"
        ),
        StartingEquipment.GOLD_ONLY to listOf("150 PO")
    )

    fun canChooseSubclass(level: Int): Boolean = level >= 3

    fun availableSubclasses(): List<Subclass> = Subclass.entries
}
