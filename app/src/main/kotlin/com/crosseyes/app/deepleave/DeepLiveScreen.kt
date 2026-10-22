package com.crosseyes.app.deepleave

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill
import kotlin.math.sin
import kotlin.random.Random

private val rng = Random(3)
private val accent = com.crosseyes.app.theme.AccentGlow
private val secondaryAccent = com.crosseyes.app.theme.SkySecondary

@Composable
fun DeepLiveScreen(onBack: () -> Unit) {
    var running by remember { mutableStateOf(true) }
    var livenessScore by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("Face detected — begin liveness") }
    val transition = rememberInfiniteTransition(label = "dl")
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    val headTurn by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(3800, easing = FastOutSlowInEasing), RepeatMode.Reverse))

    var tickCounter by remember { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        if (running) while (true) {
            tickCounter++
            livenessScore = (livenessScore + 3).coerceAtMost(100)
            val prompts = listOf(
                "Face detected — begin liveness", "Smile (turn 1/3)", "Turn head left (2/3)",
                "Turn head right (3/3)", "Blink (4/4)", "Liveness passed (5/5) — session active"
            )
            prompt = prompts[tickCounter % prompts.size]
            kotlinx.coroutines.delay(2800)
        }
    }

    FeatureFrame(title = "DeepLive", subtitle = "Animated liveness preview", icon = Icons.Filled.Spa) {
        StatusPill(if (running) "Live feed active" else "Paused", active = running)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "Face Confidence", value = "98%", accent = true)
            MetricTile(label = "Liveness", value = "$livenessScore%")
            MetricTile(label = "Pose", value = "Stable")
        }

        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f).clip(RoundedCornerShape(14.dp))) {
            val bg = Brush.verticalGradient(listOf(Color(0xFF0C1A16), Color(0xFF16362E)))
            drawRect(bg)

            val cx = size.width * 0.5f
            val headTurnPx = (headTurn - 0.5f) * size.width * 0.12f
            val chestRise = breath * size.height * 0.02f
            val headCx = cx + headTurnPx
            drawOval(
                color = accent.copy(alpha = 0.85f),
                topLeft = Offset(headCx - size.width * 0.085f, size.height * 0.16f - size.height * 0.02f),
                size = Size(size.width * 0.17f, size.height * 0.30f)
            )
            drawOval(
                color = secondaryAccent.copy(alpha = 0.55f),
                topLeft = Offset(headCx - size.width * 0.16f, size.height * 0.27f - chestRise),
                size = Size(size.width * 0.32f, size.height * 0.50f)
            )
            for (i in 0..120) {
                val a = 0.04f + rng.nextFloat() * 0.12f
                drawCircle(Color.White.copy(alpha = a), radius = 1.2f, center = Offset(rng.nextFloat() * size.width, rng.nextFloat() * size.height))
            }
            drawRect(color = accent.copy(alpha = 0.4f), size = size, style = Stroke(width = 2f))
            drawLine(secondaryAccent.copy(alpha = 0.35f), start = Offset(headCx, size.height * 0.16f), end = Offset(headCx, size.height * 0.35f - size.height * 0.10f), strokeWidth = 1.6f)
        }

        OutlinedTextField(
            value = prompt,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { running = !running }, modifier = Modifier.weight(1f)) {
                Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text(if (running) "Pause" else "Run Liveness")
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back to Home") }
        }
    }
}
