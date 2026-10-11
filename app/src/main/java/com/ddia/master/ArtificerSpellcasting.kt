package com.ddia.master

/**
 * Estado de conjuração do Artífice. O catálogo de magias fica separado.
 * Este módulo controla preparação, espaços e descanso sem depender da interface.
 */
object ArtificerSpellcasting {
    data class SpellState(
        val level: Int,
        val preparedSpells: Set<String>,
        val knownCantrips: Set<String>,
        /** Índice 0 = espaços de 1º círculo. */
        val remainingSlots: List<Int>
    )

    fun newState(level: Int, preparedSpells: Set<String>, knownCantrips: Set<String>): SpellState {
        val progression = ArtificerRules.progression(level)
        require(preparedSpells.size == progression.spellsPrepared) { "No nível $level, prepare exatamente ${progression.spellsPrepared} magias." }
        require(knownCantrips.size == progression.cantripsKnown) { "No nível $level, conheça exatamente ${progression.cantripsKnown} truques." }
        require(preparedSpells.none { it.isBlank() } && knownCantrips.none { it.isBlank() }) { "Os nomes das magias não podem ficar vazios." }
        return SpellState(level, preparedSpells, knownCantrips, progression.spellSlots)
    }

    fun castWithSlot(state: SpellState, spellName: String, slotLevel: Int): SpellState {
        require(spellName in state.preparedSpells) { "A magia precisa estar preparada antes de ser conjurada usando espaço." }
        require(slotLevel in 1..5) { "O círculo do espaço deve estar entre 1 e 5." }
        require(slotLevel <= highestAvailableSpellLevel(state.level)) { "O Artífice ainda não tem acesso a espaços desse círculo." }
        val index = slotLevel - 1
        require(state.remainingSlots[index] > 0) { "Não há espaços de ${slotLevel}º círculo disponíveis." }
        val slots = state.remainingSlots.toMutableList()
        slots[index]--
        return state.copy(remainingSlots = slots)
    }

    fun castCantrip(state: SpellState, cantripName: String): SpellState {
        require(cantripName in state.knownCantrips) { "O truque não é conhecido por este Artífice." }
        return state
    }

    fun replacePreparedSpell(state: SpellState, oldSpell: String, newSpell: String): SpellState {
        require(oldSpell in state.preparedSpells) { "A magia antiga não está preparada." }
        require(newSpell.isNotBlank()) { "Informe a nova magia." }
        require(newSpell !in state.preparedSpells) { "A nova magia já está preparada." }
        return state.copy(preparedSpells = (state.preparedSpells - oldSpell) + newSpell)
    }

    fun replaceCantrip(state: SpellState, oldCantrip: String, newCantrip: String): SpellState {
        require(oldCantrip in state.knownCantrips) { "O truque antigo não é conhecido." }
        require(newCantrip.isNotBlank()) { "Informe o novo truque." }
        require(newCantrip !in state.knownCantrips) { "O novo truque já é conhecido." }
        return state.copy(knownCantrips = (state.knownCantrips - oldCantrip) + newCantrip)
    }

    /** Descanso longo restaura os espaços; permite trocar um truque conhecido. */
    fun longRest(state: SpellState, replacementCantrip: String? = null): SpellState {
        val progression = ArtificerRules.progression(state.level)
        var cantrips = state.knownCantrips
        if (replacementCantrip != null) {
            require(replacementCantrip.isNotBlank()) { "O truque substituto não pode ficar vazio." }
            require(replacementCantrip !in cantrips) { "O Artífice já conhece esse truque." }
            cantrips = (cantrips - cantrips.first()) + replacementCantrip
        }
        return state.copy(knownCantrips = cantrips, remainingSlots = progression.spellSlots)
    }

    fun highestAvailableSpellLevel(level: Int): Int =
        ArtificerRules.progression(level).spellSlots.indexOfLast { it > 0 } + 1
}
