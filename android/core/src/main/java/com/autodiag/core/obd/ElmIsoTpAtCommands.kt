package com.autodiag.core.obd

/**
 * Public ELM327 / STN ISO-TP helper AT commands (ELM327 datasheet).
 * Does not encode vehicle-specific ECU IDs as AVAILABLE.
 */
object ElmIsoTpAtCommands {
    fun setHeader(canId11: Int): String {
        require(canId11 in 0..0x7FF) { "11-bit CAN ID only" }
        return "ATSH%03X".format(canId11)
    }

    fun flowControlHeader(canId11: Int): String {
        require(canId11 in 0..0x7FF)
        return "ATFCSH%03X".format(canId11)
    }

    fun flowControlMode(mode: Int = 1): String {
        require(mode in 0..2)
        return "ATFCSM$mode"
    }

    fun flowControlData(hex: String = "300000"): String {
        val clean = hex.uppercase().replace(" ", "")
        require(clean.matches(Regex("[0-9A-F]{6}")))
        return "ATFCSD$clean"
    }

    fun setupSequence(requestId11: Int): List<String> = listOf(
        setHeader(requestId11),
        flowControlHeader(requestId11),
        flowControlMode(1),
        flowControlData(),
    )
}
