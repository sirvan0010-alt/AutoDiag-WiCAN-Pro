package com.autodiag.wican.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderResistanceKind
import com.autodiag.core.capability.OutlanderResistanceSessionStats

@Composable
fun OutlanderResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internal: OutlanderResistanceSessionStats,
    modifier: Modifier = Modifier,
    expertRawRequest: String? = null,
    expertRawResponse: String? = null,
    showExpert: Boolean = false
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Izolační odpor popisuje elektrickou izolaci vysokonapěťového systému vůči referenci vozidla. Přesná topologie měření není z tohoto zdroje potvrzena.",
                style = MaterialTheme.typography.bodySmall
            )

            ResistanceRow("Izolační odpor", isolation, OutlanderResistanceKind.HV_ISOLATION_RESISTANCE)
            ResistanceRow("Interní odpor baterie", internal, OutlanderResistanceKind.INTERNAL_BATTERY_RESISTANCE)

            Spacer(Modifier.height(4.dp))
            Text("Session", style = MaterialTheme.typography.titleMedium)
            Text("Izolace MIN ${formatValue(isolation.minimum)} ${isolation.unit} · MAX ${formatValue(isolation.maximum)} ${isolation.unit}")
            Text("Interní odpor MIN ${formatValue(internal.minimum)} ${internal.unit} · MAX ${formatValue(internal.maximum)} ${internal.unit}")
            Text("Vzorky: izolace ${isolation.sampleCount} · interní odpor ${internal.sampleCount}", style = MaterialTheme.typography.bodySmall)

            if (showExpert) {
                Spacer(Modifier.height(4.dp))
                Text("Expert / raw", style = MaterialTheme.typography.titleMedium)
                expertRawRequest?.let { Text("REQ: $it", style = MaterialTheme.typography.bodySmall) }
                expertRawResponse?.let { Text("RESP: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun ResistanceRow(
    label: String,
    stats: OutlanderResistanceSessionStats,
    kind: OutlanderResistanceKind
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text("${formatValue(stats.current)} ${kind.unit}", style = MaterialTheme.typography.headlineSmall)
        }
        Column {
            Text("${stats.verification}", style = MaterialTheme.typography.labelSmall)
            stats.lastTimestampEpochMs?.let { Text("t=$it", style = MaterialTheme.typography.labelSmall) }
        }
    }
}

private fun formatValue(value: Double?): String = value?.let {
    if (it == kotlin.math.floor(it)) "%.0f".format(it) else "%.3f".format(it).trimEnd('0').trimEnd('.')
} ?: "—"
