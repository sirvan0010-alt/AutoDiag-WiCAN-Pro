package com.autodiag.wican

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autodiag.core.discovery.WiCanMdnsDiscovery
import com.autodiag.wican.ui.components.InfoTooltip
import com.autodiag.wican.ui.theme.AutoDiagTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AutoDiagTheme { DiscoveryScreen(WiCanMdnsDiscovery(this)) } }
    }
}

@Composable
private fun DiscoveryScreen(discovery: WiCanMdnsDiscovery) {
    val devices by discovery.devices.collectAsState()
    val state by discovery.state.collectAsState()
    val error by discovery.error.collectAsState()
    var manualIp by remember { mutableStateOf("") }
    var timedOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        timedOut = false
        discovery.start(preferWiCanOnly = false)
        delay(10_000)
        if (devices.isEmpty()) timedOut = true
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Připojení k WiCAN", style = MaterialTheme.typography.headlineSmall)
                        Text("Vyhledání adaptéru v místní síti")
                    }
                    InfoTooltip(
                        "AutoDiag nejprve hledá WiCAN v místní síti pomocí mDNS. " +
                            "Nalezený endpoint ještě neznamená, že vozidlo nebo adaptér podporuje konkrétní diagnostickou funkci. " +
                            "Schopnosti se ověří až po navázání spojení."
                    )
                }
            }

            if (devices.isNotEmpty()) {
                item { Text("Nalezená zařízení", style = MaterialTheme.typography.titleMedium) }
                items(devices) { device ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(device.serviceName, style = MaterialTheme.typography.titleMedium)
                            Text("IP: ${device.hostAddress}:${device.port}")
                            Text(if (device.looksLikeWiCan) "Rozpoznáno jako WiCAN" else "Síťová služba – ověření bude provedeno po připojení")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { /* connection layer follows */ }) { Text("Připojit ELM327") }
                                OutlinedButton(onClick = { /* SLCAN follows */ }) { Text("SLCAN") }
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
                                InfoTooltip(
                                    "Prázdný výsledek neznamená, že WiCAN není v dosahu. " +
                                        "Častou příčinou je AP/Client Isolation, hostující Wi-Fi nebo blokování multicastu. " +
                                        "Telefon a WiCAN musí být ve stejné síti bez izolace klientů."
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Zkuste ruční IP, vypnutí izolace Wi-Fi nebo přímé připojení telefonu k WiCAN v režimu Access Point.")
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = manualIp,
                                onValueChange = { manualIp = it },
                                label = { Text("IP adresa WiCAN") },
                                supportingText = { Text("Např. 192.168.4.1") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { /* manual endpoint follows */ }) { Text("Použít IP") }
                                OutlinedButton(onClick = {
                                    timedOut = false
                                    discovery.start(preferWiCanOnly = false)
                                }) { Text("Hledat znovu") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun WiCanMdnsDiscovery.State.toUiText(): String = when (this) {
    WiCanMdnsDiscovery.State.IDLE -> "Připraveno"
    WiCanMdnsDiscovery.State.DISCOVERING -> "Vyhledávám WiCAN…"
    WiCanMdnsDiscovery.State.COMPLETE -> "Vyhledávání dokončeno"
    WiCanMdnsDiscovery.State.ERROR -> "Chyba vyhledávání"
}
