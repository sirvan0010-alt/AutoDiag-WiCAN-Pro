package com.autodiag.core.obd

/** Meaning of one Mode 06 test identifier. */
data class Mode06TestDefinition(
    val obdMid: Int,
    val testId: Int,
    val labelCs: String,
    val description: String,
    val unitAndScalingId: Int? = null,
    val standardized: Boolean = true,
    val verification: String = "SAE J1979 / J1979DA",
) {
    init {
        require(obdMid in 0..0xFF)
        require(testId in 0..0xFF)
        require(unitAndScalingId == null || unitAndScalingId in 0..0xFF)
    }
}

/**
 * Optional TID layer. A MID alone is not enough to identify a test.
 * Unknown combinations remain raw and are never assigned a guessed meaning.
 */
object Mode06TestRegistry {
    private val definitions = listOf(
        Mode06TestDefinition(
            obdMid = 0x01,
            testId = 0x08,
            labelCs = "Max. napětí O2 B1S1",
            description = "Maximum sensor voltage for test cycle",
            unitAndScalingId = 0x0A,
        ),
    )

    private val exact = definitions.associateBy { Triple(it.obdMid, it.testId, it.unitAndScalingId) }
    private val byMidTid = definitions
        .filter { it.unitAndScalingId == null }
        .associateBy { it.obdMid to it.testId }

    fun get(obdMid: Int, testId: Int, unitAndScalingId: Int): Mode06TestDefinition? =
        exact[Triple(obdMid, testId, unitAndScalingId)] ?: byMidTid[obdMid to testId]
}
