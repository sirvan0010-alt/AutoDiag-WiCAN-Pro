package com.autodiag.core.obd

/** Standardized meaning of an OBD-II Mode 06 monitor identifier. */
data class Mode06MonitorDefinition(
    val obdMid: Int,
    val labelCs: String,
    val description: String,
    val standardized: Boolean = true,
    val verification: String = "SAE J1979 / J1979DA"
) {
    init { require(obdMid in 0..0xFF) }
}

/** Standard monitor definitions; manufacturer-defined IDs stay unknown. */
object Mode06MonitorRegistry {
    private val definitions = buildList {
        for (sensor in 1..4) add(Mode06MonitorDefinition(sensor, "O2 B1S$sensor", "Kyslíkový senzor – banka 1, senzor $sensor"))
        for (sensor in 1..4) add(Mode06MonitorDefinition(0x04 + sensor, "O2 B2S$sensor", "Kyslíkový senzor – banka 2, senzor $sensor"))
        for (sensor in 1..4) add(Mode06MonitorDefinition(0x08 + sensor, "O2 B3S$sensor", "Kyslíkový senzor – banka 3, senzor $sensor"))
        for (sensor in 1..4) add(Mode06MonitorDefinition(0x0C + sensor, "O2 B4S$sensor", "Kyslíkový senzor – banka 4, senzor $sensor"))
        add(Mode06MonitorDefinition(0x21, "Katalyzátor B1", "Monitor katalyzátoru – banka 1"))
        add(Mode06MonitorDefinition(0x22, "Katalyzátor B2", "Monitor katalyzátoru – banka 2"))
        add(Mode06MonitorDefinition(0x23, "Katalyzátor B3", "Monitor katalyzátoru – banka 3"))
        add(Mode06MonitorDefinition(0x24, "Katalyzátor B4", "Monitor katalyzátoru – banka 4"))
        add(Mode06MonitorDefinition(0x31, "EGR B1", "Monitor EGR – banka 1"))
        add(Mode06MonitorDefinition(0x32, "EGR B2", "Monitor EGR – banka 2"))
        add(Mode06MonitorDefinition(0x33, "EGR B3", "Monitor EGR – banka 3"))
        add(Mode06MonitorDefinition(0x34, "EGR B4", "Monitor EGR – banka 4"))
        add(Mode06MonitorDefinition(0x35, "VVT B1", "Monitor variabilního časování ventilů – banka 1"))
        add(Mode06MonitorDefinition(0x36, "VVT B2", "Monitor variabilního časování ventilů – banka 2"))
        add(Mode06MonitorDefinition(0x37, "VVT B3", "Monitor variabilního časování ventilů – banka 3"))
        add(Mode06MonitorDefinition(0x38, "VVT B4", "Monitor variabilního časování ventilů – banka 4"))
        add(Mode06MonitorDefinition(0x3D, "Purge flow", "Monitor průtoku odvzdušnění palivového systému"))
        add(Mode06MonitorDefinition(0x41, "O2 heater B1S1", "Ohřev kyslíkového senzoru – banka 1, senzor 1"))
        add(Mode06MonitorDefinition(0x42, "O2 heater B1S2", "Ohřev kyslíkového senzoru – banka 1, senzor 2"))
        add(Mode06MonitorDefinition(0x43, "O2 heater B1S3", "Ohřev kyslíkového senzoru – banka 1, senzor 3"))
        add(Mode06MonitorDefinition(0x44, "O2 heater B1S4", "Ohřev kyslíkového senzoru – banka 1, senzor 4"))
        add(Mode06MonitorDefinition(0x61, "Heated catalyst B1", "Vyhřívaný katalyzátor – banka 1"))
        add(Mode06MonitorDefinition(0x62, "Heated catalyst B2", "Vyhřívaný katalyzátor – banka 2"))
        add(Mode06MonitorDefinition(0x63, "Heated catalyst B3", "Vyhřívaný katalyzátor – banka 3"))
        add(Mode06MonitorDefinition(0x64, "Heated catalyst B4", "Vyhřívaný katalyzátor – banka 4"))
        add(Mode06MonitorDefinition(0x71, "Secondary air 1", "Monitor sekundárního vzduchu 1"))
        add(Mode06MonitorDefinition(0x72, "Secondary air 2", "Monitor sekundárního vzduchu 2"))
        add(Mode06MonitorDefinition(0x73, "Secondary air 3", "Monitor sekundárního vzduchu 3"))
        add(Mode06MonitorDefinition(0x74, "Secondary air 4", "Monitor sekundárního vzduchu 4"))
        add(Mode06MonitorDefinition(0x81, "Fuel system B1", "Monitor palivového systému – banka 1"))
        add(Mode06MonitorDefinition(0x82, "Fuel system B2", "Monitor palivového systému – banka 2"))
        add(Mode06MonitorDefinition(0x83, "Fuel system B3", "Monitor palivového systému – banka 3"))
        add(Mode06MonitorDefinition(0x84, "Fuel system B4", "Monitor palivového systému – banka 4"))
        add(Mode06MonitorDefinition(0x85, "Boost pressure B1", "Monitor regulace plnicího tlaku – banka 1"))
        add(Mode06MonitorDefinition(0x86, "Boost pressure B2", "Monitor regulace plnicího tlaku – banka 2"))
        add(Mode06MonitorDefinition(0xA1, "Misfire", "Monitor vynechávání zapalování – obecná data"))
        for (cylinder in 1..12) add(Mode06MonitorDefinition(0xA1 + cylinder, "Misfire cyl. $cylinder", "Monitor vynechávání zapalování – válec $cylinder"))
        add(Mode06MonitorDefinition(0xB0, "PM filter B1", "Monitor filtru pevných částic – banka 1"))
        add(Mode06MonitorDefinition(0xB1, "PM filter B2", "Monitor filtru pevných částic – banka 2"))
    }

    private val byMid = definitions.associateBy { it.obdMid }
    fun get(obdMid: Int): Mode06MonitorDefinition? = byMid[obdMid]
}

data class Mode06InterpretedResult(
    val raw: ObdMode06TestResult,
    val monitor: Mode06MonitorDefinition?,
    val test: Mode06TestDefinition?,
    val scaling: Mode06UasDefinition?,
    val value: Mode06ScaledValue?,
    val minimum: Mode06ScaledValue?,
    val maximum: Mode06ScaledValue?,
    val status: Mode06ResultStatus,
    val bandPosition: Double?,
    val labelCs: String,
)

object Mode06Interpreter {
    fun interpret(raw: ObdMode06TestResult): Mode06InterpretedResult {
        val monitor = Mode06MonitorRegistry.get(raw.obdMid)
        val test = Mode06TestRegistry.get(raw.obdMid, raw.testId, raw.unitAndScalingId)
        val scaling = Mode06UasRegistry.get(raw.unitAndScalingId)
        val label = test?.labelCs ?: monitor?.labelCs ?: "MID 0x%02X TID 0x%02X".format(raw.obdMid, raw.testId)
        if (scaling == null) {
            return Mode06InterpretedResult(raw, monitor, test, null, null, null, null, Mode06ResultStatus.UNKNOWN, null, label)
        }
        val value = scaling.decode(raw.testValueRaw)
        val min = scaling.decode(raw.minimumRaw)
        val max = scaling.decode(raw.maximumRaw)
        val v = value.value
        val lo = min.value
        val hi = max.value
        val status = if (v in lo..hi) Mode06ResultStatus.WITHIN_LIMITS else Mode06ResultStatus.OUTSIDE_LIMITS
        val band = if (hi > lo) ((v - lo) / (hi - lo)).coerceIn(0.0, 1.0) else null
        return Mode06InterpretedResult(raw, monitor, test, scaling, value, min, max, status, band, label)
    }
}
