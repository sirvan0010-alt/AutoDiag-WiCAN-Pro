package com.autodiag.core.contribution

object VehicleDataAnonymizer {

    private val VIN_PATTERN = Regex("^[A-HJ-NPR-Z0-9]{17}$")

    fun vehicleScopeFrom(vin: String?): VehicleScope? {
        val clean = vin?.trim()?.uppercase() ?: return null
        if (!VIN_PATTERN.matches(clean)) return null
        return VehicleScope(wmiVdsModelYear = clean.take(10))
    }

    fun redactFreeText(@Suppress("UNUSED_PARAMETER") text: String?): String? = null

    fun monthBucket(epochMs: Long): String {
        val instant = java.time.Instant.ofEpochMilli(epochMs)
        val date = java.time.LocalDate.ofInstant(instant, java.time.ZoneOffset.UTC)
        return "%04d-%02d".format(date.year, date.monthValue)
    }
}

data class VehicleScope(
    val wmiVdsModelYear: String
)
