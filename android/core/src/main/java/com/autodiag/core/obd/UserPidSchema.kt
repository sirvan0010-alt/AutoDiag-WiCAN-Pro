package com.autodiag.core.obd

/**
 * Community / custom PID row. Same *shape* as public Torque user-PID CSV,
 * our own types — equations are not executed until verified.
 */
data class UserPidDefinition(
    val name: String,
    val shortName: String,
    val modeAndPid: String,
    val equation: String,
    val min: Double?,
    val max: Double?,
    val unit: String,
    val header: String?,
    val verification: String = "unverified",
) {
    init {
        require(modeAndPid.matches(Regex("[0-9A-Fa-f]{2,4}"))) {
            "modeAndPid must be hex like 010C"
        }
    }

    val isExecutable: Boolean get() = verification == "verified"
}

object UserPidCsv {
    const val HEADER =
        "Name,ShortName,ModeAndPID,Equation,Min Value,Max Value,Units,Header"

    fun parseLine(line: String): UserPidDefinition? {
        if (line.isBlank() || line.startsWith("#") || line.startsWith("Name")) return null
        val p = splitCsv(line)
        if (p.size < 7) return null
        return UserPidDefinition(
            name = p[0],
            shortName = p[1],
            modeAndPid = p[2].replace(" ", ""),
            equation = p[3],
            min = p[4].toDoubleOrNull(),
            max = p[5].toDoubleOrNull(),
            unit = p[6],
            header = p.getOrNull(7)?.ifBlank { null },
        )
    }

    private fun splitCsv(line: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var q = false
        for (ch in line) {
            when {
                ch == '"' -> q = !q
                ch == ',' && !q -> {
                    out += cur.toString().trim()
                    cur.clear()
                }
                else -> cur.append(ch)
            }
        }
        out += cur.toString().trim()
        return out
    }
}
