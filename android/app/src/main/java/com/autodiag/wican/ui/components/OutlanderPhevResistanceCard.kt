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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.OutlanderResistanceSessionStats

@Composable
fun OutlanderPhevResistanceCard(
    isolation: OutlanderResistanceSessionStats,
    internalResistance: OutlanderResistanceSessionStats,
    expertRawRequest: String?,
    expertRawResponse: String?,
    showExpert: Boolean
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("HV baterie – odpor a izolace", style = MaterialTheme.typography.titleMedium)
            ResistanceRow("Izolační odpor HV systému", isolation)
            Text("Vnitřní odpor baterie", style = MaterialTheme.typography.bodyMedium)
            Text(
                "NOT AVAILABLE – význam hodnoty byte 38/39 z analyzovaného zdroje není nezávisle potvrzen. Zdroj ji popisuje jako internal resistance v MΩ, což není dostatečný důkaz, že jde o ESR trakční baterie.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Co se měří: diagnostický ukazatel elektrické izolace HV systému vůči referenci vozidla. Přesná topologie měření (např. HV+ / HV− vůči chassis) není z tohoto zdroje potvrzena.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "21 01: proprietární lokální datový request. U analyzovaného Mitsubishi/PHEV Watchdog vzoru odpověď obsahuje dlouhý datový blok; izolace je v tomto zdroji dekódována jako unsigned 16-bit v kΩ. Rozsah raw 0–65 535 kΩ (~0–65,5 MΩ). Rozsah sám o sobě nepotvrzuje fyzický význam ani servisní limit.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Aktuální hodnota je source-derived a pouze částečně ověřená. Pro VERIFIED je potřeba skutečný request/response capture a nezávislé potvrzení dekódování a významu.",
                style = MaterialTheme.typography.bodySmall
            )
            if (showExpert) {
                Text("Expert", style = MaterialTheme.typography.labelLarge)
                Text("REQ: ${expertRawRequest ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("RESP: ${expertRawResponse ?: "—"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ResistanceRow(label: String, stats: OutlanderResistanceSessionStats) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label)
            Text(stats.verification.name, style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatMeasurement(stats.current, stats.unit), style = MaterialTheme.typography.titleMedium)
            Text("MIN ${formatMeasurement(stats.minimum, stats.unit)}", style = MaterialTheme.typography.labelSmall)
            Text("MAX ${formatMeasurement(stats.maximum, stats.unit)}", style = MaterialTheme.typography.labelSmall)
            Text("${stats.sampleCount} vzorků", style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatMeasurement(value: Double?, unit: String): String =
    value?.let { "%.3f %s".format(java.util.Locale.US, it, unit) } ?: "—"
