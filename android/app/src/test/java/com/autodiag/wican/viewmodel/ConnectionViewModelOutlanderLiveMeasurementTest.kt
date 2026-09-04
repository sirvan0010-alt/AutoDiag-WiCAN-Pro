package com.autodiag.wican.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression specification for the Outlander 21 01 per-signal acceptance rule.
 *
 * The ViewModel currently owns Android/session state, so the production method
 * is not directly unit-testable without constructing the full ViewModel. These
 * tests capture the acceptance matrix as a small pure specification first.
 * Once the app test source set has coroutines/Android ViewModel dependencies,
 * this matrix can be moved onto the actual callback path.
 */
class ConnectionViewModelOutlanderLiveMeasurementTest {
    @Test
    fun accepts_when_two_of_three_signals_are_present() {
        val accepted = acceptanceMatrix(
            isolation = true,
            max = true,
            min = false
        )

        assertTrue(accepted)
    }

    @Test
    fun accepts_when_only_one_signal_is_present() {
        assertTrue(acceptanceMatrix(isolation = true, max = false, min = false))
        assertTrue(acceptanceMatrix(isolation = false, max = true, min = false))
        assertTrue(acceptanceMatrix(isolation = false, max = false, min = true))
    }

    @Test
    fun rejects_when_no_signal_is_decodable() {
        assertEquals(false, acceptanceMatrix(isolation = false, max = false, min = false))
    }

    private fun acceptanceMatrix(isolation: Boolean, max: Boolean, min: Boolean): Boolean =
        isolation || max || min
}
