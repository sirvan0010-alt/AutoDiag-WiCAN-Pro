package com.autodiag.wican.core.diagnostics

enum class DashboardWidgetType { VALUE, GAUGE, GRAPH, STATUS, COMMUNICATION }

data class DashboardWidget(
    val id: String,
    val signalId: String,
    val title: String,
    val unit: String,
    val type: DashboardWidgetType = DashboardWidgetType.VALUE,
    val min: Double? = null,
    val max: Double? = null,
    val visible: Boolean = true
)

data class DashboardProfile(
    val id: String,
    val name: String,
    val widgets: List<DashboardWidget>,
    val landscape: Boolean = false,
    val hudMirror: Boolean = false
)

/** Profiles contain presentation only; vehicle/protocol meaning remains in core decoders. */
object DefaultDashboardProfiles {
    val driver = DashboardProfile(
        id = "driver",
        name = "Řidičský přehled",
        widgets = listOf(
            DashboardWidget("speed", "speed", "Rychlost", "km/h", DashboardWidgetType.GAUGE, 0.0, 240.0),
            DashboardWidget("rpm", "rpm", "Otáčky", "rpm", DashboardWidgetType.VALUE),
            DashboardWidget("coolant", "coolant", "Teplota", "°C", DashboardWidgetType.VALUE),
            DashboardWidget("voltage", "voltage", "Napětí", "V", DashboardWidgetType.VALUE)
        )
    )

    val hud = driver.copy(id = "hud", name = "SEOBD HUD", landscape = true, hudMirror = true)
}
