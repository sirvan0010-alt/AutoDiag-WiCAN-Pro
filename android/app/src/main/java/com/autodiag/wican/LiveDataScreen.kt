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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autodiag.core.obd.ObdLiveDataEngine
import com.autodiag.core.obd.ObdPidRegistry
import com.autodiag.wican.viewmodel.LiveDataViewModel

/** Real read-only Mode 01 live-data screen. No synthetic values are generated here. */
@Composable
fun LiveDataScreen(
    engineProvider: () -> ObdLiveDataEngine?,
    onBack: () -> Unit,
    viewModel: LiveDataViewModel = viewModel()
) {
    val samples by viewModel.samples.collectAsState()
    val selectedPids by viewModel.selectedPids.collectAsState()
    val running by viewModel.running.collectAsState()
    val allPids = remember { ObdPidRegistry.definitions.values.sortedBy { it.pid } }
    val history = remember { mutableStateMapOf<Int, MutableList<Double>>() }
    val engine = engineProvider()

    LaunchedEffect(samples) {
        samples.forEach { sample ->
            sample.value?.let { value ->
                val values = history.getOrPut(sample.pid) { mutableListOf() }
                values.add(value)
                while (values.size > 120) values.removeAt(0)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("AutoDiag · Live Data", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${selectedPids.size}/16 hodnot · ${if (running) "PŘIPOJENO" else "ZASTAVENO"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBack) { Text("Zpět") }
        }
        Spacer(Modifier.height(10.dp))

        if (engine == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Live Data není dostupná", style = MaterialTheme.typography.titleMedium)
                    Text("Vyžaduje READY ELM327 spojení.")
                }
            }
            return@Column
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (running) viewModel.stop() else viewModel.start(engine, allPids.map { it.pid }.toSet())
            }) { Text(if (running) "Pauza" else "Spustit") }
            OutlinedButton(onClick = { viewModel.stop() }, enabled = running) { Text("Zastavit") }
        }
        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Column {
                    Text("Výběr standardních Mode 01 PID", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allPids, key = { it.pid }) { definition ->
                            FilterChip(
                                selected = definition.pid in selectedPids,
                                onClick = { viewModel.setSelected(definition.pid, definition.pid !in selectedPids) },
                                label = { Text("${definition.labelCs} (${definition.unit ?: "—"})") }
                            )
                        }
                    }
                }
            }
            items(
                allPids.filter { it.pid in selectedPids },
                key = { it.pid }
            ) { definition ->
                val sample = samples.firstOrNull { it.pid == definition.pid }
                val values = history[definition.pid].orEmpty()
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(definition.labelCs, style = MaterialTheme.typography.titleMedium)
                            Text(
                                sample?.value?.let { "%.2f %s".format(it, definition.unit ?: "") } ?: "— ${definition.unit ?: ""}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                sample == null -> "WAITING · PID 01 ${"%02X".format(definition.pid)}"
                                sample.state == ObdLiveDataEngine.State.LIVE -> "LIVE · ${sample.rawHex}"
                                sample.state == ObdLiveDataEngine.State.UNAVAILABLE -> "UNAVAILABLE · ${sample.error ?: "ECU bez hodnoty"}"
                                else -> "ERROR · ${sample.error ?: "chyba komunikace"}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sample?.state == ObdLiveDataEngine.State.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Sparkline(values)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Komunikační stav", style = MaterialTheme.typography.titleMedium)
                        Text(if (running) "Polling běží přes ObdLiveDataEngine." else "Polling je zastaven.")
                        Text("Nepodporované PID jsou filtrovány engine; chyba jednoho PID neukončí stream.", style = MaterialTheme.typography.bodySmall)
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
            drawLine(Offset(x1, y1), Offset(x2, y2), strokeWidth = 3f)
        }
    }
}
