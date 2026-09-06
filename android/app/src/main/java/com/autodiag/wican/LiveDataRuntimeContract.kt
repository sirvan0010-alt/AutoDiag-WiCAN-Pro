package com.autodiag.wican

/**
 * Runtime wiring contract for the real Mode 01 live-data path.
 *
 * ConnectionViewModel owns the single Elm327Session created during READY.
 * LiveDataViewModel owns polling state. ObdLiveDataEngine remains the only
 * Mode 01 polling/decoding engine. This contract deliberately contains no
 * transport creation, second polling loop, synthetic values, or vehicle
 * specific proprietary decoder.
 */
data class LiveDataRuntimeContract(
    val sessionOwner: String = "ConnectionViewModel",
    val engineOwner: String = "LiveDataRuntime",
    val viewModelOwner: String = "LiveDataViewModel",
    val uiOwner: String = "LiveDataScreen",
    val historyOwner: String = "LiveDataHistoryStore",
    val mode01Only: Boolean = true,
    val readOnly: Boolean = true,
    val syntheticVehicleValues: Boolean = false
)

/**
 * Creates the existing engine from the already initialized session.
 * Call this only after ConnectionViewModel reaches READY.
 */
fun createLiveDataEngine(session: com.autodiag.core.obd.Elm327Session): com.autodiag.core.obd.ObdLiveDataEngine =
    com.autodiag.core.obd.ObdLiveDataEngine(session)
