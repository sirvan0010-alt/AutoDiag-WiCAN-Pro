package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.obd.ObdPidRegistry

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
        val mode01Probe = probeMode01(session)
        results[CapabilityIds.OBD_MODE_01] = mode01Probe.capability

        return CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vin, adapterInfo = adapterInfo),
            capabilities = results,
            vinAudit = vinAudit,
            scopeKey = if (!vin.isNullOrBlank()) "vin:$vin" else "session",
            obdMode01SupportedPids = mode01Probe.supportedDecoderPids
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
                    reference, mismatchText ?: "VIN bylo načteno z odpovědi vozidla.",
                    VerificationState.PARTIALLY_VERIFIED),
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
        val supportedDecoderPids: Set<Int>
    )

    private suspend fun probeMode01(session: Elm327Session): Mode01Probe = try {
        val responses = mutableListOf<String>()
        val first = session.command("0100")
        responses += first
        if (!looksLikeNoData(first) && ObdMode01PidBitmap.parse(first).isNotEmpty()) {
            if (ObdMode01PidBitmap.advertisesRange(first, 0x20)) {
                val second = session.command("0120")
                responses += second
                if (!looksLikeNoData(second) && ObdMode01PidBitmap.advertisesRange(second, 0x40)) {
                    responses += session.command("0140")
                }
            }
        }

        val combined = responses.joinToString("\n")
        val supported = ObdMode01PidBitmap.supportedDecoderPids(combined, ObdPidRegistry.definitions.keys)
        when {
            responses.all(::looksLikeNoData) -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.UNAVAILABLE, combined.take(160), "Vozidlo neposkytlo Mode 01 supported-PID bitmapu."),
                emptySet()
            )
            supported.isNotEmpty() -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.AVAILABLE,
                    "Dekódovatelné ECU-inzerované PID: ${supported.sorted().joinToString { "0x%02X".format(it) }}",
                    "PIDy jsou vystaveny pouze tehdy, když je ECU inzeruje v Mode 01 bitmapě a AutoDiag pro ně má explicitní dekodér.",
                    VerificationState.PARTIALLY_VERIFIED),
                supported
            )
            else -> Mode01Probe(
                Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.PARTIAL, combined.take(160), "Mode 01 odpověděl, ale žádný z inzerovaných PIDů zatím nemá podporovaný dekodér."),
                emptySet()
            )
        }
    } catch (t: Throwable) {
        Mode01Probe(
            Capability(CapabilityIds.OBD_MODE_01, "Živá data (Mode 01)", CapabilityStatus.ERROR, t.message, "Dotaz na Mode 01 supported-PID bitmapu selhal."),
            emptySet()
        )
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
