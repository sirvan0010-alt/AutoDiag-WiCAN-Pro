package com.autodiag.core.capability

import com.autodiag.core.can.CanFrame

/**
 * Pairs already-classified observed request/response traffic for Outlander PHEV.
 *
 * This component deliberately does not discover or guess CAN identifiers, UDS DIDs,
 * Mode-22 PIDs, ECU addresses, or response relationships. The caller supplies the
 * traffic role and ECU key from an external observation/classification layer.
 */
class OutlanderPhevExchangePairer(
    private val responseWindowNanos: Long = DEFAULT_RESPONSE_WINDOW_NANOS
) {
    init {
        require(responseWindowNanos > 0) { "responseWindowNanos must be positive" }
    }

    data class Observation(
        val ecuKey: String,
        val role: Role,
        val frame: CanFrame,
        val source: String
    ) {
        init {
            require(ecuKey.isNotBlank()) { "ecuKey must not be blank" }
            require(source.isNotBlank()) { "source must not be blank" }
        }
    }

    enum class Role { REQUEST, RESPONSE }

    data class Pair(
        val ecuKey: String,
        val request: CanFrame,
        val response: CanFrame,
        val source: String,
        val requestTimestampNanos: Long,
        val responseTimestampNanos: Long
    )

    sealed interface Result {
        data class Paired(val pair: Pair) : Result
        data class UnmatchedResponse(val observation: Observation) : Result
        data class UnmatchedRequest(val observation: Observation) : Result
        data class InvalidTiming(val observation: Observation) : Result
    }

    private data class PendingRequest(val observation: Observation, val timestampNanos: Long)

    private val pending = LinkedHashMap<String, ArrayDeque<PendingRequest>>()

    fun accept(observation: Observation): Result? {
        val timestamp = observation.frame.timestampNanos ?: return null
        val queue = pending.getOrPut(observation.ecuKey) { ArrayDeque() }

        return when (observation.role) {
            Role.REQUEST -> {
                queue.addLast(PendingRequest(observation, timestamp))
                null
            }

            Role.RESPONSE -> {
                val candidate = queue.firstOrNull()
                when {
                    candidate == null -> Result.UnmatchedResponse(observation)
                    timestamp < candidate.timestampNanos -> Result.InvalidTiming(observation)
                    timestamp - candidate.timestampNanos > responseWindowNanos -> {
                        queue.removeFirst()
                        Result.UnmatchedResponse(observation)
                    }
                    else -> {
                        queue.removeFirst()
                        if (queue.isEmpty()) pending.remove(observation.ecuKey)
                        Result.Paired(
                            Pair(
                                ecuKey = observation.ecuKey,
                                request = candidate.observation.frame,
                                response = observation.frame,
                                source = "${candidate.observation.source};${observation.source}",
                                requestTimestampNanos = candidate.timestampNanos,
                                responseTimestampNanos = timestamp
                            )
                        )
                    }
                }
            }
        }
    }

    fun flush(nowNanos: Long): List<Result.UnmatchedRequest> {
        val expired = mutableListOf<Result.UnmatchedRequest>()
        val iterator = pending.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val queue = entry.value
            while (queue.isNotEmpty() && nowNanos - queue.first().timestampNanos > responseWindowNanos) {
                expired += Result.UnmatchedRequest(queue.removeFirst().observation)
            }
            if (queue.isEmpty()) iterator.remove()
        }
        return expired
    }

    fun clear() = pending.clear()

    companion object {
        const val DEFAULT_RESPONSE_WINDOW_NANOS = 500_000_000L
    }
}
