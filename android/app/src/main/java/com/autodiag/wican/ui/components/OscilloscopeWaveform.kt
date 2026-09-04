package com.autodiag.wican.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.autodiag.core.oscilloscope.OscilloscopeCapture
import com.autodiag.core.oscilloscope.OscilloscopeSample
import com.autodiag.core.oscilloscope.OscilloscopeViewConfig
import kotlin.math.max

/**
 * Hardware-neutral oscilloscope waveform renderer.
 *
 * The renderer deliberately consumes the core capture model and does not make
 * assumptions about the electrical source. Input limits and isolation remain
 * properties of the actual measurement hardware.
 */
@Composable
fun OscilloscopeWaveform(
    capture: OscilloscopeCapture,
    config: OscilloscopeViewConfig,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(240.dp),
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val halfVisibleAmplitude = config.visibleAmplitudeVolts() / 2.0
        val visibleDuration = config.visibleDurationNanos()

        fun xFor(sample: OscilloscopeSample): Float {
            val relative = sample.timestampNanos - config.horizontalOffsetNanos
            return (relative.toDouble() / visibleDuration.toDouble() * width).toFloat()
        }

        fun yFor(voltage: Double): Float {
            val normalized = (voltage - config.verticalOffsetVolts) / max(halfVisibleAmplitude, 1e-12)
            return (centerY - normalized * centerY).toFloat()
        }

        // Grid: the center axes plus configurable divisions.
        val gridStroke = Stroke(width = 1f)
        for (i in 0..config.gridDivisionsX) {
            val x = width * i / config.gridDivisionsX
            drawLine(
                color = Color.Gray,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = gridStroke.width,
            )
        }
        for (i in 0..config.gridDivisionsY) {
            val y = height * i / config.gridDivisionsY
            drawLine(
                color = Color.Gray,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = gridStroke.width,
            )
        }

        val path = Path()
        capture.samples.forEachIndexed { index, sample ->
            val point = Offset(xFor(sample), yFor(sample.voltage))
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }

        if (!path.isEmpty) {
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 3f, cap = StrokeCap.Round),
            )
        }
    }
}
