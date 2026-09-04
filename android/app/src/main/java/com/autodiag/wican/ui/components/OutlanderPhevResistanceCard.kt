package com.autodiag.wican.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderResistanceKind
import com.autodiag.core.capability.OutlanderResistanceSessionStats
import java.util.Locale

@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internalMax: OutlanderResistanceSessionStats,
    internalMin: OutlanderResistanceSessionStats,
    onOpenGraph: (OutlanderResistanceKind) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleMedium)
            ResistanceRow(OutlanderResistanceKind.HV_ISOLATION_RESISTANCE, isolation, onOpenGraph)
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED, internalMax, onOpenGraph)
            ResistanceRow(OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED, internalMin, onOpenGraph)
            Text("Kliknutím na kteroukoli hodnotu otevřete živý graf. Vzorky se ukládají s časem a stavem ověření.", style = MaterialTheme.typography.bodySmall)
            Text("DŮLEŽITÉ: dvě hodnoty MAX/MIN pocházejí ze zdroje, který je nazývá internal resistance, ale nevíme, co fyzicky znamenají. Nejsou prezentovány jako potvrzený vnitřní odpor (ESR) trakční baterie.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            Text("Izolační odpor: 21 01 je proprietární lokální datový request, nikoli běžný OBD PID. Zdroj dekóduje bytes 78–79 jako unsigned 16-bit v kΩ. Rozsah 0–65 535 kΩ (~0–65,5 MΩ) je zachován beze změny. Přesná topologie měření ani limit výrobce nejsou potvrzeny.", style = MaterialTheme.typography.bodySmall)
            Text("Limit výrobce / servisní limit: zatím neověřený.", style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        }
    }
}

@Composable
private fun ResistanceRow(kind: OutlanderResistanceKind, stats: OutlanderResistanceSessionStats, onOpenGraph: (OutlanderResistanceKind) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onOpenGraph(kind) }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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

private fun formatMeasurement(value: Double?, unit: String?): String = value?.let { "%.3f %s".format(Locale.US, it, unit ?: "") } ?: "—"
