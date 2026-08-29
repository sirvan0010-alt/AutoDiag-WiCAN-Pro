package com.autodiag.wican

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autodiag.core.capability.Capability
import com.autodiag.core.capability.CapabilityDiscovery
import com.autodiag.core.capability.CapabilityIds
import com.autodiag.core.capability.CapabilitySnapshot
import com.autodiag.core.capability.CapabilityStatus
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.transport.TcpWiCanTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    private val _state = MutableStateFlow(ConnectionUiState())
    val state: StateFlow<ConnectionUiState> = _state.asStateFlow()
    private var transport: TcpWiCanTransport? = null
    private var session: Elm327Session? = null
    private var job: Job? = null

    fun connect(host: String, mode: ConnectMode) {
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = ConnectionUiState(ConnectionPhase.CONNECTING, host, mode)
            runCatching {
                require(host.isNotBlank()) { "Zadejte IP adresu WiCAN." }
                transport?.disconnect()
                val port = mode.port
                val t = TcpWiCanTransport()
                transport = t
                t.connect(TransportConfig(host = host, port = port, mode = mode.transportMode, autoReconnect = true)).getOrThrow()
                if (mode == ConnectMode.SLCAN) {
                    _state.update { it.copy(phase = ConnectionPhase.READY, snapshot = slcanSnapshot()) }
                } else {
                    val s = Elm327Session(t)
                    session = s
                    _state.update { it.copy(phase = ConnectionPhase.INITIALIZING) }
                    s.initialize().getOrThrow()
                    _state.update { it.copy(phase = ConnectionPhase.DISCOVERING) }
                    val snapshot = CapabilityDiscovery().run(s).getOrThrow()
                    _state.update { it.copy(phase = ConnectionPhase.READY, snapshot = snapshot) }
                }
            }.onFailure { t ->
                if (t is kotlinx.coroutines.CancellationException) throw t
                _state.update { it.copy(phase = ConnectionPhase.ERROR, error = humanize(t)) }
                runCatching { transport?.disconnect() }
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        viewModelScope.launch {
            runCatching { transport?.disconnect() }
            transport = null
            session = null
            _state.value = ConnectionUiState()
        }
    }

    fun retry() {
        val s = _state.value
        if (s.host != null && s.mode != null) connect(s.host, s.mode)
    }

    private fun slcanSnapshot() = CapabilitySnapshot(
        vehicleIdentity = null,
        capabilities = linkedMapOf(
            CapabilityIds.COMMUNICATION to Capability(CapabilityIds.COMMUNICATION, "SLCAN TCP spojení", CapabilityStatus.AVAILABLE, "TCP spojení na port 23 je aktivní."),
            CapabilityIds.OBD_PROTOCOL to Capability(CapabilityIds.OBD_PROTOCOL, "OBD protokol", CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo"),
            CapabilityIds.OBD_VIN to Capability(CapabilityIds.OBD_VIN, "VIN", CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo"),
            CapabilityIds.OBD_DTC to Capability(CapabilityIds.OBD_DTC, "Chybové kódy (DTC)", CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo"),
            CapabilityIds.OBD_LIVE_DATA to Capability(CapabilityIds.OBD_LIVE_DATA, "Živá data (OBD)", CapabilityStatus.UNAVAILABLE, "Vozidlo údaj neposkytlo")
        )
    )

    private fun humanize(t: Throwable): String {
        val m = t.message?.lowercase().orEmpty()
        return when {
            "timed out" in m || "timeout" in m -> "Vypršel časový limit. Zkontrolujte IP adresu, port a AP/Client Isolation."
            "refused" in m -> "Spojení bylo odmítnuto. Ověřte, že WiCAN naslouchá na správném portu."
            "unreachable" in m || "network" in m -> "Síť není dostupná. Telefon a WiCAN musí být ve stejné síti bez izolace klientů."
            else -> t.message ?: "Spojení selhalo."
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        viewModelScope.launch { runCatching { transport?.disconnect() } }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ConnectionViewModel() as T
    }
}

enum class ConnectMode(val label: String, val port: Int, val transportMode: TransportMode) {
    ELM327("ELM327", 3333, TransportMode.ELM327),
    SLCAN("SLCAN", 23, TransportMode.SLCAN_RAW)
}

enum class ConnectionPhase { IDLE, CONNECTING, INITIALIZING, DISCOVERING, READY, ERROR }

data class ConnectionUiState(
    val phase: ConnectionPhase = ConnectionPhase.IDLE,
    val host: String? = null,
    val mode: ConnectMode? = null,
    val snapshot: CapabilitySnapshot? = null,
    val error: String? = null
)
