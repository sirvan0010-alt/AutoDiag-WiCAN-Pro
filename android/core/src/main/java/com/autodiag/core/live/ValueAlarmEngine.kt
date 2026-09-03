package com.autodiag.core.live

/** Torque-like alarms, without their UI. Hysteresis on measured values only. */
data class ValueAlarm(
    val id: String,
    val pid: Int,
    val min: Double? = null,
    val max: Double? = null,
    val hysteresis: Double = 0.0,
)

enum class AlarmState { IDLE, ACTIVE, UNKNOWN }

class ValueAlarmEngine {
    private val active = mutableSetOf<String>()

    fun evaluate(alarm: ValueAlarm, value: Double?): AlarmState {
        if (value == null) return AlarmState.UNKNOWN
        val was = alarm.id in active
        val low = alarm.min
        val high = alarm.max
        val h = alarm.hysteresis
        val violate = (low != null && value < low) || (high != null && value > high)
        val clear =
            (low == null || value > low + h) && (high == null || value < high - h)
        return when {
            violate -> {
                active += alarm.id
                AlarmState.ACTIVE
            }
            was && !clear -> AlarmState.ACTIVE
            else -> {
                active.remove(alarm.id)
                AlarmState.IDLE
            }
        }
    }
}
