package com.autodiag.core.obd

class Mode06Reader(private val session: Elm327Session) {
    suspend fun discover(): Mode06DiscoveryResult {
        val windows = mutableListOf<Mode06DiscoveryWindow>()
        var base = 0x00
        while (base <= 0xE0) {
            val response = session.command(Mode06DiscoveryRequest(base).toCommand())
            val window = Mode06DiscoveryDecoder.decode(response, base) ?: break
            windows += window
            val next = Mode06DiscoveryDecoder.nextBase(window) ?: break
            base = next
        }
        return Mode06DiscoveryResult(windows)
    }

    suspend fun readSupportedMonitors(): List<ObdMode06TestResult> =
        discover().supportedMids.flatMap { mid ->
            Mode06Decoder.decode(session.command(ObdMode06Request(mid).toCommand()))?.results.orEmpty()
        }
}
