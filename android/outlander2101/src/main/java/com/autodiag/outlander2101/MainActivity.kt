package com.autodiag.outlander2101

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var value: TextView
    private lateinit var graph: GraphView
    private lateinit var button: Button

    private var socket: Socket? = null
    private var output: OutputStream? = null
    private var connectedPort = -1
    @Volatile private var running = false
    @Volatile private var polling = false

    private val history = mutableListOf<Float>()
    private val handler = Handler(Looper.getMainLooper())
    private val isoTp = IsoTpDecoder()

    private val poller = object : Runnable {
        override fun run() {
            if (!running || !polling) return
            sendRaw("2101")
            handler.postDelayed(this, 1200)
        }
    }

    private val reconnecter = object : Runnable {
        override fun run() {
            if (!running) connect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 20)
        }

        val title = TextView(this).apply {
            text = "Outlander PHEV • HV IZOLACE"
            textSize = 24f
        }
        button = Button(this).apply {
            text = "HV IZOLACE"
            isEnabled = false
        }
        status = TextView(this).apply {
            text = "WiCAN: PŘIPOJOVÁNÍ…"
            textSize = 16f
        }
        value = TextView(this).apply {
            text = "— kΩ"
            textSize = 42f
            setPadding(0, 24, 0, 12)
        }
        graph = GraphView()

        root.addView(title)
        root.addView(button)
        root.addView(status)
        root.addView(value)
        root.addView(graph, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        button.setOnClickListener {
            if (running) startPolling()
        }

        connect()
    }

    private fun connect() {
        if (running) return

        runOnUiThread {
            status.text = "WiCAN: PŘIPOJOVÁNÍ…"
            button.isEnabled = false
        }

        Thread {
            var lastError: Exception? = null
            for (port in intArrayOf(35000, 3333)) {
                try {
                    val s = Socket()
                    s.tcpNoDelay = true
                    s.soTimeout = 15000
                    s.connect(InetSocketAddress("192.168.0.10", port), 4000)

                    socket = s
                    output = s.getOutputStream()
                    connectedPort = port
                    running = true
                    polling = false
                    isoTp.reset()

                    sendCommand("ATZ", 1200)
                    sendCommand("ATE0", 120)
                    sendCommand("ATL0", 120)
                    sendCommand("ATS0", 120)
                    sendCommand("ATH1", 120)
                    sendCommand("ATCAF1", 120)
                    sendCommand("ATSP6", 120)
                    sendCommand("ATAT1", 120)
                    sendCommand("ATST64", 120)
                    sendCommand("ATFCSH761", 120)
                    sendCommand("ATFCSD300000", 120)
                    sendCommand("ATFCSM1", 120)
                    sendCommand("ATSH761", 120)

                    runOnUiThread {
                        status.text = "WiCAN: PŘIPOJENO • TCP $connectedPort • BMU 761→762"
                        button.isEnabled = true
                    }

                    readLoop()
                    break
                } catch (e: Exception) {
                    lastError = e
                    try { socket?.close() } catch (_: Exception) {}
                    socket = null
                    output = null
                    running = false
                    polling = false
                }
            }

            if (lastError != null && !running) disconnectAndRetry()
        }.start()
    }

    private fun sendCommand(command: String, waitMs: Long) {
        sendRaw(command)
        try { Thread.sleep(waitMs) } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
    }

    private fun startPolling() {
        if (!running) return
        polling = true
        history.clear()
        isoTp.reset()
        runOnUiThread {
            value.text = "— kΩ"
            graph.invalidate()
            status.text = "HV IZOLACE: měřím 21 01…"
        }
        handler.removeCallbacks(poller)
        sendRaw("2101")
        handler.postDelayed(poller, 1200)
    }

    private fun readLoop() {
        val input = socket?.getInputStream() ?: return
        val line = StringBuilder()

        try {
            while (running) {
                val b = input.read()
                if (b < 0) break
                when (b) {
                    '\r'.code, '\n'.code -> {
                        if (line.isNotEmpty()) {
                            processLine(line.toString())
                            line.setLength(0)
                        }
                    }
                    else -> {
                        if (b in 0x20..0x7E) line.append(b.toChar())
                        if (line.length > 512) line.setLength(0)
                    }
                }
            }
        } catch (_: Exception) {
            // reconnect below
        }

        disconnectAndRetry()
    }

    private fun processLine(line: String) {
        val normalized = line.trim()
        if (normalized.isEmpty() || normalized == ">") return

        val payload = isoTp.accept(normalized) ?: return
        decodeWatchdog2101(payload)
    }

    private fun decodeWatchdog2101(bytes: List<Int>) {
        val hexDump = bytes.joinToString(" ") { "%02X".format(it) }

        // Direct PHEV Watchdog evidence: Lz3/a 21 01 isolation resistance
        // is UInt16 BE at response token indices 78..79, unit kOhm.
        // Never manufacture a value when the vehicle payload is shorter.
        if (bytes.size <= 79) {
            runOnUiThread {
                if (polling) {
                    status.text = "HV IZOLACE: ${bytes.size} B • RAW: $hexDump"
                    value.text = "— kΩ"
                }
            }
            return
        }

        val risoKOhm = (bytes[78] * 256 + bytes[79]).toFloat()
        if (risoKOhm <= 0f || risoKOhm > 65535f) {
            runOnUiThread {
                if (polling) status.text = "HV IZOLACE: offset 78/79 mimo platný rozsah • RAW: $hexDump"
            }
            return
        }

        runOnUiThread {
            history.add(risoKOhm)
            if (history.size > 180) history.removeAt(0)
            value.text = String.format(Locale.US, "%.0f kΩ", risoKOhm)
            graph.invalidate()
            status.text = "HV IZOLACE: ŽIVÁ DATA • ${history.size} vzorků"
        }
    }

    private fun sendRaw(command: String) {
        try {
            output?.write((command + "\r").toByteArray(Charsets.US_ASCII))
            output?.flush()
        } catch (_: Exception) {
            disconnectAndRetry()
        }
    }

    private fun disconnectAndRetry() {
        running = false
        polling = false
        isoTp.reset()
        handler.removeCallbacks(poller)
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        output = null
        connectedPort = -1

        runOnUiThread {
            button.isEnabled = false
            status.text = "WiCAN: ODPOJENO • automatický reconnect…"
        }
        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        handler.removeCallbacks(reconnecter)
        handler.postDelayed(reconnecter, 3000)
    }

    override fun onDestroy() {
        running = false
        polling = false
        handler.removeCallbacks(poller)
        handler.removeCallbacks(reconnecter)
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        output = null
        super.onDestroy()
    }

    private inner class GraphView : View(this) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            if (history.size < 2) return

            val minValue = history.minOrNull() ?: return
            val maxValue = max(history.maxOrNull() ?: minValue, minValue + 1f)
            val w = width.toFloat()
            val h = height.toFloat()
            val step = w / max(1, history.size - 1)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f

            for (i in 1 until history.size) {
                val x1 = (i - 1) * step
                val x2 = i * step
                val y1 = h - ((history[i - 1] - minValue) / (maxValue - minValue)) * (h - 20f) - 10f
                val y2 = h - ((history[i] - minValue) / (maxValue - minValue)) * (h - 20f) - 10f
                c.drawLine(x1, y1, x2, y2, paint)
            }
        }
    }
}
