package com.autodiag.wican.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autodiag.core.obd.LiveDataPidPolicy
import com.autodiag.core.obd.ObdLiveDataEngine
import com.autodiag.core.obd.ObdPidRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Bridges the real read-only Mode 01 engine to the Android UI. */
class LiveDataViewModel : ViewModel() {
    private val _samples = MutableStateFlow<List<ObdLiveDataEngine.SensorSample>>(emptyList())
    val samples: StateFlow<List<ObdLiveDataEngine.SensorSample>> = _samples.asStateFlow()
    private val _selectedPids = MutableStateFlow<List<Int>>(emptyList())
    val selectedPids: StateFlow<List<Int>> = _selectedPids.asStateFlow()
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()
    private var pollJob: Job? = null

    fun setSelected(pid: Int, selected: Boolean) {
        _selectedPids.update { current -> when {
            selected && pid !in current && current.size < 16 -> current + pid
            !selected -> current - pid
            else -> current
        }}
    }

    /** Starts the existing engine only with PIDs positively established by discovery. */
    fun start(engine: ObdLiveDataEngine, supportedPids: Set<Int>) {
        pollJob?.cancel()
        val allowed = supportedPids.filter { ObdPidRegistry.isSupported(it) }.toSet()
        val selected = _selectedPids.value.filter { it in allowed }.ifEmpty { allowed.toList().sorted() }
        _selectedPids.value = selected
        _samples.value = emptyList()
        if (selected.isEmpty()) {
            _running.value = false
            return
        }
        pollJob = viewModelScope.launch {
            _running.value = true
            try {
                engine.stream(allowed, selected.map { LiveDataPidPolicy.plan(it) }).collect { sample ->
                    _samples.update { old -> (old.filterNot { it.pid == sample.pid } + sample).sortedBy { it.pid } }
                }
            } finally {
                _running.value = false
            }
        }
    }

    fun stop() {
        pollJob?.cancel(); pollJob = null; _running.value = false
    }

    override fun onCleared() = stop()
}
