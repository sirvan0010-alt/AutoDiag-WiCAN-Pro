package com.autodiag.wican

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.obd.ObdLiveDataEngine
import com.autodiag.core.obd.ObdPidRegistry
import com.autodiag.wican.viewmodel.LiveDataViewModel

/** Real read-only Mode 01 live-data surface. Values come from ObdLiveDataEngine, never from UI generation. */
@Composable
fun LiveDataScreen(
    viewModel: LiveDataViewModel,
    engine: ObdLiveDataEngine?,
    supportedPids: Set<Int>,
    onBack: () -> Unit
) {
    val samples by viewModel.samples.collectAsState()
    val running by viewModel.running.collectAsState()

    LaunchedEffect(engine, supportedPids) {
        if (engine != null) viewModel.start(engine, supportedPids)
        else viewModel.stop()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("SEOBD · Live Data", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (supportedPids.isEmpty()) "Žádný podporovaný PID nebyl zjištěn"
                    else "${supportedPids.size} zjištěný PID · skutečná odpověď ELM327",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBack) { Text("Zpět") }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (engine != null) viewModel.start(engine, supportedPids) }, enabled = engine != null && supportedPids.isNotEmpty()) {
                Text("Obnovit polling")
            }
            OutlinedButton(onClick = viewModel::stop, enabled = running) { Text("Pauza") }
        }
        Spacer(Modifier.height(10.dp))
        if (samples.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(if (supportedPids.isEmpty()) "Live Data není k dispozici." else "Čekám na první odpověď ECU…")
                    Text("Stav: ${if (running) "POLLING" else "STOPPED"}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(samples, key = { it.pid }) { sample ->
                LiveSampleCard(sample)
            }
        }
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Evidence živých dat", style = MaterialTheme.typography.titleMedium)
                Text("PIDy jsou omezeny na hodnoty pozitivně potvrzené discovery a přítomné v dekodérovém registru.")
                Text("UI nevytváří syntetické hodnoty a nezvyšuje stav ověření nad dostupnou evidenci.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LiveSampleCard(sample: ObdLiveDataEngine.SensorSample) {
    val definition = ObdPidRegistry.get(sample.pid)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(definition?.labelCs ?: sample.labelCs, style = MaterialTheme.typography.titleMedium)
                Text(
                    sample.value?.let { "%.1f ${sample.unit.orEmpty()}" } ?: "—",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("PID 0x%02X · ${sample.state}".format(sample.pid), style = MaterialTheme.typography.labelMedium)
            if (sample.rawHex.isNotBlank()) Text("RAW: ${sample.rawHex}", style = MaterialTheme.typography.bodySmall)
            sample.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Text(
                if (sample.state == ObdLiveDataEngine.State.LIVE) "LIVE · ECU response"
                else "${sample.state} · not a current vehicle measurement",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
