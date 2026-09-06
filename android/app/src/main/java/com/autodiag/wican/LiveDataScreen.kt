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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.obd.ObdLiveDataEngine
import com.autodiag.core.obd.ObdPidRegistry
import com.autodiag.wican.viewmodel.LiveDataViewModel

/** Real read-only Mode 01 UI. Values are never generated locally. */
@Composable
fun LiveDataScreen(
    viewModel: LiveDataViewModel,
    session: Elm327Session,
    supportedPids: Set<Int>,
    onBack: () -> Unit
) {
    val samples by viewModel.samples.collectAsState()
    val selectedPids by viewModel.selectedPids.collectAsState()
    val running by viewModel.running.collectAsState()
    val histories = remember { mutableStateMapOf<Int, List<Double>>() }

    DisposableEffect(session, supportedPids) {
        viewModel.start(session, supportedPids)
        onDispose { viewModel.stop() }
    }

    LaunchedEffect(samples) {
        samples.filter { it.state == ObdLiveDataEngine.State.LIVE && it.value != null }.forEach { sample ->
            val old = histories[sample.pid].orEmpty()
            histories[sample.pid] = (old + sample.value!!).takeLast(120)
        }
    }

    val availableDefinitions = ObdPidRegistry.definitions.values.sortedBy { it.pid }
    val liveCount = samples.count { it.state == ObdLiveDataEngine.State.LIVE && it.value != null }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("SEOBD · Live Data", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (running) "Skutečná data z vozidla · Mode 01 · ${liveCount} aktivních hodnot"
                    else "Polling zastaven · žádná syntetická data",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBack) { Text("Zpět") }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (running) viewModel.stop() else viewModel.start(session, supportedPids) }) {
                Text(if (running) "Pauza" else "Pokračovat")
            }
            OutlinedButton(onClick = { viewModel.stop() }) { Text("Zastavit") }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Výběr signálů", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableDefinitions.forEach { def ->
                        val supported = def.pid in supportedPids
                        FilterChip(
                            selected = def.pid in selectedPids,
                            enabled = supported,
                            onClick = { viewModel.setSelected(def.pid, def.pid !in selectedPids) },
                            label = { Text("${def.labelCs} (${def.unit})") }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ověřené pro tuto relaci: ${supportedPids.sorted().joinToString { "0x%02X".format(it) }.ifBlank { "žádné" }}. Neověřené PID se nečtou.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            items(samples.filter { it.pid in selectedPids }, key = { it.pid }) { sample ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(sample.labelCs, style = MaterialTheme.typography.titleMedium)
                            Text(
                                sample.value?.let { "%.2f %s".format(it, sample.unit ?: "") } ?: "—",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${sample.state.name} · PID 01%02X · raw ${sample.rawHex.ifBlank { "—" }}".format(sample.pid),
                            style = MaterialTheme.typography.labelSmall
                        )
                        sample.error?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        Sparkline(histories[sample.pid].orEmpty())
                    }
                }
            }
            if (samples.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Čekám na první odpověď ECU…", style = MaterialTheme.typography.titleMedium)
                            Text("UI nevytváří náhradní hodnoty. Zobrazí se pouze skutečná odpověď Mode 01.")
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Komunikační stav", style = MaterialTheme.typography.titleMedium)
                        Text("Zdroj: existující Elm327Session → ObdLiveDataEngine")
                        Text("Režim: read-only · bez syntetických hodnot · bez druhého polling loopu")
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkline(values: List<Double>) {
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
            drawLine(color = Color.Gray, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 3f)
        }
    }
}
