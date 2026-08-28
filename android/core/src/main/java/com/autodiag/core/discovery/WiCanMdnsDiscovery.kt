package com.autodiag.core.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local mDNS/NSD discovery. It finds endpoints only; it never infers vehicle capabilities. */
class WiCanMdnsDiscovery(private val context: Context) {
    companion object {
        const val SERVICE_HTTP = "_http._tcp."
        const val SERVICE_WICAN = "_wican._tcp."
    }

    data class DiscoveredDevice(
        val serviceName: String,
        val serviceType: String,
        val host: String,
        val port: Int,
        val hostAddress: String = host,
        val txtAttributes: Map<String, String> = emptyMap()
    ) {
        val looksLikeWiCan: Boolean
            get() = listOf(serviceName, serviceType, txtAttributes.toString())
                .joinToString(" ").lowercase()
                .let { "wican" in it || "meatpi" in it }

        fun suggestedElm327Endpoint(): Pair<String, Int> = hostAddress to 3333
        fun suggestedSlcanEndpoint(): Pair<String, Int> = hostAddress to 23
    }

    enum class State { IDLE, DISCOVERING, COMPLETE, ERROR }

    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null
    private val listeners = mutableListOf<NsdManager.DiscoveryListener>()
    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    private val _state = MutableStateFlow(State.IDLE)
    private val _error = MutableStateFlow<String?>(null)

    val devices: StateFlow<List<DiscoveredDevice>> = _devices.asStateFlow()
    val state: StateFlow<State> = _state.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    fun start(serviceTypes: List<String> = listOf(SERVICE_HTTP), preferWiCanOnly: Boolean = false) {
        stop()
        _devices.value = emptyList()
        _error.value = null
        _state.value = State.DISCOVERING
        multicastLock = wifi.createMulticastLock("AutoDiag-mDNS").apply {
            setReferenceCounted(false)
            acquire()
        }
        serviceTypes.distinct().forEach { discover(it, preferWiCanOnly) }
    }

    fun stop() {
        listeners.toList().forEach { runCatching { nsd.stopServiceDiscovery(it) } }
        listeners.clear()
        multicastLock?.let { if (it.isHeld) it.release() }
        multicastLock = null
        if (_state.value == State.DISCOVERING) _state.value = State.COMPLETE
    }

    private fun discover(serviceType: String, preferWiCanOnly: Boolean) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) = Unit

            override fun onServiceFound(info: NsdServiceInfo) {
                if (preferWiCanOnly && !looksLikeWiCan(info)) return
                runCatching {
                    nsd.resolveService(info, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            _error.value = "Zařízení bylo nalezeno, ale jeho síťová adresa se nepodařila zjistit."
                        }

                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            val address = resolved.host.hostAddress ?: return
                            val device = DiscoveredDevice(
                                serviceName = resolved.serviceName,
                                serviceType = resolved.serviceType,
                                host = resolved.host.hostName ?: address,
                                port = resolved.port,
                                hostAddress = address,
                                txtAttributes = resolved.attributes.mapValues { it.value.toString(Charsets.UTF_8) }
                            )
                            _devices.value = (_devices.value + device)
                                .distinctBy { "${it.hostAddress}:${it.port}" }
                        }
                    })
                }
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                _devices.value = _devices.value.filterNot { it.serviceName == info.serviceName }
            }

            override fun onDiscoveryStopped(type: String) = Unit

            override fun onStartDiscoveryFailed(type: String, errorCode: Int) {
                _error.value = "mDNS vyhledávání se nepodařilo spustit."
                _state.value = State.ERROR
                runCatching { nsd.stopServiceDiscovery(this) }
            }

            override fun onStopDiscoveryFailed(type: String, errorCode: Int) = Unit
        }
        listeners += listener
        runCatching { nsd.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener) }
            .onFailure {
                _error.value = "Vyhledávání WiCAN zařízení se nepodařilo spustit."
                _state.value = State.ERROR
            }
    }

    private fun looksLikeWiCan(info: NsdServiceInfo): Boolean =
        listOf(info.serviceName, info.serviceType, info.attributes.toString())
            .joinToString(" ").lowercase()
            .let { "wican" in it || "meatpi" in it }
}
