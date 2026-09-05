package com.autodiag.wican

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.autodiag.core.obd.LiveDataPoint
import com.autodiag.core.obd.MeasurementAvailability
import com.autodiag.core.obd.MeasurementOrigin
import com.autodiag.core.obd.VerificationState
import kotlinx.coroutines.delay
import kotlin.math.sin

private data class LiveCardModel(
    val id: String,
    val label: String,
    val unit: String,
    val base: Double,
    val amplitude: Double
)

/** Phase-5 UI shell. Real transport samples can be injected without changing the visual layer. */
@Composable
fun LiveDataScreen(onBack: () -> Unit) {
    val definitions = remember {
        listOf(
            LiveCardModel("rpm", "Otáčky", "rpm", 1800.0, 180.0),
            LiveCardModel("speed", "Rychlost", "km/h", 54.0, 4.0),
            LiveCardModel("coolant", "Chladicí kapalina", "°C", 88.0, 1.5),
            LiveCardModel("load", "Zatížení motoru", "%", 31.0, 6.0),
            LiveCardModel("map", "MAP", "kPa", 101.0, 5.0),
            LiveCardModel("iat", "Teplota sání", "°C", 31.0, 1.0),
            LiveCardModel("throttle", "Škrticí klapka", "%", 18.0, 3.0),
            LiveCardModel("voltage", "Napětí řídicí jednotky", "V", 14.1, 0.15)
        )
    }
    var selected by remember { mutableStateOf(definitions.take(4).map { it.id }.toSet()) }
    var paused by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    val history = remember { mutableStateListOf<List<Double>>() }

    LaunchedEffect(paused) {
        while (!paused) {
            delay(200)
            tick++
            val values = definitions.filter { it.id in selected }.map { it.base + it.amplitude * sin(tick / 8.0 + it.id.hashCode() % 7) }
            history.add(values)
            while (history.size > 120) history.removeAt(0)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("SEOBD · Live Data", style = MaterialTheme.typography.headlineSmall)
                Text("${selected.size}/16 hodnot · adaptivní vzorkování", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onBack) { Text("Zpět") }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { paused = !paused }) { Text(if (paused) "Pokračovat" else "Pauza") }
            OutlinedButton(onClick = { selected = emptySet() }) { Text("Zrušit výběr") }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Výběr signálů", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                definitions.forEach { def ->
                    FilterChip(
                        selected = def.id in selected,
                        onClick = { if (def.id in selected || selected.size < 16) selected = if (def.id in selected) selected - def.id else selected + def.id },
                        label = { Text("${def.label} (${def.unit})") },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
            items(definitions.filter { it.id in selected }, key = { it.id }) { def ->
                val current = def.base + def.amplitude * sin(tick / 8.0 + def.id.hashCode() % 7)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(def.label, style = MaterialTheme.typography.titleMedium)
                            Text("%.1f %s".format(current, def.unit), style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("MEASURED · ${if (paused) "PAUSED" else "LIVE"} · UNVERIFIED", style = MaterialTheme.typography.labelSmall)
                        Sparkline(history, definitions.filter { it.id in selected }.indexOf(def))
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Komunikační kvalita", style = MaterialTheme.typography.titleMedium)
                        Text("Cíl 10 Hz · UI vzorkování 5 Hz · latence: — · timeout: 0 %")
                        Text("Simulovaný náhled: hodnoty nejsou prezentovány jako měření vozidla.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(history: List<List<Double>>, index: Int) {
    val values = history.mapNotNull { it.getOrNull(index) }
    Canvas(Modifier.fillMaxWidth().height(64.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it > 0.000001 } ?: 1.0
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        for (i in 1 until values.size) {
            val x1 = (i - 1) * step
            val x2 = i * step
            val y1 = size.height - ((values[i - 1] - min) / range * size.height).toFloat()
            val y2 = size.height - ((values[i] - min) / range * size.height).toFloat()
            drawLine(Offset(x1, y1), Offset(x2, y2), strokeWidth = 3f)
        }
    }
}
