package com.autodiag.core.diagnostic

/** Typed lifecycle/event vocabulary shared by live sessions, simulator and replay. */
enum class DiagnosticEventType {
    SESSION_STARTED,
    TRANSPORT_CONNECTED,
    ELM_INITIALIZED,
    CAPABILITY_DISCOVERED,
    VEHICLE_IDENTIFIED,
    ECU_DISCOVERED,
    MEASUREMENT_RECEIVED,
    DTC_RECEIVED,
    TEST_PHASE_STARTED,
    TEST_PHASE_COMPLETED,
    LOAD_STARTED,
    LOAD_STOPPED,
    RECOVERY_STARTED,
    BUS_HEALTH_CHANGED,
    CAPTURE_STARTED,
    CAPTURE_STOPPED,
    ANALYSIS_COMPLETED,
    SESSION_ENDED
}

/** Immutable event with timestamp and optional evidence reference. */
data class DiagnosticEvent(
    val type: DiagnosticEventType,
    val timestampEpochMs: Long,
    val sessionId: String,
    val message: String? = null,
    val evidenceKey: String? = null,
    val ecuId: String? = null
)

/** In-memory event stream primitive; higher layers may adapt it to Flow/persistence. */
class DiagnosticEventStream {
    private val events = mutableListOf<DiagnosticEvent>()

    @Synchronized
    fun append(event: DiagnosticEvent) {
        events += event
    }

    @Synchronized
    fun snapshot(): List<DiagnosticEvent> = events.toList()

    @Synchronized
    fun clear() {
        events.clear()
    }
}
