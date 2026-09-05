package com.autodiag.outlander2101

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var value: TextView
    private lateinit var graph: GraphView
    private var socket: Socket? = null
    private var output: OutputStream? = null
    private var running = false
    private val history = mutableListOf<Float>()
    private val handler = Handler(Looper.getMainLooper())

    private val poller = object : Runnable {
        override fun run() {
            if (!running) return
            send("2101")
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 20)
        }

        val host = EditText(this).apply {
            setText("192.168.0.10")
            hint = "WiCAN IP"
            singleLine = true
        }
        val port = EditText(this).apply {
            setText("35000")
            hint = "Port"
            singleLine = true
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val connect = Button(this).apply { text = "PŘIPOJIT WiCAN" }
        val b2101 = Button(this).apply { text = "2101"; isEnabled = false }

        status = TextView(this).apply { text = "Odpojeno"; textSize = 16f }
        value = TextView(this).apply {
            text = "—"
            textSize = 42f
            setPadding(0, 24, 0, 12)
        }
        graph = GraphView()

        root.addView(host)
        root.addView(port)
        root.addView(connect)
        root.addView(b2101)
        root.addView(status)
        root.addView(value)
        root.addView(graph, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        connect.setOnClickListener {
            if (running) disconnect() else connect(host.text.toString(), port.text.toString().toIntOrNull() ?: 35000, b2101)
        }
        b2101.setOnClickListener {
            if (!running) return@setOnClickListener
            handler.removeCallbacks(poller)
            send("2101")
            handler.postDelayed(poller, 1000)
        }
    }

    private fun connect(host: String, port: Int, button: Button) {
        Thread {
            try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), 4000)
                socket = s
                output = s.getOutputStream()
                running = true
                sendRaw("ATZ")
                Thread.sleep(700)
                sendRaw("ATE0")
                sendRaw("ATL0")
                sendRaw("ATS0")
                sendRaw("ATH1")
                sendRaw("ATSP6")
                sendRaw("ATAT1")
                sendRaw("ATAL")
                sendRaw("ATST32")
                sendRaw("ATSH761")
                runOnUiThread {
                    status.text = "WiCAN připojen"
                    button.isEnabled = true
                }
                readLoop()
            } catch (e: Exception) {
                running = false
                runOnUiThread { status.text = "Chyba: ${e.message ?: "připojení"}" }
            }
        }.start()
    }

    private fun readLoop() {
        val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        while (running) {
            val line = reader.readLine() ?: break
            parseLine(line)
        }
    }

    private fun parseLine(line: String) {
        val clean = line.trim().replace(" ", "").replace("\r", "")
        if (clean.isEmpty() || clean == ">" || clean.equals("OK", true)) return
        if (!clean.startsWith("762", true)) return

        val payload = clean.substring(3).replace(":", "")
        if (payload.length < 4) return
        val bytes = ArrayList<Int>()
        var i = 0
        while (i + 1 < payload.length) {
            val b = payload.substring(i, i + 2).toIntOrNull(16) ?: break
            bytes.add(b)
            i += 2
        }
        if (bytes.size <= 79) return

        // Candidate from the evidence repository: response indices 78..79, UInt16 BE, kOhm.
        val risoKOhm = (bytes[78] * 256 + bytes[79]).toFloat()
        runOnUiThread {
            history.add(risoKOhm)
            if (history.size > 120) history.removeAt(0)
            value.text = String.format(Locale.US, "%.0f kΩ", risoKOhm)
            graph.invalidate()
            status.text = "měření běží • ${history.size} vzorků"
        }
    }

    private fun send(command: String) {
        Thread { sendRaw(command) }.start()
    }

    private fun sendRaw(command: String) {
        try {
            output?.write((command + "\r").toByteArray())
            output?.flush()
        } catch (_: Exception) {
            running = false
        }
    }

    private fun disconnect() {
        running = false
        handler.removeCallbacks(poller)
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        output = null
        status.text = "Odpojeno"
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    private inner class GraphView : View(this) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(c: Canvas) {
            super.onDraw(c)
            if (history.size < 2) return
            val min = history.minOrNull() ?: return
            val maxV = max(history.maxOrNull() ?: min, min + 1f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            val w = width.toFloat()
            val h = height.toFloat()
            val step = w / max(1, history.size - 1)
            for (i in 1 until history.size) {
                val x1 = (i - 1) * step
                val x2 = i * step
                val y1 = h - ((history[i - 1] - min) / (maxV - min)) * (h - 20f) - 10f
                val y2 = h - ((history[i] - min) / (maxV - min)) * (h - 20f) - 10f
                c.drawLine(x1, y1, x2, y2, paint)
            }
        }
    }
}
