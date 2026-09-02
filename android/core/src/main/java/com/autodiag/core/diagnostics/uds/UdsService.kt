package com.autodiag.core.diagnostics.uds

/**
 * Safety-oriented classification of UDS services.
 *
 * This is a protocol-level classification only. It does not mean that a
 * particular ECU or vehicle supports the service or that the operation is
 * safe to execute. Vehicle/ECU capability discovery must make that decision.
 */
enum class UdsServiceRisk {
    READ,
    STATE_CHANGING,
    SERVICE_WRITE,
    CONFIG_WRITE,
    ACTUATOR_CONTROL,
    SECURITY_CRITICAL,
    PROGRAMMING,
    ADVANCED,
    UNKNOWN,
}

enum class UdsService(val serviceId: Int, val risk: UdsServiceRisk) {
    DIAGNOSTIC_SESSION_CONTROL(0x10, UdsServiceRisk.STATE_CHANGING),
    ECU_RESET(0x11, UdsServiceRisk.SECURITY_CRITICAL),
    CLEAR_DIAGNOSTIC_INFORMATION(0x14, UdsServiceRisk.SERVICE_WRITE),
    READ_DTC_INFORMATION(0x19, UdsServiceRisk.READ),
    READ_DATA_BY_IDENTIFIER(0x22, UdsServiceRisk.READ),
    READ_MEMORY_BY_ADDRESS(0x23, UdsServiceRisk.READ),
    READ_SCALING_DATA_BY_IDENTIFIER(0x24, UdsServiceRisk.READ),
    SECURITY_ACCESS(0x27, UdsServiceRisk.SECURITY_CRITICAL),
    COMMUNICATION_CONTROL(0x28, UdsServiceRisk.STATE_CHANGING),
    WRITE_DATA_BY_IDENTIFIER(0x2E, UdsServiceRisk.CONFIG_WRITE),
    INPUT_OUTPUT_CONTROL_BY_IDENTIFIER(0x2F, UdsServiceRisk.ACTUATOR_CONTROL),
    ROUTINE_CONTROL(0x31, UdsServiceRisk.SERVICE_WRITE),
    REQUEST_DOWNLOAD(0x34, UdsServiceRisk.PROGRAMMING),
    REQUEST_UPLOAD(0x35, UdsServiceRisk.PROGRAMMING),
    TRANSFER_DATA(0x36, UdsServiceRisk.PROGRAMMING),
    REQUEST_TRANSFER_EXIT(0x37, UdsServiceRisk.PROGRAMMING),
    REQUEST_FILE_TRANSFER(0x38, UdsServiceRisk.PROGRAMMING),
    TESTER_PRESENT(0x3E, UdsServiceRisk.STATE_CHANGING),
    ACCESS_TIMING_PARAMETERS(0x83, UdsServiceRisk.ADVANCED),
    SECURED_DATA_TRANSMISSION(0x84, UdsServiceRisk.SECURITY_CRITICAL),
    CONTROL_DTC_SETTING(0x85, UdsServiceRisk.STATE_CHANGING),
    RESPONSE_ON_EVENT(0x86, UdsServiceRisk.ADVANCED),
    LINK_CONTROL(0x87, UdsServiceRisk.ADVANCED),
    UNKNOWN(-1, UdsServiceRisk.UNKNOWN);

    companion object {
        fun fromServiceId(serviceId: Int): UdsService {
            val normalized = serviceId and 0xFF
            return entries.firstOrNull { it.serviceId == normalized } ?: UNKNOWN
        }

        fun positiveResponseServiceId(serviceId: Int): Int = (serviceId and 0xFF) + 0x40

        fun isPositiveResponse(serviceId: Int, originalServiceId: Int): Boolean =
            (serviceId and 0xFF) == positiveResponseServiceId(originalServiceId)
    }
}
