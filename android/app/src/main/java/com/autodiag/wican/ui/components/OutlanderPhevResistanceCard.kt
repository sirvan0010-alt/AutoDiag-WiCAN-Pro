package com.autodiag.wican.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderResistanceSessionStats

@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats?,
    internalResistance: OutlanderResistanceSessionStats?
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleMedium)
            ResistanceRow("Izolační odpor HV systému", isolation)
            ResistanceRow("Vnitřní odpor baterie", internalResistance)
            Text(
                "Izolační odpor a vnitřní odpor baterie jsou dvě různé fyzikální veličiny a mají rozdílné jednotky.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Izolační odpor: zdroj PHEV Watchdog označuje pole jako ISOLATION_RESISTANCE v datovém bloku 21 01. Význam je zde omezen na elektrickou izolaci HV systému vůči referenci vozidla; přesná topologie měření není z tohoto zdroje potvrzena.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Vnitřní odpor baterie z tohoto zdroje není v aplikaci považován za potvrzenou měřenou hodnotu. Dekódování v MΩ je fyzikálně sporné a čeká na nezávislé ověření.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Tooltip: 21 01 je proprietární lokální datový request používaný v některých Mitsubishi diagnostických datech. Odpověď může být dlouhý ISO-TP blok; zde je izolace dekódována jako unsigned 16-bit v kΩ. Rozsah je 0–65 535 kΩ (~0–65,5 MΩ). Samotný rozsah ani fyzikální plausibilita nepotvrzují konkrétní topologii měření.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ResistanceRow(label: String, stats: OutlanderResistanceSessionStats?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(label)
            Text(stats?.verification?.name ?: "UNVERIFIED", style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(stats?.current?.let { formatMeasurement(it, stats.unit) } ?: "—")
            Text(
                stats?.let { "MIN ${formatMeasurement(it.minimum, it.unit)} · MAX ${formatMeasurement(it.maximum, it.unit)}" } ?: "bez měření",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun formatMeasurement(value: Double?, unit: String): String =
    value?.let { "%.3f %s".format(java.util.Locale.US, it, unit) } ?: "—"
