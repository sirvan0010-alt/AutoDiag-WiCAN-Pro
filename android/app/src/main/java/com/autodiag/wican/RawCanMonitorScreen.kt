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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autodiag.core.can.CanFrame
import com.autodiag.core.can.RawCanMonitorState

@Composable
fun RawCanMonitorScreen(
    state: RawCanMonitorState,
    onFilterChanged: (String) -> Unit,
    onPauseToggle: () -> Unit,
    onClear: () -> Unit,
    onDisconnect: () -> Unit
) {
    var filter by remember(state.idFilter) { mutableStateOf(state.idFilter) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Live CAN Monitor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("SLCAN / classic CAN 2.0 · ${if (state.paused) "PAUZA" else "LIVE"}")
            }
            Text("RX ${state.stats.receivedFrames}", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Frame rate: ${state.stats.frameRateHz()?.let { String.format("%.1f Hz", it) } ?: "—"}", modifier = Modifier.weight(1f))
            Text("Bytes: ${state.stats.receivedBytes}")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = filter,
            onValueChange = {
                filter = it
                onFilterChanged(it)
            },
            label = { Text("Filtr ID (HEX)") },
            placeholder = { Text("např. 123 nebo 0x123") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPauseToggle, modifier = Modifier.weight(1f)) {
                Text(if (state.paused) "Pokračovat" else "Pause")
            }
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))
        Text("Zobrazeno ${state.frames.size} / ${state.maxFrames} rámců · statistiky RX běží i při filtru/pauze", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        Card(Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(state.frames, key = { frameKey(it) }) { frame ->
                    CanFrameRow(frame)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onDisconnect, modifier = Modifier.fillMaxWidth()) { Text("Odpojit") }
    }
}

@Composable
private fun CanFrameRow(frame: CanFrame) {
    val type = when {
        frame.isRemote -> "RTR"
        frame.isExtended -> "EXT"
        else -> "STD"
    }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("%08X".format(frame.id), modifier = Modifier.weight(0.25f), fontWeight = FontWeight.Medium)
        Text(type, modifier = Modifier.weight(0.14f), style = MaterialTheme.typography.bodySmall)
        Text("${frame.dataLength}", modifier = Modifier.weight(0.10f), style = MaterialTheme.typography.bodySmall)
        Text(frame.hex(), modifier = Modifier.weight(0.51f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun frameKey(frame: CanFrame): String =
    "${frame.timestampNanos ?: 0L}-${frame.id}-${frame.isExtended}-${frame.isRemote}-${frame.data.contentHashCode()}"
