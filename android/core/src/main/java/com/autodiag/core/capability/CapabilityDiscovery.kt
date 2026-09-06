package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327Session

/**
 * Read-only capability discovery. Every value is derived from an ECU/adapter
 * response; the app never invents missing vehicle data.
 */
class CapabilityDiscovery {
    suspend fun run(session: Elm327Session): CapabilitySnapshot {
        if (!session.isInitialized) session.initialize().getOrThrow()

        val results = linkedMapOf<String, Capability>()
        var adapterInfo: String? = null
        var vin: String? = null
        var vinAudit = VinAudit()

        results[CapabilityIds.COMMUNICATION] = probeCommunication(session).also { adapterInfo = it.detail }
        results[CapabilityIds.OBD_PROTOCOL] = probeProtocol(session)

        val vinProbe = probeVin(session)
        results[CapabilityIds.OBD_VIN] = vinProbe.capability
        vin = vinProbe.referenceVin
        vinAudit = VinAudit(referenceVin = vin, ecuRecords = vinProbe.ecuRecords)

        results[CapabilityIds.OBD_MODE_03] = probeMode03(session)
        val mode01 = probeMode01(session)
        results[CapabilityIds.OBD_MODE_01] = mode01.capability

        return CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vin, adapterInfo = adapterInfo),
            capabilities = results,
            vinAudit = vinAudit,
            scopeKey = if (!vin.isNullOrBlank()) "vin:$vin" else "session",
            mode01SupportedPids = mode01.supportedPids
        )
    }

    private suspend fun probeCommunication(session: Elm327Session): Capability = try {
        val body = session.command("ATI")
        if (looksLikeNoData(body)) Capability(
            CapabilityIds.COMMUNICATION, "Komunikace s adaptérem", CapabilityStatus.ERROR,
            body.take(80), "Adaptér neodpověděl na identifikační příkaz."
        ) else Capability(
            CapabilityIds.COMMUNICATION, "Komunikace s adaptérem", CapabilityStatus.AVAILABLE,
            body.lineSequence().firstOrNull()?.trim()?.take(80), "Spojení s adaptérem je aktivní.", VerificationState.VERIFIED
        )
    } catch (t: Throwable) {
        Capability(CapabilityIds.COMMUNICATION, "Komunikace s adaptérem", CapabilityStatus.ERROR,
            t.message, "Spojení s adaptérem selhalo. Zkontrolujte Wi-Fi, IP a AP/Client Isolation.")
    }

    private suspend fun probeProtocol(session: Elm327Session): Capability = try {
        val body = session.command("ATDP")
        if (looksLikeNoData(body)) Capability(
            CapabilityIds.OBD_PROTOCOL, "OBD protokol", CapabilityStatus.UNAVAILABLE,
            body.take(80), "Adaptér neposkytl informaci o protokolu."
        ) else Capability(
            CapabilityIds.OBD_PROTOCOL, "OBD protokol", CapabilityStatus.AVAILABLE,
            body.lineSequence().firstOrNull()?.trim()?.take(80), "Protokol byl zjištěn z adaptéru.", VerificationState.PARTIALLY_VERIFIED
        )
    } catch (t: Throwable) {
        Capability(CapabilityIds.OBD_PROTOCOL, "OBD protokol", CapabilityStatus.ERROR,
            t.message, "Dotaz na protokol selhal.")
    }

    private data class VinProbe(
        val capability: Capability,
        val referenceVin: String?,
        val ecuRecords: List<EcuVinRecord>
    )

    private suspend fun probeVin(session: Elm327Session): VinProbe = try {
        val body = session.command("0902")
        val records = VinResponseParser.parse(body)
        val distinctVins = records.map { it.vin }.distinct()
        val reference = records.firstOrNull()?.vin ?: extractVin(body)
        val mismatchText = if (distinctVins.size > 1) {
            "Nalezeny různé VIN v odpovědích ECU (${distinctVins.size} hodnoty)."
        } else null

        if (looksLikeNoData(body) || body.contains("UNABLE", ignoreCase = true)) {
            VinProbe(
                Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.UNAVAILABLE, body.take(80),
                    "Vozidlo údaj neposkytlo. AutoDiag se pokusil načíst VIN (Mode 09 PID 02), ale hodnota nebyla dostupná. AutoDiag ji nedopočítává."),
                null, emptyList()
            )
        } else if (reference != null) {
            VinProbe(
                Capability(CapabilityIds.OBD_VIN, "VIN", if (mismatchText == null) CapabilityStatus.AVAILABLE else CapabilityStatus.PARTIAL,
                    reference, mismatchText ?: "VIN bylo načteno z odpovědi vozidla.", VerificationState.PARTIALLY_VERIFIED),
                reference, records
            )
        } else {
            VinProbe(
                Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.PARTIAL, body.take(80),
                    "Odpověď na VIN přišla, ale formát se nepodařilo spolehlivě dekódovat."),
                null, records
            )
        }
    } catch (t: Throwable) {
        VinProbe(
            Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.ERROR, t.message,
                "Načtení VIN selhalo kvůli chybě komunikace."),
            null, emptyList()
        )
    }

    private suspend fun probeMode03(session: Elm327Session): Capability = try {
        val body = session.command("03")
        when {
            body.contains("UNABLE TO CONNECT", true) || body.contains("NOT CONNECTED", true) ->
                Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.UNAVAILABLE, body.take(80), "Vozidlo údaj neposkytlo. Dotaz Mode 03 se nepodařilo dokončit.")
            looksLikeNoData(body) ->
                Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.PARTIAL, body.take(80), "Mode 03 nevrátil DTC data. To samo o sobě neznamená chybu komunikace; vozidlo může mít prázdný seznam kódů.", VerificationState.PARTIALLY_VERIFIED)
            else -> Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.AVAILABLE, body.lineSequence().firstOrNull()?.trim()?.take(80), "Řídicí jednotka odpověděla na Mode 03. Dekódování DTC je samostatný krok.", VerificationState.PARTIALLY_VERIFIED)
        }
    } catch (t: Throwable) {
        Capability(CapabilityIds.OBD_MODE_03, "Chybové kódy (Mode 03)", CapabilityStatus.ERROR, t.message, "Dotaz Mode 03 selhal.")
    }

    private data class Mode01Probe(
        val capability: Capability,
        val supportedPids: Set<Int>
    )

    private suspend fun probeMode01(session: Elm327Session): Mode01Probe = try {
        val supported = discoverSupportedMode01Pids(session)
        val rpmResponse = session.command("010C")
        val rpmAvailable = !looksLikeNoData(rpmResponse) &&
            rpmResponse.contains("41 0C", ignoreCase = true).let { it || rpmResponse.replace(" ", "").contains("410C", ignoreCase = true) }
        val usable = if (rpmAvailable) supported + 0x0C else supported
        when {
            usable.isNotEmpty() -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.AVAILABLE,
                    "${usable.size} podporovaných PID", "ECU deklarovala podporované standardní PIDy přes Mode 01 bitmapy.", VerificationState.PARTIALLY_VERIFIED),
                usable
            )
            rpmAvailable -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.PARTIAL,
                    rpmResponse.lineSequence().firstOrNull()?.trim()?.take(80), "PID 010C odpověděl, ale ECU neposkytla čitelnou supported-PID bitmapu.", VerificationState.PARTIALLY_VERIFIED),
                setOf(0x0C)
            )
            else -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.UNAVAILABLE,
                    rpmResponse.take(80), "Vozidlo údaj neposkytlo. Nebyla potvrzena dostupnost standardních Mode 01 PIDů."),
                emptySet()
            )
        }
    } catch (t: Throwable) {
        Mode01Probe(
            Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.ERROR, t.message, "Dotaz Mode 01 selhal."),
            emptySet()
        )
    }

    /** Reads SAE supported-PID pages: 0100, 0120, 0140, 0160, 0180, 01A0, 01C0. */
    private suspend fun discoverSupportedMode01Pids(session: Elm327Session): Set<Int> {
        val supported = linkedSetOf<Int>()
        var basePid = 0x00
        repeat(7) {
            val response = session.command("01${basePid.toString(16).padStart(2, '0')}")
            val bytes = parseMode01Payload(response, basePid)
            if (bytes == null || bytes.size < 4) return@repeat
            val bitmap = (bytes[0].toLong() shl 24) or
                (bytes[1].toLong() shl 16) or
                (bytes[2].toLong() shl 8) or bytes[3].toLong()
            for (bit in 0 until 32) {
                if ((bitmap and (1L shl (31 - bit))) != 0L) supported += basePid + bit + 1
            }
            if ((bitmap and 1L) == 0L) return@repeat
            basePid += 0x20
        }
        return supported
    }

    private fun parseMode01Payload(response: String, requestedPid: Int): List<Int>? {
        val normalized = response.uppercase().replace("SEARCHING...", "")
        val lines = normalized.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith(">") }
        val line = lines.firstOrNull { line ->
            val compact = line.replace(Regex("[^0-9A-F]"), "")
            compact.startsWith("41${requestedPid.toString(16).padStart(2, '0').uppercase()}")
        } ?: return null
        val tokens = line.split(Regex("[^0-9A-F]+"), RegexOption.IGNORE_CASE).filter { it.length == 2 }
        if (tokens.size < 6) return null
        return tokens.drop(2).take(4).mapNotNull { it.toIntOrNull(16) }
    }

    companion object {
        fun looksLikeNoData(body: String): Boolean {
            val u = body.uppercase()
            return u.isBlank() || u.contains("NO DATA") || u.contains("NODATA") ||
                (u.contains("?") && u.length < 8) || u.contains("NOT CONNECTED") ||
                u.contains("UNABLE TO CONNECT") || (u.contains("BUS INIT") && u.contains("ERROR"))
        }

        fun extractVin(body: String): String? = Regex("[A-HJ-NPR-Z0-9]{17}")
            .find(body.uppercase().replace("SEARCHING...", "").replace("\\s+".toRegex(), ""))?.value
    }
}
