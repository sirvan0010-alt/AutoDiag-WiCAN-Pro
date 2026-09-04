package com.autodiag.wican.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autodiag.core.can.RawCanMonitorState
import com.autodiag.core.can.SlcanCanFrameStream
import com.autodiag.core.capability.CapabilityDiscovery
import com.autodiag.core.capability.CapabilitySnapshot
import com.autodiag.core.capability.EcuDataIdentity
import com.autodiag.core.capability.OutlanderLiveSamplingSettings
import com.autodiag.core.capability.OutlanderPhev21LiveMeasurementRunner
import com.autodiag.core.capability.OutlanderPhevResistanceDecoder
import com.autodiag.core.capability.OutlanderResistanceHistory
import com.autodiag.core.capability.OutlanderResistanceKind
import com.autodiag.core.capability.OutlanderResistanceMeasurement
import com.autodiag.core.capability.OutlanderResistanceSample
import com.autodiag.core.capability.OutlanderResistanceSessionStats
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.SimulatorWiCanTransport
import com.autodiag.core.transport.TcpWiCanTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMetrics
import com.autodiag.core.transport.TransportMode
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConnectionPhase { IDLE, CONNECTING, INITIALIZING_ELM, DISCOVERING_CAPABILITIES, READY, ERROR;
    val labelCs: String get() = when (this) { IDLE -> "Nepřipojeno"; CONNECTING -> "Připojování…"; INITIALIZING_ELM -> "Inicializace ELM327…"; DISCOVERING_CAPABILITIES -> "Zjišťování schopností…"; READY -> "Připojeno"; ERROR -> "Chyba" }
}

data class ConnectionUiState(
    val phase: ConnectionPhase = ConnectionPhase.IDLE,
    val mode: TransportMode? = null,
    val host: String? = null,
    val port: Int? = null,
    val snapshot: CapabilitySnapshot? = null,
    val errorMessage: String? = null,
    val linkOnly: Boolean = false,
    val transportState: ConnectionState? = null,
    val transportMetrics: TransportMetrics = TransportMetrics(),
    val rawCanMonitor: RawCanMonitorState = RawCanMonitorState(),
    val outlanderIsolation: OutlanderResistanceSessionStats = OutlanderResistanceSessionStats(OutlanderResistanceKind.HV_ISOLATION_RESISTANCE),
    val outlanderInternalResistanceMax: OutlanderResistanceSessionStats = OutlanderResistanceSessionStats(OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED),
    val outlanderInternalResistanceMin: OutlanderResistanceSessionStats = OutlanderResistanceSessionStats(OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED),
    val outlanderIsolationHistory: List<OutlanderResistanceSample> = emptyList(),
    val outlanderInternalMaxHistory: List<OutlanderResistanceSample> = emptyList(),
    val outlanderInternalMinHistory: List<OutlanderResistanceSample> = emptyList(),
    val outlanderSamplingSettings: OutlanderLiveSamplingSettings = OutlanderLiveSamplingSettings(),
    val outlanderLiveMeasurementActive: Boolean = false,
    val outlanderLastMeasurementError: String? = null,
    val outlanderExpertRequest: String? = null,
    val outlanderExpertResponse: String? = null
)

class ConnectionViewModel(
    private val transportFactory: () -> WiCanTransport = { TcpWiCanTransport() },
    private val discovery: CapabilityDiscovery = CapabilityDiscovery()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()
    private val isolationHistory = OutlanderResistanceHistory()
    private val internalMaxHistory = OutlanderResistanceHistory()
    private val internalMinHistory = OutlanderResistanceHistory()
    private var transport: WiCanTransport? = null
    private var session: Elm327Session? = null
    private var job: Job? = null
    private var metricsJob: Job? = null
    private var rawCanStream: SlcanCanFrameStream? = null
    private var rawCanJob: Job? = null
    private var outlanderRunner: OutlanderPhev21LiveMeasurementRunner? = null

    fun connectElm327(host: String, port: Int = 3333) = connect(host, port, TransportMode.ELM327, true)
    fun connectSlcan(host: String, port: Int = 23) = connect(host, port, TransportMode.SLCAN_RAW, false)
    fun connectSimulator() = connect("simulator", 0, TransportMode.SIMULATOR, true)
    fun setRawCanFilter(filter: String) = _uiState.update { it.copy(rawCanMonitor = it.rawCanMonitor.copy(idFilter = filter)) }
    fun toggleRawCanPause() = _uiState.update { it.copy(rawCanMonitor = it.rawCanMonitor.copy(paused = !it.rawCanMonitor.paused)) }
    fun clearRawCan() = _uiState.update { it.copy(rawCanMonitor = it.rawCanMonitor.clear()) }
    fun setOutlanderSamplingInterval(intervalMs: Long) {
        require(intervalMs in OutlanderLiveSamplingSettings.OPTIONS_MS)
        _uiState.update { it.copy(outlanderSamplingSettings = it.outlanderSamplingSettings.copy(intervalMs = intervalMs)) }
        if (_uiState.value.outlanderLiveMeasurementActive) startOutlanderLiveMeasurement()
    }

    fun startOutlanderLiveMeasurement() {
        val activeSession = session ?: run {
            _uiState.update { it.copy(outlanderLastMeasurementError = "Aktivní měření vyžaduje připojený ELM327 transport.") }
            return
        }
        if (_uiState.value.mode != TransportMode.ELM327 && _uiState.value.mode != TransportMode.SIMULATOR) {
            _uiState.update { it.copy(outlanderLastMeasurementError = "Aktivní 21 01 měření je dostupné pouze přes ELM327 transport.") }
            return
        }
        outlanderRunner?.stop()
        val runner = OutlanderPhev21LiveMeasurementRunner(activeSession, viewModelScope) { result ->
            val acceptedAny = acceptOutlanderLiveResult(result)
            if (!acceptedAny) {
                _uiState.update { it.copy(outlanderLastMeasurementError = result.error ?: "21 01 neposkytl žádnou dekódovatelnou hodnotu.") }
            }
        }
        outlanderRunner = runner
        _uiState.update { it.copy(outlanderLiveMeasurementActive = true, outlanderLastMeasurementError = null) }
        runner.start(_uiState.value.outlanderSamplingSettings.intervalMs)
    }

    fun stopOutlanderLiveMeasurement() {
        outlanderRunner?.stop()
        outlanderRunner = null
        _uiState.update { it.copy(outlanderLiveMeasurementActive = false) }
    }

    /**
     * Accept each decoded 21 01 signal independently. A missing signal must
     * not discard other signals decoded from the same response.
     */
    private fun acceptOutlanderLiveResult(result: OutlanderPhev21LiveMeasurementRunner.Result): Boolean {
        val isolation = result.isolationResistance
        val maxMeasurement = result.internalResistanceMax
        val minMeasurement = result.internalResistanceMin

        if (isolation == null && maxMeasurement == null && minMeasurement == null) {
            return false
        }

        isolation?.let { isolationHistory.add(OutlanderResistanceSample(result.timestampEpochMs, it.value, it.verification)) }
        maxMeasurement?.let { internalMaxHistory.add(OutlanderResistanceSample(result.timestampEpochMs, it.value, it.verification)) }
        minMeasurement?.let { internalMinHistory.add(OutlanderResistanceSample(result.timestampEpochMs, it.value, it.verification)) }

        _uiState.update { state ->
            state.copy(
                outlanderIsolation = isolation?.let { state.outlanderIsolation.accept(it) } ?: state.outlanderIsolation,
                outlanderInternalResistanceMax = maxMeasurement?.let { state.outlanderInternalResistanceMax.accept(it) } ?: state.outlanderInternalResistanceMax,
                outlanderInternalResistanceMin = minMeasurement?.let { state.outlanderInternalResistanceMin.accept(it) } ?: state.outlanderInternalResistanceMin,
                outlanderIsolationHistory = isolationHistory.snapshot(),
                outlanderInternalMaxHistory = internalMaxHistory.snapshot(),
                outlanderInternalMinHistory = internalMinHistory.snapshot(),
                outlanderExpertRequest = result.rawRequest,
                outlanderExpertResponse = result.rawResponse,
                outlanderLastMeasurementError = null
            )
        }
        return true
    }

    /**
     * Feed a source-normalized Watchdog-style 21 01 response into Outlander live-data state.
     * This method is intentionally passive: it never sends a request and never invents an ECU/CAN ID.
     */
    fun ingestOutlanderBatteryResponse(
        response: IntArray,
        timestampEpochMs: Long,
        ecuIdentity: EcuDataIdentity? = null,
        rawResponse: String? = null
    ) {
        val isolation = runCatching {
            OutlanderPhevResistanceDecoder.decodeIsolationMeasurement(response, timestampEpochMs, ecuIdentity, "21 01", rawResponse)
        }.getOrNull()
        val maxMeasurement = runCatching {
            OutlanderResistanceMeasurement(
                kind = OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED,
                value = OutlanderPhevResistanceDecoder.decodeUnverifiedInternalResistanceMaximum(response),
                timestampEpochMs = timestampEpochMs,
                ecuIdentity = ecuIdentity,
                rawRequest = "21 01",
                rawResponse = rawResponse,
                verification = com.autodiag.core.capability.OutlanderMeasurementVerification.UNVERIFIED
            )
        }.getOrNull()
        val minMeasurement = runCatching {
            OutlanderResistanceMeasurement(
                kind = OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED,
                value = OutlanderPhevResistanceDecoder.decodeUnverifiedInternalResistanceMinimum(response),
                timestampEpochMs = timestampEpochMs,
                ecuIdentity = ecuIdentity,
                rawRequest = "21 01",
                rawResponse = rawResponse,
                verification = com.autodiag.core.capability.OutlanderMeasurementVerification.UNVERIFIED
            )
        }.getOrNull()

        isolation?.let { isolationHistory.add(OutlanderResistanceSample(timestampEpochMs, it.value, it.verification)) }
        maxMeasurement?.let { internalMaxHistory.add(OutlanderResistanceSample(timestampEpochMs, it.value, it.verification)) }
        minMeasurement?.let { internalMinHistory.add(OutlanderResistanceSample(timestampEpochMs, it.value, it.verification)) }

        if (isolation == null && maxMeasurement == null && minMeasurement == null) return

        _uiState.update { state ->
            state.copy(
                outlanderIsolation = isolation?.let { state.outlanderIsolation.accept(it) } ?: state.outlanderIsolation,
                outlanderInternalResistanceMax = maxMeasurement?.let { state.outlanderInternalResistanceMax.accept(it) } ?: state.outlanderInternalResistanceMax,
                outlanderInternalResistanceMin = minMeasurement?.let { state.outlanderInternalResistanceMin.accept(it) } ?: state.outlanderInternalResistanceMin,
                outlanderIsolationHistory = isolationHistory.snapshot(),
                outlanderInternalMaxHistory = internalMaxHistory.snapshot(),
                outlanderInternalMinHistory = internalMinHistory.snapshot(),
                outlanderExpertRequest = "21 01",
                outlanderExpertResponse = rawResponse
            )
        }
    }

    private fun transportFor(mode: TransportMode): WiCanTransport = when (mode) {
        TransportMode.SIMULATOR -> SimulatorWiCanTransport()
        TransportMode.ELM327, TransportMode.SLCAN_RAW -> transportFactory()
    }

    private fun observeMetrics(t: WiCanTransport) {
        metricsJob?.cancel()
        metricsJob = viewModelScope.launch { t.metrics.collect { metrics -> _uiState.update { it.copy(transportMetrics = metrics, transportState = t.state) } } }
    }

    private fun startRawCanMonitor(t: WiCanTransport) {
        rawCanStream?.stop(); rawCanJob?.cancel(); rawCanStream = SlcanCanFrameStream(t, viewModelScope)
        rawCanJob = viewModelScope.launch { rawCanStream!!.frames.collect { frame -> _uiState.update { it.copy(rawCanMonitor = it.rawCanMonitor.onFrame(frame)) } } }
    }

    private fun stopRawCanMonitor() {
        rawCanJob?.cancel(); rawCanJob = null; rawCanStream?.stop(); rawCanStream = null
    }

    private fun connect(host: String, port: Int, mode: TransportMode, runDiscovery: Boolean) {
        job?.cancel(); metricsJob?.cancel(); stopRawCanMonitor(); stopOutlanderLiveMeasurement()
        job = viewModelScope.launch {
            _uiState.value = ConnectionUiState(phase = ConnectionPhase.CONNECTING, mode = mode, host = host, port = port, linkOnly = !runDiscovery)
            runCatching {
                if (mode != TransportMode.SIMULATOR) require(host.isNotBlank()) { "IP adresa není vyplněna." }
                runCatching { session?.close() }; runCatching { transport?.disconnect() }
                val t = transportFor(mode); transport = t; observeMetrics(t)
                t.connect(TransportConfig(host = host, port = port, mode = mode, autoReconnect = mode != TransportMode.SIMULATOR)).getOrThrow()
                _uiState.update { it.copy(transportState = t.state) }
                if (!runDiscovery) {
                    startRawCanMonitor(t); _uiState.update { it.copy(phase = ConnectionPhase.READY, errorMessage = null, snapshot = null, linkOnly = true, transportState = t.state) }; return@runCatching
                }
                _uiState.update { it.copy(phase = ConnectionPhase.INITIALIZING_ELM) }
                val s = Elm327Session(t); session = s; s.initialize().getOrThrow()
                _uiState.update { it.copy(phase = ConnectionPhase.DISCOVERING_CAPABILITIES) }
                val snap = discovery.run(s)
                _uiState.update { it.copy(phase = ConnectionPhase.READY, snapshot = snap, errorMessage = null, linkOnly = false, transportState = t.state) }
            }.onFailure { err ->
                _uiState.update { it.copy(phase = ConnectionPhase.ERROR, errorMessage = humanize(err), snapshot = null, transportState = transport?.state) }
                stopOutlanderLiveMeasurement(); stopRawCanMonitor(); runCatching { session?.close() }; runCatching { transport?.disconnect() }
                session = null; transport = null; metricsJob?.cancel()
            }
        }
    }

    fun disconnect() {
        job?.cancel(); metricsJob?.cancel(); stopOutlanderLiveMeasurement(); stopRawCanMonitor()
        viewModelScope.launch {
            runCatching { session?.close() }; runCatching { transport?.disconnect() }
            session = null; transport = null; _uiState.value = ConnectionUiState()
        }
    }

    override fun onCleared() { stopOutlanderLiveMeasurement(); stopRawCanMonitor(); super.onCleared() }

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
            require(modelClass.isAssignableFrom(ConnectionViewModel::class.java)); return ConnectionViewModel() as T
        }
    }
}
