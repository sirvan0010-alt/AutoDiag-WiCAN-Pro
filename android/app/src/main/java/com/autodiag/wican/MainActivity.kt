package com.autodiag.wican

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.CapabilityStatus
import com.autodiag.core.discovery.WiCanMdnsDiscovery
import com.autodiag.wican.ui.components.InfoTooltip
import com.autodiag.wican.ui.theme.AutoDiagTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val connectionViewModel: ConnectionViewModel by viewModels { ConnectionViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AutoDiagTheme { App(connectionViewModel) } }
    }
}

@Composable
private fun App(viewModel: ConnectionViewModel) {
    val connection by viewModel.state.collectAsState()
    val discovery = remember { WiCanMdnsDiscovery(LocalContext.current) }
    val devices by discovery.devices.collectAsState()
    val mdnsState by discovery.state.collectAsState()
    val mdnsError by discovery.error.collectAsState()
    var manualIp by remember { mutableStateOf("") }
    var timedOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        discovery.start(preferWiCanOnly = false)
        delay(10_000)
        timedOut = devices.isEmpty()
    }

    if (connection.phase == ConnectionPhase.READY || connection.phase == ConnectionPhase.INITIALIZING || connection.phase == ConnectionPhase.DISCOVERING || connection.phase == ConnectionPhase.ERROR) {
        CapabilityScreen(connection, viewModel)
        return
    }

    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Připojení k WiCAN", style = MaterialTheme.typography.headlineSmall)
                Text("Vyhledání adaptéru v místní síti")
            }
            items(devices) { device ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(device.serviceName, style = MaterialTheme.typography.titleMedium)
                        Text("IP: ${device.hostAddress}:${device.port}")
                        Text(if (device.looksLikeWiCan) "Rozpoznáno jako WiCAN" else "Síťová služba – ověření po připojení")
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.connect(device.hostAddress, ConnectMode.ELM327) }) { Text("Připojit ELM327") }
                            OutlinedButton(onClick = { viewModel.connect(device.hostAddress, ConnectMode.SLCAN) }) { Text("SLCAN") }
                        }
                    }
                }
            }
            item {
                Text("Stav: ${mdnsState.toUiText()}")
                mdnsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            item {
                OutlinedTextField(manualIp, { manualIp = it }, label = { Text("IP adresa WiCAN") }, supportingText = { Text("Např. 192.168.4.1") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.connect(manualIp.trim(), ConnectMode.ELM327) }) { Text("ELM327 :3333") }
                    OutlinedButton(onClick = { viewModel.connect(manualIp.trim(), ConnectMode.SLCAN) }) { Text("SLCAN :23") }
                }
            }
            if (timedOut && devices.isEmpty()) item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Zařízení nebylo nalezeno. Zkuste ruční IP nebo zkontrolujte AP/Client Isolation.", Modifier.weight(1f))
                        InfoTooltip("Vyhledávání", "Telefon a WiCAN musí být ve stejné síti bez izolace klientů.")
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityScreen(state: ConnectionUiState, viewModel: ConnectionViewModel) {
    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Schopnosti", style = MaterialTheme.typography.headlineSmall)
                Text(state.host?.let { "$it — ${state.mode?.label ?: ""}" } ?: "")
                when (state.phase) {
                    ConnectionPhase.CONNECTING -> Text("Připojování…")
                    ConnectionPhase.INITIALIZING -> Text("Inicializace ELM327…")
                    ConnectionPhase.DISCOVERING -> Text("Zjišťování schopností…")
                    else -> Unit
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            state.snapshot?.let { snapshot ->
                items(snapshot.capabilities.values.toList()) { cap ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cap.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(cap.status.toUiText())
                            }
                            if (cap.status == CapabilityStatus.UNAVAILABLE) Text("Vozidlo údaj neposkytlo")
                            cap.detail?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            InfoTooltip(cap.displayName, cap.detail ?: "Stav schopnosti byl zjištěn při připojení.")
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.phase == ConnectionPhase.ERROR) OutlinedButton(onClick = viewModel::retry) { Text("Zkusit znovu") }
                    OutlinedButton(onClick = viewModel::disconnect) { Text("Odpojit") }
                }
            }
        }
    }
}

private fun CapabilityStatus.toUiText() = when (this) {
    CapabilityStatus.AVAILABLE -> "Dostupné"
    CapabilityStatus.PARTIAL -> "Částečně"
    CapabilityStatus.UNAVAILABLE -> "Nedostupné"
    CapabilityStatus.UNKNOWN -> "Neznámé"
    CapabilityStatus.ERROR -> "Chyba"
}

private fun WiCanMdnsDiscovery.State.toUiText() = when (this) {
    WiCanMdnsDiscovery.State.IDLE -> "Připraveno"
    WiCanMdnsDiscovery.State.DISCOVERING -> "Vyhledávám WiCAN…"
    WiCanMdnsDiscovery.State.COMPLETE -> "Vyhledávání dokončeno"
    WiCanMdnsDiscovery.State.ERROR -> "Chyba vyhledávání"
}
