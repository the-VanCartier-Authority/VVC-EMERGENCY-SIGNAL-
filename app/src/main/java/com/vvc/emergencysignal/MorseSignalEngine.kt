package com.vvc.emergencysignal

data class MorseStep(val on: Boolean, val durationMs: Long)

object MorseSignalEngine {
    private const val UNIT_MS = 200L
    private const val SYMBOL_GAP_MS = UNIT_MS
    private const val LETTER_GAP_MS = UNIT_MS * 3
    private const val WORD_GAP_MS = UNIT_MS * 7

    fun sosPattern(): List<MorseStep> = buildList {
        appendCode("...")
        add(MorseStep(false, LETTER_GAP_MS))
        appendCode("---")
        add(MorseStep(false, LETTER_GAP_MS))
        appendCode("...")
        add(MorseStep(false, WORD_GAP_MS))
    }

    private fun MutableList<MorseStep>.appendCode(code: String) {
        code.forEachIndexed { index, symbol ->
            add(MorseStep(true, UNIT_MS * if (symbol == '.') 1 else 3))
            if (index < code.lastIndex) add(MorseStep(false, SYMBOL_GAP_MS))
        }
    }
}
