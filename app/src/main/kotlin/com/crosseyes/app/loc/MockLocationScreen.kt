package com.crosseyes.app.loc

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.crosseyes.app.theme.MintPrimary
import com.crosseyes.app.theme.SkySecondary
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill
import com.crosseyes.app.system.ShizukuManager
import com.crosseyes.app.system.ShizukuLocationSpoofer
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class MockLoc(
    val lat: Double,
    val lon: Double,
    val altitude: Double,
    val speed: Double,
    val bearing: Float,
    val accuracy: Float
)

@Composable
fun MockLocationScreen(onBack: () -> Unit) {
    var active by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf(40.7580) }
    var lon by remember { mutableStateOf(-73.9855) }
    var speed by remember { mutableFloatStateOf(4.5f) }
    var altitude by remember { mutableFloatStateOf(12f) }
    var accuracy by remember { mutableFloatStateOf(8f) }
    var headingBy by remember { mutableFloatStateOf(45f) }
    var tick by remember { mutableIntStateOf(0) }
    var trail by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val origin = remember { 40.7580 to -73.9855 }
    val ctx = LocalContext.current
    var toasts by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(active, headingBy, speed) {
        if (!active) return@LaunchedEffect
        while (true) {
            tick++
            val dLat = (cos(Math.toRadians(headingBy.toDouble())) * speed * 0.000001)
            val dLon = (sin(Math.toRadians(headingBy.toDouble())) * speed * 0.000001)
            lat += dLat
            lon += dLon
            trail = (trail + offsetOf(lat, lon, origin.first, origin.second)).takeLast(220)
            delay(80)
        }
    }

    FeatureFrame(title = "Mock Location", subtitle = "Simulated GPS output", icon = Icons.Filled.MyLocation) {
        StatusPill(if (active) "Mocking GPS" else "Stopped", active = active)
        StatusPill(
            if (ShizukuManager.isPermissionGranted()) "Shizuku: Connected" else "Shizuku: Not connected",
            active = ShizukuManager.isPermissionGranted()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Lat", "%.4f".format(lat), accent = true)
            MetricTile("Lon", "%.4f".format(lon), accent = true)
            MetricTile("Altitude", "%.0f m".format(altitude))
            MetricTile("Accuracy", "±%.0f m".format(accuracy))
        }
        MockMap(
            trail = trail,
            heading = headingBy,
            accent = MintPrimary,
            secondary = SkySecondary
        )
        Card(label = "Parameters") {
            SliderRow(label = "Speed", value = speed, onValueChange = { speed = it }, range = 0f..15f, display = "%.1f m/s".format(speed))
            SliderRow(label = "Heading", value = headingBy, onValueChange = { headingBy = it }, range = 0f..360f, display = "${headingBy.toInt()}°")
            SliderRow(label = "Altitude", value = altitude, onValueChange = { altitude = it }, range = 0f..80f, display = "${altitude.toInt()} m")
            SliderRow(label = "Accuracy", value = accuracy, onValueChange = { accuracy = it }, range = 1f..50f, display = "±${accuracy.toInt()} m")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = {
                if (active) {
                    ShizukuLocationSpoofer.disableMockProvider(ctx)
                } else {
                    ShizukuLocationSpoofer.enableMockProvider(ctx)
                }
                active = !active
            }, modifier = Modifier.weight(1f)) {
                Text(if (active) "Stop Mocking" else "Start Mocking")
            }
            FilledTonalButton(
                onClick = {
                    val success = ShizukuLocationSpoofer.setLocation(ctx, lat, lon, altitude.toDouble(), accuracy)
                    toasts = if (success) "Injected: $lat, $lon" else "Failed to inject location"
                    Toast.makeText(ctx, toasts, Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Inject Location")
            }
        }
        toasts?.let {
            StatusPill(it, active = true)
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    display: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(display, style = MaterialTheme.typography.bodyMedium, color = MintPrimary)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

private fun offsetOf(lat: Double, lon: Double, lat0: Double, lon0: Double): Offset {
    val x = (lon - lon0) * 800_000.0
    val y = (lat - lat0) * -800_000.0
    return Offset(x.toFloat().coerceIn(-390f, 390f), y.toFloat().coerceIn(-220f, 220f))
}

@Composable
fun MockMap(
    trail: List<Offset>,
    heading: Float,
    accent: Color,
    secondary: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
    ) {
        val bg = Brush.verticalGradient(listOf(Color(0xFF0C1A16), Color(0xFF142E29)))
        drawRect(bg)
        for (i in 0..4) {
            val gx = 0.1f + i * 0.2f
            drawLine(secondary.copy(alpha = 0.18f), Offset(size.width * gx, 0f), Offset(size.width * gx, size.height), 1.4f)
        }
        for (i in 0..2) {
            val gy = 0.15f + i * 0.3f
            drawLine(MintPrimary.copy(alpha = 0.16f), Offset(0f, size.height * gy), Offset(size.width, size.height * gy), 1.4f)
        }
        val cx = size.width / 2f
        val cy = size.height / 2f
        // draw the trail
        trail.forEachIndexed { i, o ->
            val p = Offset(cx + o.x * size.width * 0.15f, cy + o.y * size.height * 0.4f)
            drawCircle(accent.copy(alpha = 0.5f + 0.3f * i / maxOf(trail.size, 1)), 2.5f, p)
        }
        // draw the heading line
        val h = Math.toRadians(heading.toDouble()).toFloat()
        val hx = cx + (size.width * 0.08f) * sin(h)
        val hy = cy - (size.width * 0.08f) * cos(h)
        drawLine(accent, Offset(cx, cy), Offset(hx, hy), 2.4f)
        drawCircle(accent, 6f, Offset(cx, cy))
        drawCircle(secondary, 10f, Offset(cx, cy), style = Stroke(width = 1.4f))
    }
}
