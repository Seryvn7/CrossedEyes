package com.crosseyes.app.cam

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill
import kotlin.math.roundToInt
import kotlin.random.Random

private val noise = Random(7)
private val accent = com.crosseyes.app.theme.MintPrimary

@Composable
fun CameraEmulatorScreen(onBack: () -> Unit) {
    var running by remember { mutableStateOf(true) }
    var fpsTarget by remember { mutableIntStateOf(30) }
    val transition = rememberInfiniteTransition(label = "cam")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "cam.t"
    )
    var fps by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(running, fpsTarget) {
        if (running) while (true) {
            val target = if (running) fpsTarget else 0
            fps = fps * 0.9f + target * 0.1f
            kotlinx.coroutines.delay(50)
        }
    }

    FeatureFrame(title = "Camera Emulator", subtitle = "Simulated live camera feed", icon = Icons.Filled.PhotoCamera) {
        StatusPill(if (running) "LIVE" else "PAUSED", active = running)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "FPS", value = fps.roundToInt().toString(), accent = true)
            MetricTile(label = "Resolution", value = "1280x720")
            MetricTile(label = "Sensor", value = "sim-1")
        }
        EmulatedFeed(t)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { running = !running }, modifier = Modifier.weight(1f)) {
                Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text(if (running) "Pause" else "Play")
            }
            FilledTonalButton(onClick = { }, modifier = Modifier.weight(1f), enabled = running) {
                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Capture")
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FPS", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = fpsTarget.toFloat(),
                onValueChange = { fpsTarget = it.toInt() },
                valueRange = 10f..60f,
                steps = 4,
                modifier = Modifier.weight(1f)
            )
            Text("${fpsTarget} fps", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun EmulatedFeed(t: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(14.dp))) {
        val bg = Brush.verticalGradient(listOf(Color(0xFF0C1A16), Color(0xFF16362E)))
        drawRect(bg)

        val cx = size.width * (0.5f + 0.22f * (t - 0.5f))
        val cy = size.height * (0.5f + 0.15f * (t - 0.5f))
        drawOval(
            color = Color(0xFF9FEFD0),
            topLeft = Offset(cx - size.width * 0.09f, cy - size.height * 0.14f),
            size = kotlin.math.max(6f, size.width * 0.18f).let { androidx.compose.ui.geometry.Size(it, size.height * 0.26f) }
        )
        drawOval(
            color = Color(0xFF6FD38F),
            topLeft = Offset(cx - size.width * 0.16f, cy - size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.42f)
        )

        for (i in 0..180) {
            val a = 0.05f + noise.nextFloat() * 0.12f
            drawCircle(
                color = Color.White.copy(alpha = a),
                radius = 1.4f,
                center = Offset(noise.nextFloat() * size.width, noise.nextFloat() * size.height)
            )
        }
        drawRect(
            color = accent.copy(alpha = 0.25f),
            size = size,
            style = Stroke(width = 2f)
        )
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(0f, size.height / 3f),
            end = Offset(size.width, size.height / 3f),
            strokeWidth = 1f
        )
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(0f, 2f * size.height / 3f),
            end = Offset(size.width, 2f * size.height / 3f),
            strokeWidth = 1f
        )
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(size.width / 3f, 0f),
            end = Offset(size.width / 3f, size.height),
            strokeWidth = 1f
        )
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(2f * size.width / 3f, 0f),
            end = Offset(2f * size.width / 3f, size.height),
            strokeWidth = 1f
        )

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.argb(235, 91, 240, 176)
        paint.textSize = size.height * 0.09f
        drawContext.canvas.nativeCanvas.drawText("REC", size.width * 0.04f, size.height * 0.13f, paint)

        paint.textSize = size.height * 0.05f
        paint.color = android.graphics.Color.argb(200, 232, 255, 246)
        val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        drawContext.canvas.nativeCanvas.drawText(ts, size.width * 0.78f, size.height * 0.13f, paint)
        drawCircle(color = Color(0xFF9FEFD0), radius = 2.5f, center = Offset(size.width * 0.48f, size.height * 0.11f))
    }
}
