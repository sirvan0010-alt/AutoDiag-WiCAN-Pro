package com.autodiag.wican.core.capability

/**
 * Capabilities exposed by a WiCAN adapter's physical LED controller.
 *
 * This model deliberately separates a basic LED command from an automatic
 * activity mode. Recent WiCAN PRO firmware documents LED command/blink
 * support, but AutoDiag must verify the exact API and firmware behavior
 * before enabling automatic activity indication.
 */
data class WiCanLedCapability(
    val supported: Boolean,
    val firmwareVersion: String? = null,
    val commandInterface: LedCommandInterface = LedCommandInterface.UNKNOWN,
    val rgbSupported: Boolean = false,
    val blinkSupported: Boolean = false,
    val activityModeSupported: Boolean = false,
    val verified: Boolean = false,
)

enum class LedCommandInterface {
    UNKNOWN,
    WEB_CONSOLE,
    ELM327_TERMINAL,
    HTTP_API,
    MQTT,
    FIRMWARE_NATIVE,
}

enum class LedActivityState {
    IDLE,
    CONNECTED,
    RX_ACTIVITY,
    TX_ACTIVITY,
    HIGH_TRAFFIC,
    FAULT,
    UPDATING,
}

/**
 * Priority used by a future firmware-side or adapter-side LED state arbiter.
 * Fault/update indications must not be hidden by cosmetic traffic activity.
 */
fun LedActivityState.priority(): Int = when (this) {
    LedActivityState.FAULT -> 100
    LedActivityState.UPDATING -> 90
    LedActivityState.HIGH_TRAFFIC -> 40
    LedActivityState.RX_ACTIVITY,
    LedActivityState.TX_ACTIVITY -> 30
    LedActivityState.CONNECTED -> 20
    LedActivityState.IDLE -> 10
}
