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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autodiag.core.capability.Capability
import com.autodiag.core.capability.CapabilityStatus
import com.autodiag.core.capability.VinAudit
import com.autodiag.core.discovery.WiCanMdnsDiscovery
import com.autodiag.core.transport.TransportMode
import com.autodiag.wican.ui.components.InfoTooltip
import com.autodiag.wican.ui.theme.AutoDiagTheme
import com.autodiag.wican.viewmodel.ConnectionPhase
import com.autodiag.wican.viewmodel.ConnectionUiState
import com.autodiag.wican.viewmodel.ConnectionViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val connectionViewModel: ConnectionViewModel by viewModels { ConnectionViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val discovery = WiCanMdnsDiscovery(this)
        setContent {
            AutoDiagTheme {
                val conn by connectionViewModel.uiState.collectAsState()
                if (conn.phase == ConnectionPhase.IDLE) {
                    DiscoveryScreen(
                        discovery,
                        onConnectElm = { host, port -> connectionViewModel.connectElm327(host, port) },
                        onConnectSlcan = { host, port -> connectionViewModel.connectSlcan(host, port) },
                        onConnectSimulator = { connectionViewModel.connectSimulator() }
                    )
                } else {
                    ConnectionResultScreen(
                        conn,
                        onDisconnect = { connectionViewModel.disconnect() },
                        onRetry = {
                            when (conn.mode) {
                                TransportMode.SIMULATOR -> connectionViewModel.connectSimulator()
                                TransportMode.SLCAN_RAW -> conn.host?.let { connectionViewModel.connectSlcan(it, conn.port ?: 23) }
                                else -> conn.host?.let { connectionViewModel.connectElm327(it, conn.port ?: 3333) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(
    discovery: WiCanMdnsDiscovery,
    onConnectElm: (String, Int) -> Unit,
    onConnectSlcan: (String, Int) -> Unit,
    onConnectSimulator: () -> Unit
) {
    val devices by discovery.devices.collectAsState()
    val state by discovery.state.collectAsState()
    val error by discovery.error.collectAsState()
    var manualIp by remember { mutableStateOf("") }
    var timedOut by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        discovery.start(preferWiCanOnly = false)
        onDispose { discovery.stop() }
    }
    LaunchedEffect(Unit) {
        timedOut = false
        delay(10_000)
        if (devices.isEmpty()) timedOut = true
    }

    Scaffold { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Připojení k WiCAN", style = MaterialTheme.typography.headlineSmall)
                        Text("Vyhledání adaptéru v místní síti")
                    }
                    InfoTooltip("AutoDiag nejprve hledá WiCAN v místní síti pomocí mDNS. Nalezený endpoint ještě neznamená podporu diagnostických funkcí. Schopnosti se ověří až po spojení ELM327.")
                }
            }
            if (devices.isNotEmpty()) {
                item { Text("Nalezená zařízení", style = MaterialTheme.typography.titleMedium) }
                items(devices, key = { "${it.hostAddress}:${it.port}:${it.serviceName}" }) { device ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(device.serviceName, style = MaterialTheme.typography.titleMedium)
                            Text("IP: ${device.hostAddress}:${device.port}")
                            Text(if (device.looksLikeWiCan) "Rozpoznáno jako WiCAN" else "Síťová služba – ověření po připojení")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val (elmHost, elmPort) = device.suggestedElm327Endpoint()
                                val (slHost, slPort) = device.suggestedSlcanEndpoint()
                                Button(onClick = { onConnectElm(elmHost, elmPort) }) { Text("Připojit ELM327") }
                                OutlinedButton(onClick = { onConnectSlcan(slHost, slPort) }) { Text("SLCAN") }
                            }
                        }
                    }
                }
            }
            item {
                Text("Stav: ${state.toUiText()}")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            if (timedOut && devices.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zařízení nebylo nalezeno", style = MaterialTheme.typography.titleMedium)
                                InfoTooltip("Prázdný výsledek neznamená, že WiCAN není v dosahu. Častou příčinou je AP/Client Isolation. Telefon a WiCAN musí být ve stejné síti bez izolace.")
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Zkuste ruční IP, vypnutí izolace Wi-Fi nebo režim Access Point WiCAN.")
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                manualIp,
                                { manualIp = it },
                                label = { Text("IP adresa WiCAN") },
                                supportingText = { Text("Např. 192.168.4.1") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { if (manualIp.isNotBlank()) onConnectElm(manualIp.trim(), 3333) }, enabled = manualIp.isNotBlank()) { Text("ELM327") }
                                OutlinedButton(onClick = { if (manualIp.isNotBlank()) onConnectSlcan(manualIp.trim(), 23) }, enabled = manualIp.isNotBlank()) { Text("SLCAN") }
                                OutlinedButton(onClick = { timedOut = false; discovery.start(preferWiCanOnly = false) }) { Text("Hledat znovu") }
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Simulátor (bez hardwaru)", style = MaterialTheme.typography.titleMedium)
                            InfoTooltip("In-process ELM327 bez sítě. Slouží k vývoji a testům. Odpovědi jsou syntetické a nereprezentují skutečné vozidlo.")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Deterministický ELM327 transport pro UI a CI testy.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onConnectSimulator, modifier = Modifier.fillMaxWidth()) {
                            Text("Připojit simulátor")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionResultScreen(state: ConnectionUiState, onDisconnect: () -> Unit, onRetry: () -> Unit) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Spojení", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("${state.phase.labelCs} · ${state.host ?: "—"}:${state.port ?: "—"} · ${state.mode ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Transport: ${state.mode ?: "—"} · ${state.transportState ?: "—"} · ${state.host ?: "—"}:${state.port ?: "—"}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            when (state.phase) {
                ConnectionPhase.CONNECTING, ConnectionPhase.INITIALIZING_ELM, ConnectionPhase.DISCOVERING_CAPABILITIES -> {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text(state.phase.labelCs)
                    }
                }
                ConnectionPhase.ERROR -> {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Spojení selhalo", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp)); Text(state.errorMessage ?: "Neznámá chyba")
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onRetry) { Text("Zkusit znovu") }
                                OutlinedButton(onClick = onDisconnect) { Text("Zpět") }
                            }
                        }
                    }
                }
                ConnectionPhase.READY -> {
                    if (state.linkOnly) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("SLCAN link aktivní", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("TCP spojení na portu ${state.port} je navázané. OBD/ELM schopnosti se na této cestě neprohlašují za dostupné — pouze surový link. Pro Capability Discovery použijte ELM327 (:3333).")
                                Spacer(Modifier.height(8.dp))
                                InfoTooltip("SLCAN je samostatná transportní cesta. Úspěšné TCP neznamená dostupnost Mode 01/03 ani VIN.")
                            }
                        }
                    } else {
                        val caps = state.snapshot?.capabilities?.values?.toList().orEmpty()
                        if (state.mode == TransportMode.SIMULATOR) {
                            Text("SIMULÁTOR – syntetická data, ne data z vozidla", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(8.dp))
                        }
                        state.snapshot?.vehicleIdentity?.vin?.let { vin ->
                            Text("VIN vozidla", style = MaterialTheme.typography.labelMedium)
                            Text(vin, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        state.snapshot?.vinAudit?.let { VinAuditCard(it) }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(caps, key = { it.id }) { CapabilityCard(it) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onDisconnect, Modifier.fillMaxWidth()) { Text("Odpojit") }
                }
                ConnectionPhase.IDLE -> Unit
            }
        }
    }
}

@Composable
private fun VinAuditCard(audit: VinAudit) {
    if (audit.ecuRecords.isEmpty()) return

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kontrola VIN v řídicích jednotkách", fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                InfoTooltip("AutoDiag porovnává VIN z odpovědí Mode 09 PID 02. Pokud ELM327 vrátí CAN hlavičky, zobrazí také adresu ECU. Rozdílné VIN může být důkazem výměny nebo nesouladu jednotky, ale samo o sobě neurčuje příčinu.")
            }
            Spacer(Modifier.height(6.dp))
            if (audit.hasMismatch) {
                Text("⚠ NESHODA VIN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Text("VIN vozidla: ${audit.referenceVin ?: "—"}")
                Spacer(Modifier.height(6.dp))
                audit.ecuRecords.forEach { record ->
                    val isMismatch = audit.referenceVin != null && record.vin != audit.referenceVin
                    Text(
                        "${record.displayEcu}: ${record.vin}${if (isMismatch) "  ← ODLIŠNÉ VIN" else ""}",
                        color = if (isMismatch) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pozor: rozdílné VIN v ECU je nález k prověření. Neznamená automaticky havárii ani manipulaci s vozidlem.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("✓ VIN je konzistentní ve všech rozpoznaných ECU odpovědích", color = MaterialTheme.colorScheme.primary)
                audit.ecuRecords.forEach { record -> Text("${record.displayEcu}: ${record.vin}", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun CapabilityCard(cap: Capability) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cap.displayName, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(
                    when (cap.status) {
                        CapabilityStatus.AVAILABLE -> "Dostupné"
                        CapabilityStatus.PARTIAL -> "Částečně"
                        CapabilityStatus.UNAVAILABLE -> "Nedostupné"
                        CapabilityStatus.UNKNOWN -> "Neznámé"
                        CapabilityStatus.ERROR -> "Chyba"
                    },
                    color = when (cap.status) {
                        CapabilityStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
                        CapabilityStatus.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            cap.detail?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (cap.status == CapabilityStatus.UNAVAILABLE) {
                Text("Vozidlo údaj neposkytlo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            cap.userMessage?.let { msg -> Spacer(Modifier.height(4.dp)); InfoTooltip(msg) }
        }
    }
}

private fun WiCanMdnsDiscovery.State.toUiText(): String = when (this) {
    WiCanMdnsDiscovery.State.IDLE -> "Připraveno"
    WiCanMdnsDiscovery.State.DISCOVERING -> "Vyhledávám WiCAN…"
    WiCanMdnsDiscovery.State.COMPLETE -> "Vyhledávání dokončeno"
    WiCanMdnsDiscovery.State.ERROR -> "Chyba vyhledávání"
}
