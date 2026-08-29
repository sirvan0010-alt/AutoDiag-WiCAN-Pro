package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327Session

fun interface CapabilityProbe {
    suspend fun probe(session: Elm327Session): Capability
}

object BuiltinProbes {
    val communication = CapabilityProbe { session ->
        probeSimple(session, "ATI", CapabilityIds.COMMUNICATION, "Komunikace s adaptérem", "Adaptér odpověděl na identifikační příkaz.", "Adaptér neodpověděl na identifikační příkaz.", verified = true)
    }

    val protocol = CapabilityProbe { session ->
        probeSimple(session, "ATDP", CapabilityIds.OBD_PROTOCOL, "OBD protokol", "Protokol byl zjištěn z adaptéru.", "Adaptér neposkytl informaci o protokolu.")
    }

    val vin = CapabilityProbe { session ->
        val result = runCatching { session.command("0902") }
        val now = System.currentTimeMillis()
        if (result.isFailure) return@CapabilityProbe Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.ERROR, result.exceptionOrNull()?.message)
        val body = result.getOrNull().orEmpty()
        if (looksLikeNoData(body) || body.contains("UNABLE", true)) {
            return@CapabilityProbe Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo")
        }
        val vin = extractVin(body)
        Capability(
            CapabilityIds.OBD_VIN,
            "VIN",
            if (vin != null) CapabilityStatus.AVAILABLE else CapabilityStatus.PARTIAL,
            vin ?: body.take(80),
            if (vin != null) VerificationState.PARTIALLY_VERIFIED else VerificationState.UNVERIFIED
        )
    }

    val dtc = CapabilityProbe { session ->
        probeSimple(session, "03", CapabilityIds.OBD_DTC, "Chybové kódy (DTC)", "Řídicí jednotka odpověděla na Mode 03.", "Vozidlo údaj neposkytlo.")
    }

    val liveData = CapabilityProbe { session ->
        probeSimple(session, "010C", CapabilityIds.OBD_LIVE_DATA, "Živá data (OBD)", "Vozidlo odpovědělo na PID 010C.", "Vozidlo údaj neposkytlo.")
    }

    val defaultSequence = listOf(communication, protocol, vin, dtc, liveData)

    private suspend fun probeSimple(session: Elm327Session, command: String, id: String, name: String, available: String, unavailable: String, verified: Boolean = false): Capability {
        val result = runCatching { session.command(command) }
        if (result.isFailure) return Capability(id, name, CapabilityStatus.ERROR, result.exceptionOrNull()?.message)
        val body = result.getOrNull().orEmpty()
        if (looksLikeNoData(body)) return Capability(id, name, CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo")
        return Capability(id, name, CapabilityStatus.AVAILABLE, body.lineSequence().firstOrNull()?.trim()?.take(80), if (verified) VerificationState.VERIFIED else VerificationState.PARTIALLY_VERIFIED)
    }

    internal fun looksLikeNoData(body: String): Boolean {
        val u = body.uppercase()
        return u.isBlank() || u.contains("NO DATA") || u.contains("NODATA") || u.contains("NOT CONNECTED") || u.contains("UNABLE TO CONNECT") || (u.contains("BUS INIT") && u.contains("ERROR"))
    }

    internal fun extractVin(body: String): String? = Regex("[A-HJ-NPR-Z0-9]{17}").find(body.uppercase().replace("\\s+".toRegex(), ""))?.value
}
