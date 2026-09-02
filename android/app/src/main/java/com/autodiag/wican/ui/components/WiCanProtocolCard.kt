package com.autodiag.wican.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.transport.TransportMode
import com.autodiag.core.transport.WiCanProtocolCatalog

@Composable
fun WiCanProtocolCard(
    onSelect: (TransportMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val recommended = WiCanProtocolCatalog.recommended
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Komunikační protokol adaptéru", style = MaterialTheme.typography.titleMedium)
                    Text("AutoDiag volí vhodnou cestu automaticky.", style = MaterialTheme.typography.bodySmall)
                }
                InfoTooltip("Toto je protokol mezi aplikací a WiCAN PRO. Není to totéž jako diagnostický protokol mezi WiCAN a vozidlem. Pro běžnou diagnostiku je doporučen ELM327. SLCAN/RAW CAN je určen pro pokročilou práci s CAN rámci. Diagnostický protokol vozidla AutoDiag zjistí až po navázání spojení.")
            }
            Spacer(Modifier.height(10.dp))
            Text("✓ Automatická volba: ${recommended.titleCs}", color = MaterialTheme.colorScheme.primary)
            Text(recommended.bestForCs, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Button(onClick = { onSelect(recommended.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Připojit doporučeným režimem")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onSelect(TransportMode.ELM327) }, modifier = Modifier.weight(1f)) { Text("ELM327") }
                OutlinedButton(onClick = { onSelect(TransportMode.SLCAN_RAW) }, modifier = Modifier.weight(1f)) { Text("SLCAN") }
            }
            Spacer(Modifier.height(8.dp))
            WiCanProtocolCatalog.modes.filter { it.id != TransportMode.SIMULATOR }.forEach { mode ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(mode.titleCs, style = MaterialTheme.typography.labelLarge)
                        Text(mode.shortDescriptionCs, style = MaterialTheme.typography.bodySmall)
                    }
                    InfoTooltip(mode.tooltipCs)
                }
                Spacer(Modifier.height(6.dp))
            }
            Text("Poznámka: automatická volba zde znamená volbu transportní cesty. Po připojení AutoDiag samostatně detekuje protokol ECU a podporované služby vozidla.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
