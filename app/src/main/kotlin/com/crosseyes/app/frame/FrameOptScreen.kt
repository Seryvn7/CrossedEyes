package com.crosseyes.app.frame

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.crosseyes.app.theme.MintPrimary
import com.crosseyes.app.theme.SkySecondary
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.SystemClock
import java.io.File
import java.io.FileOutputStream

@Composable
fun FrameOptScreen(onBack: () -> Unit) {
    var quality by remember { mutableIntStateOf(85) }
    var scale by remember { mutableIntStateOf(75) }
    var keepAspect by remember { mutableStateOf(true) }
    var sharpen by remember { mutableFloatStateOf(1.4f) }
    var optimizing by remember { mutableStateOf(false) }
    var resultMs by remember { mutableFloatStateOf(0f) }
    var resultSizeKb by remember { mutableIntStateOf(0) }
    var lastOut by remember { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    FeatureFrame(title = "Frame Render Optimization", subtitle = "Faster generation & better performance", icon = Icons.Filled.Speed) {
        StatusPill(if (optimizing) "Optimizing…" else "Idle", active = optimizing)
        Card(label = "Settings") {
            SliderRow(
                label = "Output quality",
                value = quality.toFloat(),
                onValueChange = { quality = it.toInt() },
                range = 10f..100f,
                display = "$quality%"
            )
            SliderRow(
                label = "Downscale",
                value = scale.toFloat(),
                onValueChange = { scale = it.toInt() },
                range = 25f..100f,
                display = "${scale}%"
            )
            SliderRow(
                label = "Sharpen",
                value = sharpen,
                onValueChange = { sharpen = it },
                range = 0.5f..3f,
                display = "%.2f x".format(sharpen)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { keepAspect = !keepAspect }
            ) {
                Icon(
                    if (keepAspect) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (keepAspect) MintPrimary else SkySecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text("Keep original aspect", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    optimizing = true
                    scope.launch {
                        val (bmp, elapsed) = withContext(kotlinx.coroutines.Dispatchers.Default) {
                            optimizeFrame(quality = quality, scalePercent = scale, sharpen = sharpen, keepAspect = keepAspect)
                        }
                        preview = bmp
                        resultMs = elapsed
                        val file = File(ctx.getDir("output", 0), "frame-${System.currentTimeMillis()}.jpg")
                        FileOutputStream(file).use { o -> bmp.compress(Bitmap.CompressFormat.JPEG, quality, o) }
                        resultSizeKb = (file.length() / 1024).toInt()
                        lastOut = file.absolutePath
                        optimizing = false
                    }
                },
                enabled = !optimizing,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (optimizing) "Working…" else "Optimize Frame")
            }
        }
        preview?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp).clip(RoundedCornerShape(12.dp))
            )
        }
        if (lastOut != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Render time", "%.0f ms".format(resultMs), accent = true)
                MetricTile("File size", "${resultSizeKb} KB")
                MetricTile("Throughput", "${(1000f / resultMs.coerceAtLeast(1f)).toInt()} fps", accent = true)
            }
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

private fun optimizeFrame(quality: Int, scalePercent: Int, sharpen: Float, keepAspect: Boolean): Pair<Bitmap, Float> {
    val source = makeSampleFrame(960, 540)
    val t0 = SystemClock.elapsedRealtime()
    val sw = (source.width * scalePercent / 100).coerceAtLeast(64)
    val sh = (source.height * scalePercent / 100).coerceAtLeast(64)
    // "Keep original aspect" locks the height ratio; when off we render a square crop.
    val (targetW, targetH) = if (keepAspect) sw to sh else sw to sw
    var out = Bitmap.createScaledBitmap(source, targetW, targetH, true)
    if (sharpen > 1.001f) out = sharpenBitmap(out, sharpen)
    source.recycle()
    val elapsed = (SystemClock.elapsedRealtime() - t0).toFloat()
    return out to elapsed
}

private fun makeSampleFrame(w: Int, h: Int): Bitmap {
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = android.graphics.Canvas(bmp)
    c.drawColor(android.graphics.Color.parseColor("#0C1A16"))
    val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    p.color = android.graphics.Color.parseColor("#5BF0B0")
    c.drawOval(w * 0.35f, h * 0.2f, w * 0.65f, h * 0.7f, p)
    p.color = android.graphics.Color.parseColor("#4FC3F7")
    c.drawOval(w * 0.40f, h * 0.42f, w * 0.60f, h * 0.72f, p)
    p.color = android.graphics.Color.WHITE
    p.textSize = h * 0.12f
    c.drawText("Sample", w * 0.42f, h * 0.15f, p)
    return bmp
}

private fun sharpenBitmap(src: Bitmap, amount: Float): Bitmap {
    // Unsharp mask: out = src + amount * (src - boxBlur3x3(src)).
    // (The platform ConvolutionMatrix is not on this compile classpath, so we do
    //  the 3x3 convolution manually — same visual result, slightly slower.)
    val w = src.width
    val h = src.height
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val srcPx = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
    val outPx = IntArray(w * h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = srcPx[y * w + x]
            val r = android.graphics.Color.red(c).toFloat()
            val g = android.graphics.Color.green(c).toFloat()
            val b = android.graphics.Color.blue(c).toFloat()
            var sr = 0f; var sg = 0f; var sb = 0f
            for (dy in -1..1) for (dx in -1..1) {
                val nx = (x + dx).coerceIn(0, w - 1)
                val ny = (y + dy).coerceIn(0, h - 1)
                val n = srcPx[ny * w + nx]
                sr += android.graphics.Color.red(n)
                sg += android.graphics.Color.green(n)
                sb += android.graphics.Color.blue(n)
            }
            sr /= 9f; sg /= 9f; sb /= 9f
            val nr = (r + (r - sr) * amount).coerceIn(0f, 255f)
            val ng = (g + (g - sg) * amount).coerceIn(0f, 255f)
            val nb = (b + (b - sb) * amount).coerceIn(0f, 255f)
            outPx[y * w + x] = android.graphics.Color.argb(android.graphics.Color.alpha(c), nr.toInt(), ng.toInt(), nb.toInt())
        }
    }
    out.setPixels(outPx, 0, w, 0, 0, w, h)
    return out
}
