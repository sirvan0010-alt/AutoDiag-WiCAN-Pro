package com.autodiag.wican

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autodiag.core.capability.Capability
import com.autodiag.core.capability.CapabilityStatus
import com.autodiag.core.capability.VinAudit
import com.autodiag.core.discovery.WiCanMdnsDiscovery
import com.autodiag.core.transport.TransportMode
import com.autodiag.wican.ui.components.InfoTooltip
import com.autodiag.wican.ui.components.OutlanderPhevResistanceCard
import com.autodiag.wican.ui.components.WiCanProtocolCard
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
                if (conn.phase == ConnectionPhase.IDLE) DiscoveryScreen(discovery, connectionViewModel::connectElm327, connectionViewModel::connectSlcan, connectionViewModel::connectSimulator)
                else ConnectionResultScreen(conn, connectionViewModel::disconnect, {
                    when (conn.mode) {
                        TransportMode.SIMULATOR -> connectionViewModel.connectSimulator()
                        TransportMode.SLCAN_RAW -> conn.host?.let { connectionViewModel.connectSlcan(it, conn.port ?: 23) }
                        else -> conn.host?.let { connectionViewModel.connectElm327(it, conn.port ?: 3333) }
                    }
                }, connectionViewModel::setRawCanFilter, connectionViewModel::toggleRawCanPause, connectionViewModel::clearRawCan)
            }
        }
    }
}

@Composable
private fun DiscoveryScreen(discovery: WiCanMdnsDiscovery, onConnectElm: (String, Int) -> Unit, onConnectSlcan: (String, Int) -> Unit, onConnectSimulator: () -> Unit) {
    val devices by discovery.devices.collectAsState(); val state by discovery.state.collectAsState(); val error by discovery.error.collectAsState()
    var manualIp by remember { mutableStateOf("") }; var timedOut by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { discovery.start(preferWiCanOnly = false); onDispose { discovery.stop() } }
    LaunchedEffect(Unit) { timedOut = false; delay(10_000); if (devices.isEmpty()) timedOut = true }
    Scaffold { padding -> LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text("Připojení k WiCAN", style = MaterialTheme.typography.headlineSmall); Text("Vyhledání adaptéru v místní síti") }; InfoTooltip("AutoDiag nejprve hledá WiCAN v místní síti pomocí mDNS. Nalezený endpoint ještě neznamená podporu diagnostických funkcí.") } }
        if (devices.isNotEmpty()) {
            item { Text("Nalezená zařízení", style = MaterialTheme.typography.titleMedium) }
            items(devices, key = { "${it.hostAddress}:${it.port}:${it.serviceName}" }) { device -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(device.serviceName, style = MaterialTheme.typography.titleMedium); Text("IP: ${device.hostAddress}:${device.port}"); Text(if (device.looksLikeWiCan) "Rozpoznáno jako WiCAN" else "Síťová služba – ověření po připojení"); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { val e = device.suggestedElm327Endpoint(); val s = device.suggestedSlcanEndpoint(); Button(onClick = { onConnectElm(e.first, e.second) }) { Text("Připojit ELM327") }; OutlinedButton(onClick = { onConnectSlcan(s.first, s.second) }) { Text("SLCAN") } } } } }
        }
        item { WiCanProtocolCard(onSelect = { mode -> val device = devices.firstOrNull(); when (mode) { TransportMode.ELM327 -> if (device != null) { val e = device.suggestedElm327Endpoint(); onConnectElm(e.first, e.second) } else if (manualIp.isNotBlank()) onConnectElm(manualIp.trim(), 3333); TransportMode.SLCAN_RAW -> if (device != null) { val s = device.suggestedSlcanEndpoint(); onConnectSlcan(s.first, s.second) } else if (manualIp.isNotBlank()) onConnectSlcan(manualIp.trim(), 23); TransportMode.SIMULATOR -> onConnectSimulator() } }) }
        item { Text("Stav: ${state.toUiText()}"); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (timedOut && devices.isEmpty()) item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Zařízení nebylo nalezeno", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); Text("Zkuste ruční IP, vypnutí izolace Wi-Fi nebo režim Access Point WiCAN."); Spacer(Modifier.height(12.dp)); OutlinedTextField(manualIp, { manualIp = it }, label = { Text("IP adresa WiCAN") }, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { if (manualIp.isNotBlank()) onConnectElm(manualIp.trim(), 3333) }, enabled = manualIp.isNotBlank()) { Text("ELM327") }; OutlinedButton(onClick = { if (manualIp.isNotBlank()) onConnectSlcan(manualIp.trim(), 23) }, enabled = manualIp.isNotBlank()) { Text("SLCAN") }; OutlinedButton(onClick = { timedOut = false; discovery.start(preferWiCanOnly = false) }) { Text("Hledat znovu") } } } } }
        item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Simulátor (bez hardwaru)", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); Text("Deterministický ELM327 transport pro UI a CI testy."); Spacer(Modifier.height(12.dp)); Button(onClick = onConnectSimulator, modifier = Modifier.fillMaxWidth()) { Text("Připojit simulátor") } } } }
    } }
}

@Composable
private fun ConnectionResultScreen(state: ConnectionUiState, onDisconnect: () -> Unit, onRetry: () -> Unit, onRawCanFilter: (String) -> Unit, onRawCanPause: () -> Unit, onRawCanClear: () -> Unit) {
    Scaffold { padding -> Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Spojení", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("${state.phase.labelCs} · ${state.host ?: "—"}:${state.port ?: "—"} · ${state.mode ?: ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Transport: ${state.mode ?: "—"} · ${state.transportState ?: "—"} · RX ${state.transportMetrics.rxBytes} B · TX ${state.transportMetrics.txBytes} B", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        when (state.phase) {
            ConnectionPhase.CONNECTING, ConnectionPhase.INITIALIZING_ELM, ConnectionPhase.DISCOVERING_CAPABILITIES -> Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text(state.phase.labelCs) }
            ConnectionPhase.ERROR -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Spojení selhalo", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); Text(state.errorMessage ?: "Neznámá chyba"); Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = onRetry) { Text("Zkusit znovu") }; OutlinedButton(onClick = onDisconnect) { Text("Zpět") } } } }
            ConnectionPhase.READY -> if (state.linkOnly && state.mode == TransportMode.SLCAN_RAW) RawCanMonitorScreen(state.rawCanMonitor, onRawCanFilter, onRawCanPause, onRawCanClear, onDisconnect) else {
                if (state.mode == TransportMode.SIMULATOR) Text("SIMULÁTOR – syntetická data, ne data z vozidla", style = MaterialTheme.typography.labelLarge)
                state.snapshot?.vehicleIdentity?.vin?.let { Text("VIN vozidla", style = MaterialTheme.typography.labelMedium); Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                state.snapshot?.vinAudit?.let { VinAuditCard(it) }
                Spacer(Modifier.height(8.dp))
                OutlanderPhevResistanceCard(state.outlanderIsolation, state.outlanderInternalResistanceMax, state.outlanderInternalResistanceMin)
                Spacer(Modifier.height(8.dp))
                val caps = state.snapshot?.capabilities?.values?.toList().orEmpty()
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(caps, key = { it.id }) { CapabilityCard(it) } }
                Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = onDisconnect, Modifier.fillMaxWidth()) { Text("Odpojit") }
            }
            ConnectionPhase.IDLE -> Unit
        }
    } }
}

@Composable private fun VinAuditCard(audit: VinAudit) { if (audit.ecuRecords.isEmpty()) return; Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("Kontrola VIN v řídicích jednotkách", fontWeight = FontWeight.Medium); Spacer(Modifier.height(6.dp)); if (audit.hasMismatch) { Text("⚠ NESHODA VIN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold); Text("VIN vozidla: ${audit.referenceVin ?: "—"}"); audit.ecuRecords.forEach { record -> val mismatch = audit.referenceVin != null && record.vin != audit.referenceVin; Text("${record.displayEcu}: ${record.vin}${if (mismatch) "  ← ODLIŠNÉ VIN" else ""}", color = if (mismatch) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface) } } else Text("✓ VIN je konzistentní ve všech rozpoznaných ECU odpovědích", color = MaterialTheme.colorScheme.primary) } } }
@Composable private fun CapabilityCard(cap: Capability) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(cap.displayName, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)); Text(when (cap.status) { CapabilityStatus.AVAILABLE -> "Dostupné"; CapabilityStatus.PARTIAL -> "Částečně"; CapabilityStatus.UNAVAILABLE -> "Nedostupné"; CapabilityStatus.UNKNOWN -> "Neznámé"; CapabilityStatus.ERROR -> "Chyba" }) }; cap.detail?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }; cap.userMessage?.let { InfoTooltip(it) } } } }
private fun WiCanMdnsDiscovery.State.toUiText(): String = when (this) { WiCanMdnsDiscovery.State.IDLE -> "Připraveno"; WiCanMdnsDiscovery.State.DISCOVERING -> "Vyhledávám WiCAN…"; WiCanMdnsDiscovery.State.COMPLETE -> "Vyhledávání dokončeno"; WiCanMdnsDiscovery.State.ERROR -> "Chyba vyhledávání" }
