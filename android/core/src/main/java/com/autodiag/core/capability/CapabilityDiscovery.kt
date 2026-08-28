package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327Session

/**
 * Capability Discovery M1: only reports what can be observed from ELM/OBD
 * responses. It never invents OEM/Tesla signals or vehicle values.
 */
class CapabilityDiscovery {
    suspend fun run(session: Elm327Session): CapabilitySnapshot {
        if (!session.isInitialized) session.initialize().getOrThrow()

        val results = linkedMapOf<String, Capability>()
        var adapterInfo: String? = null
        var vin: String? = null

        results[CapabilityIds.COMMUNICATION] = probeCommunication(session).also {
            adapterInfo = it.detail
        }
        results[CapabilityIds.OBD_PROTOCOL] = probeProtocol(session)
        results[CapabilityIds.OBD_VIN] = probeVin(session).also {
            if (it.status == CapabilityStatus.AVAILABLE) vin = it.detail
        }
        results[CapabilityIds.OBD_MODE_03] = probeMode03(session)
        results[CapabilityIds.OBD_MODE_01] = probeMode01(session)

        return CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vin, adapterInfo = adapterInfo),
            capabilities = results,
            scopeKey = if (!vin.isNullOrBlank()) "vin:$vin" else "session"
        )
    }

    private suspend fun probeCommunication(session: Elm327Session): Capability = try {
        val body = session.command("ATI")
        if (looksLikeNoData(body)) {
            Capability(
                CapabilityIds.COMMUNICATION,
                "Komunikace s adaptérem",
                CapabilityStatus.ERROR,
                body.take(80),
                "Adaptér neodpověděl na identifikační příkaz."
            )
        } else {
            Capability(
                CapabilityIds.COMMUNICATION,
                "Komunikace s adaptérem",
                CapabilityStatus.AVAILABLE,
                body.lineSequence().firstOrNull()?.trim()?.take(80),
                "Spojení s adaptérem je aktivní.",
                VerificationState.VERIFIED
            )
        }
    } catch (t: Throwable) {
        Capability(
            CapabilityIds.COMMUNICATION,
            "Komunikace s adaptérem",
            CapabilityStatus.ERROR,
            t.message,
            "Spojení s adaptérem selhalo. Zkontrolujte Wi-Fi, IP a AP/Client Isolation."
        )
    }

    private suspend fun probeProtocol(session: Elm327Session): Capability = try {
        val body = session.command("ATDP")
        if (looksLikeNoData(body)) {
            Capability(
                CapabilityIds.OBD_PROTOCOL,
                "OBD protokol",
                CapabilityStatus.UNAVAILABLE,
                body.take(80),
                "Adaptér neposkytl informaci o protokolu."
            )
        } else {
            Capability(
                CapabilityIds.OBD_PROTOCOL,
                "OBD protokol",
                CapabilityStatus.AVAILABLE,
                body.lineSequence().firstOrNull()?.trim()?.take(80),
                "Protokol byl zjištěn z adaptéru.",
                VerificationState.PARTIALLY_VERIFIED
            )
        }
    } catch (t: Throwable) {
        Capability(
            CapabilityIds.OBD_PROTOCOL,
            "OBD protokol",
            CapabilityStatus.ERROR,
            t.message,
            "Dotaz na protokol selhal."
        )
    }

    private suspend fun probeVin(session: Elm327Session): Capability = try {
        val body = session.command("0902")
        if (looksLikeNoData(body) || body.contains("UNABLE", ignoreCase = true)) {
            Capability(
                CapabilityIds.OBD_VIN,
                "VIN",
                CapabilityStatus.UNAVAILABLE,
                body.take(80),
                "Vozidlo údaj neposkytlo. AutoDiag se pokusil načíst VIN (Mode 09 PID 02), ale hodnota nebyla dostupná. AutoDiag ji nedopočítává."
            )
        } else {
            val vin = extractVin(body)
            Capability(
                CapabilityIds.OBD_VIN,
                "VIN",
                if (vin != null) CapabilityStatus.AVAILABLE else CapabilityStatus.PARTIAL,
                vin ?: body.take(80),
                if (vin != null) "VIN bylo načteno z odpovědi vozidla." else "Odpověď na VIN přišla, ale formát se nepodařilo spolehlivě dekódovat.",
                if (vin != null) VerificationState.PARTIALLY_VERIFIED else VerificationState.UNVERIFIED
            )
        }
    } catch (t: Throwable) {
        Capability(
            CapabilityIds.OBD_VIN,
            "VIN",
            CapabilityStatus.ERROR,
            t.message,
            "Načtení VIN selhalo kvůli chybě komunikace."
        )
    }

    private suspend fun probeMode03(session: Elm327Session): Capability = try {
        val body = session.command("03")
        when {
            body.contains("UNABLE TO CONNECT", ignoreCase = true) || body.contains("NOT CONNECTED", ignoreCase = true) ->
                Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.UNAVAILABLE, body.take(80), "Vozidlo údaj neposkytlo. Dotaz Mode 03 se nepodařilo dokončit.")
            looksLikeNoData(body) ->
                Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.PARTIAL, body.take(80), "Mode 03 nevrátil DTC data. To samo o sobě neznamená chybu komunikace; vozidlo může mít prázdný seznam kódů.", VerificationState.PARTIALLY_VERIFIED)
            else ->
                Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.AVAILABLE, body.lineSequence().firstOrNull()?.trim()?.take(80), "Řídicí jednotka odpověděla na Mode 03. Dekódování DTC je samostatný krok.", VerificationState.PARTIALLY_VERIFIED)
        }
    } catch (t: Throwable) {
        Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.ERROR, t.message, "Dotaz Mode 03 selhal.")
    }

    private suspend fun probeMode01(session: Elm327Session): Capability = try {
        val body = session.command("010C")
        when {
            body.contains("UNABLE TO CONNECT", ignoreCase = true) || body.contains("NOT CONNECTED", ignoreCase = true) ->
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.UNAVAILABLE, body.take(80), "Vozidlo údaj neposkytlo. Základní PID Mode 01 nebyl dostupný.")
            looksLikeNoData(body) ->
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.UNAVAILABLE, body.take(80), "Vozidlo údaj neposkytlo. Základní PID 010C v této konfiguraci nevrátil data.")
            else ->
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.AVAILABLE, body.lineSequence().firstOrNull()?.trim()?.take(80), "Vozidlo odpovědělo na základní PID Mode 01.", VerificationState.PARTIALLY_VERIFIED)
        }
    } catch (t: Throwable) {
        Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.ERROR, t.message, "Dotaz Mode 01 selhal.")
    }

    companion object {
        fun looksLikeNoData(body: String): Boolean {
            val u = body.uppercase()
            return u.isBlank() || u.contains("NO DATA") || u.contains("NODATA") ||
                (u.contains("?") && u.length < 8) || u.contains("NOT CONNECTED") ||
                u.contains("UNABLE TO CONNECT") || (u.contains("BUS INIT") && u.contains("ERROR"))
        }

        fun extractVin(body: String): String? {
            val cleaned = body.uppercase()
                .replace("SEARCHING...", "")
                .replace("\\s+".toRegex(), "")
            return Regex("[A-HJ-NPR-Z0-9]{17}").find(cleaned)?.value
        }
    }
}
