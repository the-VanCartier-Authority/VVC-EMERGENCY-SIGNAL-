package com.vvc.emergencysignal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MorseSignalEngineTest {
    @Test
    fun sosContainsExpectedSignalDurations() {
        val pattern = MorseSignalEngine.sosPattern()
        assertEquals(true, pattern.first().on)
        assertEquals(200L, pattern.first().durationMs)
        assertEquals(600L, pattern[6].durationMs)
        assertTrue(pattern.last().durationMs >= 1_000L)
    }
}
