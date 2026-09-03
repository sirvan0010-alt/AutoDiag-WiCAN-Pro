package com.autodiag.core.experimental.summon

/** Simulator-only vehicle pose. LIVE path still cannot consume this. */
data class SummonPose(
    val xM: Double,
    val yM: Double,
    val headingDeg: Double,
    val speedMps: Double,
)

data class SummonWaypoint(val xM: Double, val yM: Double)

class SummonSimulator(
    private val gate: SummonController = SummonController(),
) {
    var pose: SummonPose = SummonPose(0.0, 0.0, 0.0, 0.0)
        private set
    var waypoint: SummonWaypoint = SummonWaypoint(8.0, 0.0)

    fun step(request: SummonRequest, dtSec: Double = 0.1): SummonTick {
        val tick = gate.tick(request.copy(mode = SummonExecutionMode.SIMULATOR))
        if (tick.phase == SummonPhase.HOLDING) {
            val dx = waypoint.xM - pose.xM
            val dy = waypoint.yM - pose.yM
            val dist = kotlin.math.hypot(dx, dy)
            if (dist < 0.3) {
                pose = pose.copy(speedMps = 0.0)
                return tick.copy(
                    phase = SummonPhase.COMPLETE,
                    messageCs = "SIMULATOR: cíl dosažen (stále bez CAN).",
                    audit = "summon simulator complete",
                )
            }
            val step = 0.4 * dtSec
            val nx = pose.xM + dx / dist * step
            val ny = pose.yM + dy / dist * step
            pose = SummonPose(nx, ny, kotlin.math.atan2(dy, dx) * 180.0 / Math.PI, step / dtSec)
        } else {
            pose = pose.copy(speedMps = 0.0)
        }
        return tick
    }
}
