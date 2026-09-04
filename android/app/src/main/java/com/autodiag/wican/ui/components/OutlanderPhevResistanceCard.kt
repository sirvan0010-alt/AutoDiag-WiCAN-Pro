package com.autodiag.wican.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderLiveSamplingSettings
import com.autodiag.core.capability.OutlanderResistanceKind
import com.autodiag.core.capability.OutlanderResistanceSample
import com.autodiag.core.capability.OutlanderResistanceSessionStats
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.max

@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internalMax: OutlanderResistanceSessionStats,
    internalMin: OutlanderResistanceSessionStats,
    onOpenGraph: (OutlanderResistanceKind) -> Unit = {}
) {
    var graphKind by remember { mutableStateOf<OutlanderResistanceKind?>(null) }
    var samplingMs by remember { mutableLongStateOf(1_000L) }
    var localSamples by remember { mutableStateOf<Map<OutlanderResistanceKind, List<OutlanderResistanceSample>>>(emptyMap()) }
    var lastRecordedMs by remember { mutableLongStateOf(0L) }

    val stats = mapOf(
        OutlanderResistanceKind.HV_ISOLATION_RESISTANCE to isolation,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED to internalMax,
        OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED to internalMin
    )

    LaunchedEffect(isolation.current, internalMax.current, internalMin.current, samplingMs) {
        val now = System.currentTimeMillis()
        if (now - lastRecordedMs >= samplingMs) {
            val updated = localSamples.toMutableMap()
            stats.forEach { (kind, value) ->
                value.current?.let { current ->
                    val list = (updated[kind].orEmpty() + OutlanderResistanceSample(now, current, value.verification)).takeLast(2_000)
                    updated[kind] = list
                }
            }
            localSamples = updated
            lastRecordedMs = now
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleMedium)
            ResistanceRow(OutlanderResistanceKind.HV_ISOLATION_RESISTANCE, isolation) { graphKind = it; onOpenGraph(it) }
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED, internalMax) { graphKind = it; onOpenGraph(it) }
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED, internalMin) { graphKind = it; onOpenGraph(it) }
            Text("Kliknutím na hodnotu otevřete živý graf. Graf lze zobrazit jako velkou plochu a průběh se během měření průběžně doplňuje.", style = MaterialTheme.typography.bodySmall)
            Text("Vzorkování grafu: ${samplingMs} ms", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlanderLiveSamplingSettings.OPTIONS_MS.forEach { option ->
                    OutlinedButton(onClick = { samplingMs = option }) { Text("${option} ms") }
                }
            }
            Text("Poznámka: toto nastavení určuje minimální interval ukládání vzorků do grafu. Nezvyšuje samo o sobě rychlost ECU; skutečná frekvence je omezena diagnostickým request/response cyklem.", style = MaterialTheme.typography.bodySmall)
            Text("DŮLEŽITÉ: dvě hodnoty MAX/MIN pocházejí ze zdroje, který je nazývá internal resistance, ale nevíme, co fyzicky znamenají. Nejsou prezentovány jako potvrzený vnitřní odpor (ESR) trakční baterie.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Text("Izolační odpor: 21 01 je proprietární lokální datový request, nikoli běžný OBD PID. Zdroj dekóduje bytes 78–79 jako unsigned 16-bit v kΩ. Rozsah 0–65 535 kΩ (~0–65,5 MΩ) je zachován. Přesná topologie měření ani limit výrobce nejsou potvrzeny.", style = MaterialTheme.typography.bodySmall)
            Text("Limit výrobce / servisní limit: zatím neověřený.", style = MaterialTheme.typography.bodySmall)
        }
    }

    graphKind?.let { kind ->
        val graphSamples = localSamples[kind].orEmpty()
        AlertDialog(
            onDismissRequest = { graphKind = null },
            confirmButton = { OutlinedButton(onClick = { graphKind = null }) { Text("Zavřít") } },
            title = { Text(kind.displayNameCs) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Živý průběh · ${graphSamples.size} vzorků", style = MaterialTheme.typography.labelMedium)
                    ResistanceLiveGraph(graphSamples, kind.unit)
                    Text(kind.meaningStatusCs, style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }
}

@Composable
private fun ResistanceRow(kind: OutlanderResistanceKind, stats: OutlanderResistanceSessionStats, onClick: (OutlanderResistanceKind) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick(kind) }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(kind.displayNameCs)
            Text(kind.meaningStatusCs, style = MaterialTheme.typography.labelSmall)
            Text("Stav: ${stats.verification.name}", style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatMeasurement(stats.current, kind.unit), style = MaterialTheme.typography.titleMedium)
            Text("MIN ${formatMeasurement(stats.minimum, kind.unit)} · MAX ${formatMeasurement(stats.maximum, kind.unit)}", style = MaterialTheme.typography.labelSmall)
            Text("Graf ›", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ResistanceLiveGraph(samples: List<OutlanderResistanceSample>, unit: String?) {
    if (samples.size < 2) {
        Text("Čekám na další živé vzorky…", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(120.dp))
        return
    }
    val values = samples.map { it.value }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val span = max(max - min, 1e-9)
    Canvas(Modifier.fillMaxWidth().height(260.dp)) {
        val path = Path()
        samples.forEachIndexed { index, sample ->
            val x = if (samples.size == 1) 0f else size.width * index / (samples.size - 1).toFloat()
            val y = size.height - ((sample.value - min) / span * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path)
    }
    Text("MIN ${formatMeasurement(min, unit)} · MAX ${formatMeasurement(max, unit)} · poslední ${formatMeasurement(values.last(), unit)}", style = MaterialTheme.typography.labelSmall)
}

private fun formatMeasurement(value: Double?, unit: String?): String = value?.let { "%.3f %s".format(Locale.US, it, unit ?: "") } ?: "—"
