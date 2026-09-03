package com.autodiag.core.obd

/** Physical interpretation of a 16-bit Mode 06 value. */
data class Mode06ScaledValue(
    val value: Double,
    val unit: String,
)

/** Explicit Unit And Scaling ID definition. */
data class Mode06UasDefinition(
    val uasid: Int,
    val description: String,
    val decode: (Int) -> Mode06ScaledValue,
    val signed: Boolean,
    val verification: String,
)

/** Standard UASID registry. Unknown identifiers remain undecoded. */
object Mode06UasRegistry {
    private val definitions = listOf(
        Mode06UasDefinition(0x04, "Raw value, 0.001/bit", { raw -> Mode06ScaledValue(raw * 0.001, "") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x05, "Raw value, 0.0000305/bit", { raw -> Mode06ScaledValue(raw * 0.0000305, "") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x06, "Raw value, 0.000305/bit", { raw -> Mode06ScaledValue(raw * 0.000305, "") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x07, "Rotational frequency, 0.25 rpm/bit", { raw -> Mode06ScaledValue(raw * 0.25, "rpm") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x09, "Vehicle speed, 1 km/h/bit", { raw -> Mode06ScaledValue(raw.toDouble(), "km/h") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x0A, "Voltage, 0.122 mV/bit", { raw -> Mode06ScaledValue(raw * 0.000122, "V") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x10, "Time, 1 ms/bit", { raw -> Mode06ScaledValue(raw.toDouble(), "ms") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x20, "Ratio, 0.00390625/bit", { raw -> Mode06ScaledValue(raw / 256.0, "ratio") }, false, "J1979 Appendix E"),
        // Verified against ISO/SAE-derived Appendix E material: mass/time, 0.01 g/s per bit.
        Mode06UasDefinition(0x27, "Mass per time, 0.01 g/s/bit", { raw -> Mode06ScaledValue(raw * 0.01, "g/s") }, false, "ISO/SAE Appendix E"),
        Mode06UasDefinition(0x2B, "Count, 1 count/bit", { raw -> Mode06ScaledValue(raw.toDouble(), "count") }, false, "J1979 Appendix E"),
        Mode06UasDefinition(0x8C, "Signed voltage, 0.01 V/bit", { raw -> Mode06ScaledValue(s16(raw) * 0.01, "V") }, true, "J1979 Appendix E")
    )

    private val byId = definitions.associateBy { it.uasid }

    fun get(uasid: Int): Mode06UasDefinition? = byId[uasid]
    fun decode(uasid: Int, raw: Int): Mode06ScaledValue? = get(uasid)?.decode?.invoke(raw)

    private fun s16(raw: Int): Int = if ((raw and 0x8000) != 0) raw - 0x10000 else raw
}
