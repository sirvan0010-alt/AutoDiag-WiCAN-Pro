package com.autodiag.wican.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autodiag.core.capability.CapabilityDiscovery
import com.autodiag.core.capability.CapabilitySnapshot
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.SimulatorWiCanTransport
import com.autodiag.core.transport.TcpWiCanTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMode
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConnectionPhase {
    IDLE, CONNECTING, INITIALIZING_ELM, DISCOVERING_CAPABILITIES, READY, ERROR;

    val labelCs: String get() = when (this) {
        IDLE -> "Nepřipojeno"
        CONNECTING -> "Připojování…"
        INITIALIZING_ELM -> "Inicializace ELM327…"
        DISCOVERING_CAPABILITIES -> "Zjišťování schopností…"
        READY -> "Připojeno"
        ERROR -> "Chyba"
    }
}

data class ConnectionUiState(
    val phase: ConnectionPhase = ConnectionPhase.IDLE,
    val mode: TransportMode? = null,
    val host: String? = null,
    val port: Int? = null,
    val snapshot: CapabilitySnapshot? = null,
    val errorMessage: String? = null,
    val linkOnly: Boolean = false,
    val transportState: ConnectionState? = null
)

/** ELM327 performs discovery; SLCAN establishes only a raw TCP link. */
class ConnectionViewModel(
    private val transportFactory: () -> WiCanTransport = { TcpWiCanTransport() },
    private val discovery: CapabilityDiscovery = CapabilityDiscovery()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    private var transport: WiCanTransport? = null
    private var session: Elm327Session? = null
    private var job: Job? = null

    fun connectElm327(host: String, port: Int = 3333) = connect(host, port, TransportMode.ELM327, true)
    fun connectSlcan(host: String, port: Int = 23) = connect(host, port, TransportMode.SLCAN_RAW, false)
    fun connectSimulator() = connect("simulator", 0, TransportMode.SIMULATOR, true)

    private fun transportFor(mode: TransportMode): WiCanTransport = when (mode) {
        TransportMode.SIMULATOR -> SimulatorWiCanTransport()
        TransportMode.ELM327, TransportMode.SLCAN_RAW -> transportFactory()
    }

    private fun connect(host: String, port: Int, mode: TransportMode, runDiscovery: Boolean) {
        job?.cancel()
        job = viewModelScope.launch {
            _uiState.value = ConnectionUiState(
                phase = ConnectionPhase.CONNECTING,
                mode = mode,
                host = host,
                port = port,
                linkOnly = !runDiscovery
            )
            runCatching {
                if (mode != TransportMode.SIMULATOR) {
                    require(host.isNotBlank()) { "IP adresa není vyplněna." }
                }
                runCatching { session?.close() }
                runCatching { transport?.disconnect() }

                val t = transportFor(mode)
                transport = t
                t.connect(
                    TransportConfig(
                        host = host,
                        port = port,
                        mode = mode,
                        autoReconnect = mode != TransportMode.SIMULATOR
                    )
                ).getOrThrow()
                _uiState.update { it.copy(transportState = t.state) }

                if (!runDiscovery) {
                    _uiState.update {
                        it.copy(
                            phase = ConnectionPhase.READY,
                            errorMessage = null,
                            snapshot = null,
                            linkOnly = true,
                            transportState = t.state
                        )
                    }
                    return@runCatching
                }

                _uiState.update { it.copy(phase = ConnectionPhase.INITIALIZING_ELM) }
                val s = Elm327Session(t)
                session = s
                s.initialize().getOrThrow()

                _uiState.update { it.copy(phase = ConnectionPhase.DISCOVERING_CAPABILITIES) }
                val snap = discovery.run(s)
                _uiState.update {
                    it.copy(
                        phase = ConnectionPhase.READY,
                        snapshot = snap,
                        errorMessage = null,
                        linkOnly = false,
                        transportState = t.state
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        phase = ConnectionPhase.ERROR,
                        errorMessage = humanize(err),
                        snapshot = null,
                        transportState = transport?.state
                    )
                }
                runCatching { session?.close() }
                runCatching { transport?.disconnect() }
                session = null
                transport = null
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        viewModelScope.launch {
            runCatching { session?.close() }
            runCatching { transport?.disconnect() }
            session = null
            transport = null
            _uiState.value = ConnectionUiState()
        }
    }

    private fun humanize(t: Throwable): String {
        val msg = t.message?.lowercase().orEmpty()
        return when {
            msg.contains("ip adresa") -> "Zadejte IP adresu WiCAN."
            msg.contains("timeout") || msg.contains("timed out") -> "Vypršel časový limit. Zkontrolujte IP, port a AP/Client Isolation."
            msg.contains("refused") || msg.contains("connect") -> "Spojení odmítnuto. ELM327 očekává port 3333, SLCAN port 23."
            msg.contains("není připojen") -> "WiCAN není připojen. Zkuste spojení znovu."
            else -> "Spojení selhalo: ${t.message ?: t.javaClass.simpleName}"
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ConnectionViewModel::class.java))
            return ConnectionViewModel() as T
        }
    }
}
