package com.autodiag.wican.core.capability

/**
 * Exact identity used when deciding whether a diagnostic capability may be used.
 * Unknown fields stay unknown; callers must not widen scope implicitly.
 */
data class VehicleScope(
    val vin: String? = null,
    val make: String? = null,
    val model: String? = null,
    val generation: String? = null,
    val modelYear: Int? = null,
    val productionDate: String? = null,
    val engine: String? = null,
    val motor: String? = null,
    val battery: String? = null,
    val transmission: String? = null,
    val drivetrain: String? = null,
    val region: String? = null
)

data class EcuScope(
    val address: Int? = null,
    val name: String? = null,
    val supplier: String? = null,
    val partNumber: String? = null,
    val hardwareNumber: String? = null,
    val softwareNumber: String? = null,
    val softwareVersion: String? = null,
    val protocol: String? = null
)

data class DiagnosticScope(
    val vehicle: VehicleScope,
    val ecu: EcuScope? = null
)

enum class ScopeMatch {
    EXACT,
    PARTIAL,
    CONFLICT,
    UNKNOWN
}

/** Conservative matcher: conflicting known values always win over partial matches. */
object DiagnosticScopeMatcher {
    fun match(actual: DiagnosticScope, required: DiagnosticScope): ScopeMatch {
        val vehicle = compareVehicle(actual.vehicle, required.vehicle)
        if (vehicle == ScopeMatch.CONFLICT) return vehicle

        val actualEcu = actual.ecu
        val requiredEcu = required.ecu
        if (requiredEcu == null) return vehicle
        if (actualEcu == null) return ScopeMatch.UNKNOWN

        val ecu = compareEcu(actualEcu, requiredEcu)
        if (ecu == ScopeMatch.CONFLICT) return ecu
        if (vehicle == ScopeMatch.EXACT && ecu == ScopeMatch.EXACT) return ScopeMatch.EXACT
        return ScopeMatch.PARTIAL
    }

    private fun compareVehicle(a: VehicleScope, r: VehicleScope): ScopeMatch {
        var exact = true
        fun <T> check(actual: T?, required: T?) {
            if (required == null) return
            if (actual == null) { exact = false; return }
            if (actual != required) throw ScopeConflict()
        }
        return try {
            check(a.vin, r.vin); check(a.make, r.make); check(a.model, r.model)
            check(a.generation, r.generation); check(a.modelYear, r.modelYear)
            check(a.productionDate, r.productionDate); check(a.engine, r.engine)
            check(a.motor, r.motor); check(a.battery, r.battery)
            check(a.transmission, r.transmission); check(a.drivetrain, r.drivetrain)
            check(a.region, r.region)
            if (exact) ScopeMatch.EXACT else ScopeMatch.PARTIAL
        } catch (_: ScopeConflict) { ScopeMatch.CONFLICT }
    }

    private fun compareEcu(a: EcuScope, r: EcuScope): ScopeMatch {
        var exact = true
        fun <T> check(actual: T?, required: T?) {
            if (required == null) return
            if (actual == null) { exact = false; return }
            if (actual != required) throw ScopeConflict()
        }
        return try {
            check(a.address, r.address); check(a.name, r.name); check(a.supplier, r.supplier)
            check(a.partNumber, r.partNumber); check(a.hardwareNumber, r.hardwareNumber)
            check(a.softwareNumber, r.softwareNumber); check(a.softwareVersion, r.softwareVersion)
            check(a.protocol, r.protocol)
            if (exact) ScopeMatch.EXACT else ScopeMatch.PARTIAL
        } catch (_: ScopeConflict) { ScopeMatch.CONFLICT }
    }

    private class ScopeConflict : RuntimeException()
}
