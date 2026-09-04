package com.autodiag.wican.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderLiveSamplingSettings
import com.autodiag.core.capability.OutlanderResistanceKind
import com.autodiag.core.capability.OutlanderResistanceSample
import com.autodiag.core.capability.OutlanderResistanceSessionStats
import java.util.Locale
import kotlin.math.max

/** Backward-compatible entry point used by older callers. */
@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internalResistance: OutlanderResistanceSessionStats,
    expertRawRequest: String?,
    expertRawResponse: String?,
    showExpert: Boolean
) {
    val maxStats = if (internalResistance.kind == OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED) internalResistance else OutlanderResistanceSessionStats(OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED)
    val minStats = if (internalResistance.kind == OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED) internalResistance else OutlanderResistanceSessionStats(OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED)
    OutlanderPhevResistanceCard(isolation, maxStats, minStats, emptyList(), emptyList(), emptyList(), false, {}, {}, {})
}

@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internalMax: OutlanderResistanceSessionStats,
    internalMin: OutlanderResistanceSessionStats,
    isolationHistory: List<OutlanderResistanceSample>,
    internalMaxHistory: List<OutlanderResistanceSample>,
    internalMinHistory: List<OutlanderResistanceSample>,
    measurementActive: Boolean,
    onStartMeasurement: () -> Unit,
    onStopMeasurement: () -> Unit,
    onSamplingInterval: (Long) -> Unit,
    expertRawRequest: String? = null,
    expertRawResponse: String? = null
) {
    var graphKind by remember { mutableStateOf<OutlanderResistanceKind?>(null) }
    val stats = mapOf(
        OutlanderResistanceKind.HV_ISOLATION_RESISTANCE to isolation,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED to internalMax,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED to internalMin
    )
    val histories = mapOf(
        OutlanderResistanceKind.HV_ISOLATION_RESISTANCE to isolationHistory,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED to internalMaxHistory,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED to internalMinHistory
    )

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleMedium)
            Text(if (measurementActive) "● AKTIVNÍ MĚŘENÍ 21 01" else "○ Aktivní měření je zastaveno", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStartMeasurement, enabled = !measurementActive) { Text("Spustit měření") }
                OutlinedButton(onClick = onStopMeasurement, enabled = measurementActive) { Text("Zastavit") }
            }
            ResistanceRow(OutlanderResistanceKind.HV_ISOLATION_RESISTANCE, isolation) { graphKind = it }
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED, internalMax) { graphKind = it }
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED, internalMin) { graphKind = it }
            Text("Vzorkování ECU requestu: ${stats.values.first().lastTimestamp?.let { "aktivní" } ?: "čeká"}", style = MaterialTheme.typography.labelSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlanderLiveSamplingSettings.OPTIONS_MS.forEach { option ->
                    OutlinedButton(onClick = { onSamplingInterval(option) }) { Text("${option} ms") }
                }
            }
            Text("Interval nyní řídí skutečný 21 01 request/response cyklus. UI už nevyrábí vlastní vzorky.", style = MaterialTheme.typography.bodySmall)
            Text("MAX/MIN pochází ze zdroje, který je nazývá internal resistance, ale nevíme, co fyzicky znamenají. Nejsou prezentovány jako potvrzený vnitřní odpor (ESR) trakční baterie.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Text("Izolační odpor: zdroj dekóduje bytes 78–79 jako unsigned 16-bit v kΩ. Přesná topologie měření ani limit výrobce nejsou potvrzeny.", style = MaterialTheme.typography.bodySmall)
            expertRawResponse?.takeIf { it.isNotBlank() }?.let {
                Text("Poslední raw response: ${it.take(180)}${if (it.length > 180) "…" else ""}", style = MaterialTheme.typography.labelSmall)
            }
            expertRawRequest?.let { Text("Request: $it", style = MaterialTheme.typography.labelSmall) }
        }
    }

    graphKind?.let { kind ->
        val samples = histories[kind].orEmpty()
        AlertDialog(
            onDismissRequest = { graphKind = null },
            confirmButton = { OutlinedButton(onClick = { graphKind = null }) { Text("Zavřít") } },
            title = { Text(kind.displayNameCs) },
            text = {
                Column(Modifier.fillMaxWidth().fillMaxHeight(0.75f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Živý průběh · ${samples.size} vzorků", style = MaterialTheme.typography.labelMedium)
                    ResistanceLiveGraph(samples, kind.unit, Modifier.weight(1f).fillMaxWidth())
                    Text(kind.meaningStatusCs, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }
}

@Composable
private fun ResistanceRow(kind: OutlanderResistanceKind, stats: OutlanderResistanceSessionStats, onClick: (OutlanderResistanceKind) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick(kind) }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(kind.displayNameCs); Text(kind.meaningStatusCs, style = MaterialTheme.typography.labelSmall); Text("Stav: ${stats.verification.name}", style = MaterialTheme.typography.labelSmall) }
        Column(horizontalAlignment = Alignment.End) { Text(formatMeasurement(stats.current, kind.unit), style = MaterialTheme.typography.titleMedium); Text("MIN ${formatMeasurement(stats.minimum, kind.unit)} · MAX ${formatMeasurement(stats.maximum, kind.unit)}", style = MaterialTheme.typography.labelSmall); Text("Graf ›", style = MaterialTheme.typography.labelSmall) }
    }
}

@Composable
private fun ResistanceLiveGraph(samples: List<OutlanderResistanceSample>, unit: String?, modifier: Modifier) {
    if (samples.size < 2) { Text("Čekám na další živé vzorky…", style = MaterialTheme.typography.bodySmall); Spacer(Modifier.height(120.dp)); return }
    val values = samples.map { it.value }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = max(max - min, 1e-9)
    Canvas(modifier) {
        val path = Path()
        samples.forEachIndexed { index, sample ->
            val x = size.width * index / (samples.size - 1).toFloat()
            val y = size.height - ((sample.value - min) / span * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path)
    }
    Text("MIN ${formatMeasurement(min, unit)} · MAX ${formatMeasurement(max, unit)} · poslední ${formatMeasurement(values.last(), unit)}", style = MaterialTheme.typography.labelSmall)
}

private fun formatMeasurement(value: Double?, unit: String?): String = value?.let { "%.3f %s".format(Locale.US, it, unit ?: "") } ?: "—"
