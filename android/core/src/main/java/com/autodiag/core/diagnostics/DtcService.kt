package com.autodiag.core.diagnostics

import com.autodiag.core.obd.DtcMemory
import com.autodiag.core.obd.ObdDtcCommands

/** A transport-neutral diagnostic operation. The transport layer executes the bytes. */
data class DiagnosticOperation(
    val request: ByteArray,
    val description: String,
    val stateChanging: Boolean
) {
    override fun equals(other: Any?): Boolean = other is DiagnosticOperation &&
        request.contentEquals(other.request) && description == other.description && stateChanging == other.stateChanging

    override fun hashCode(): Int = request.contentHashCode() * 31 + description.hashCode() * 31 + stateChanging.hashCode()
}

object DtcService {
    fun read(memory: DtcMemory): DiagnosticOperation = when (memory) {
        DtcMemory.STORED -> DiagnosticOperation(byteArrayOf(ObdDtcCommands.READ_STORED_MODE.toByte()), "Read stored DTCs", false)
        DtcMemory.PENDING -> DiagnosticOperation(byteArrayOf(ObdDtcCommands.READ_PENDING_MODE.toByte()), "Read pending DTCs", false)
        DtcMemory.PERMANENT -> DiagnosticOperation(byteArrayOf(ObdDtcCommands.READ_PERMANENT_MODE.toByte()), "Read permanent DTCs", false)
        DtcMemory.UNKNOWN -> error("Unknown DTC memory")
    }

    /** Clear is deliberately exposed as a state-changing operation, never as a read helper. */
    fun clear(): DiagnosticOperation = DiagnosticOperation(
        request = ObdDtcCommands.clearRequest(),
        description = "Clear ECU DTC memory",
        stateChanging = true
    )
}
