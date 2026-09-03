package com.autodiag.core.obd

/** SAE J1979 PID 01 — MIL, DTC count, continuous/non-continuous monitors. */
data class ReadinessReport(
    val milOn: Boolean,
    val storedDtcCount: Int,
    val sparkIgnition: Boolean?,
    val monitors: List<MonitorStatus>,
)

data class MonitorStatus(
    val id: String,
    val nameCs: String,
    val supported: Boolean,
    val complete: Boolean?,
)

object ObdReadinessDecoder {
    fun decodePid01(data: List<Int>): ReadinessReport? {
        if (data.size < 4) return null
        val a = data[0]
        val b = data[1]
        val c = data[2]
        val d = data[3]
        val mil = (a and 0x80) != 0
        val count = a and 0x7F
        val spark = (b and 0x08) == 0
        val monitors = mutableListOf<MonitorStatus>()
        fun add(v: Int, supportMask: Int, readyMask: Int, id: String, name: String) {
            val supported = (v and supportMask) != 0
            val incomplete = (v and readyMask) != 0
            monitors += MonitorStatus(id, name, supported, if (supported) !incomplete else null)
        }
        add(b, 0x01, 0x10, "misfire", "Výpadky zapalování")
        add(b, 0x02, 0x20, "fuel", "Palivový systém")
        add(b, 0x04, 0x40, "ccm", "Komplexní součásti")
        if (spark) {
            fun cd(mask: Int, id: String, name: String) {
                val supported = (c and mask) != 0
                val incomplete = (d and mask) != 0
                monitors += MonitorStatus(id, name, supported, if (supported) !incomplete else null)
            }
            cd(0x01, "cat", "Katalyzátor")
            cd(0x02, "hcat", "Vyhřívaný katalyzátor")
            cd(0x04, "evap", "EVAP")
            cd(0x08, "sair", "Sekundární vzduch")
            cd(0x10, "ac", "A/C chladivo")
            cd(0x20, "o2", "Kyslíkový senzor")
            cd(0x40, "o2h", "Ohřev O2")
            cd(0x80, "egr", "EGR/VVT")
        } else {
            fun cd(mask: Int, id: String, name: String) {
                val supported = (c and mask) != 0
                val incomplete = (d and mask) != 0
                monitors += MonitorStatus(id, name, supported, if (supported) !incomplete else null)
            }
            cd(0x01, "nmhc", "NMHC katalyzátor")
            cd(0x02, "nox", "NOx/SCR")
            cd(0x04, "boost", "Boost pressure")
            cd(0x08, "egs", "Výfukový senzor")
            cd(0x20, "pf", "Filtr pevných částic")
            cd(0x80, "egr", "EGR/VVT")
        }
        return ReadinessReport(mil, count, spark, monitors)
    }
}
