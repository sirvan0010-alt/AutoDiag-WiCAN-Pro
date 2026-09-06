package com.autodiag.wican.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.obd.LiveDataPidPolicy
import com.autodiag.core.obd.ObdLiveDataEngine
import com.autodiag.core.obd.ObdPidRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Bridges the single READY ELM session to the real read-only Mode 01 engine. */
class LiveDataViewModel : ViewModel() {
    private val _samples = MutableStateFlow<List<ObdLiveDataEngine.SensorSample>>(emptyList())
    val samples: StateFlow<List<ObdLiveDataEngine.SensorSample>> = _samples.asStateFlow()
    private val _selectedPids = MutableStateFlow(listOf(0x0C))
    val selectedPids: StateFlow<List<Int>> = _selectedPids.asStateFlow()
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()
    private val _supportedPids = MutableStateFlow<Set<Int>>(emptySet())
    val supportedPids: StateFlow<Set<Int>> = _supportedPids.asStateFlow()
    private var pollJob: Job? = null

    fun setSelected(pid: Int, selected: Boolean) {
        if (!ObdPidRegistry.isSupported(pid)) return
        _selectedPids.update { current -> when {
            selected && pid !in current && current.size < 16 -> current + pid
            !selected -> current - pid
            else -> current
        }}
    }

    /** Starts polling on the already initialized session; never creates another transport/session. */
    fun start(session: Elm327Session, supportedPids: Set<Int>) {
        stop()
        val allowed = supportedPids.filter(ObdPidRegistry::isSupported).toSet()
        _supportedPids.value = allowed
        pollJob = viewModelScope.launch {
            _running.value = true
            val engine = ObdLiveDataEngine(session)
            engine.stream(
                supportedPids = allowed,
                plans = _selectedPids.value.map { LiveDataPidPolicy.plan(it) }
            ).collect { sample ->
                _samples.update { old -> (old.filterNot { it.pid == sample.pid } + sample).sortedBy { it.pid } }
            }
            _running.value = false
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
        _running.value = false
    }

    override fun onCleared() = stop()
}
