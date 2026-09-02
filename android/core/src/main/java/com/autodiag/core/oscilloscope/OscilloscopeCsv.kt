package com.autodiag.core.oscilloscope

/** Deterministic CSV export for a single oscilloscope channel. */
object OscilloscopeCsv {
    fun export(capture: OscilloscopeCapture): String = buildString {
        appendLine("timestamp_nanos,voltage_volts")
        capture.samples.forEach { sample ->
            append(sample.timestampNanos)
            append(',')
            append(sample.voltage)
            appendLine()
        }
    }
}

/** Simple text replay format: one timestamp/voltage pair per line. */
object OscilloscopeReplay {
    fun encode(capture: OscilloscopeCapture): String = buildString {
        appendLine("WICAN-OSC-REPLAY,1")
        appendLine("channel=${capture.channel}")
        appendLine("sample_rate_hz=${capture.sampleRateHz}")
        capture.samples.forEach { sample ->
            append(sample.timestampNanos)
            append(',')
            append(sample.voltage)
            appendLine()
        }
    }

    fun decode(text: String): OscilloscopeCapture {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        require(lines.firstOrNull() == "WICAN-OSC-REPLAY,1") { "Unsupported replay format" }
        val channel = lines.firstOrNull { it.startsWith("channel=") }
            ?.substringAfter('=')?.toIntOrNull() ?: error("Missing channel")
        val sampleRateHz = lines.firstOrNull { it.startsWith("sample_rate_hz=") }
            ?.substringAfter('=')?.toLongOrNull() ?: error("Missing sample rate")
        val samples = lines.dropWhile { !it.matches(Regex("^-?\\d+,.*$")) }.map { line ->
            val parts = line.split(',', limit = 2)
            require(parts.size == 2)
            OscilloscopeSample(parts[0].toLong(), parts[1].toDouble())
        }
        return OscilloscopeCapture(channel, samples, sampleRateHz)
    }
}
