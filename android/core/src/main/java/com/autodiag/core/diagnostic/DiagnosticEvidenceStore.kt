package com.autodiag.core.diagnostic

/** Thread-safe append-only history of diagnostic evidence for a session or capture. */
class DiagnosticEvidenceStore {
    private val evidence = mutableListOf<DiagnosticEvidence<*>>()

    @Synchronized
    fun append(item: DiagnosticEvidence<*>) {
        evidence += item
    }

    @Synchronized
    fun snapshot(): List<DiagnosticEvidence<*>> = evidence.toList()

    @Synchronized
    fun clear() {
        evidence.clear()
    }
}
