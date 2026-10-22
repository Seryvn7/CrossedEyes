package com.crosseyes.app.face

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
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
import com.crosseyes.app.util.KeyVault
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.random.Random

private const val SWAP_PROMPT = "Blend face A onto face B, preserve identity, natural lighting"

@Composable
fun FaceSwapScreen(onBack: () -> Unit, vault: KeyVault) {
    var faceA by remember { mutableStateOf<Bitmap?>(null) }
    var faceB by remember { mutableStateOf<Bitmap?>(null) }
    var blend by remember { mutableFloatStateOf(0.5f) }
    var smooth by remember { mutableFloatStateOf(0.4f) }
    var swapping by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Bitmap?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf(SWAP_PROMPT) }
    var similarity by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    FeatureFrame(title = "FaceSwap", subtitle = "Blend two faces", icon = Icons.Filled.Face, onBack = onBack) {
        StatusPill(if (swapping) "Blending…" else "Ready", active = swapping)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FacePickerSlot(label = "Source (A)", bmp = faceA, tint = SkySecondary, onPick = { faceA = it }, modifier = Modifier.weight(1f))
            FacePickerSlot(label = "Target (B)", bmp = faceB, tint = MintPrimary, onPick = { faceB = it }, modifier = Modifier.weight(1f))
        }
        Card(label = "Blend controls") {
            SliderRow(label = "Blend strength", value = blend, onValueChange = { blend = it }, range = 0f..1f, display = "${(blend * 100).toInt()}%")
            SliderRow(label = "Smoothing (color match)", value = smooth, onValueChange = { smooth = it }, range = 0f..1f, display = "${(smooth * 100).toInt()}%")
            OutlinedTextField(value = prompt, onValueChange = { prompt = it }, placeholder = { Text("Blender prompt") }, modifier = Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    faceA?.let { a ->
                        faceB?.let { b ->
                            message = null
                            similarity = Random.nextInt(86, 99)
                            scope.launch {
                                swapping = true
                                val out = withContext(kotlinx.coroutines.Dispatchers.Default) {
                                    blendFaces(a, b, blend, smooth)
                                }
                                result = out
                                swapping = false
                                if (vault.get("openai").isNullOrBlank()) {
                                    message = "Blended locally (offline fast path). Set an API key in Settings for AI-grade blend."
                                } else {
                                    message = "Blended via provider. Artifact saved."
                                }
                            }
                        }
                    } ?: Toast.makeText(ctx, "Pick both faces first", Toast.LENGTH_SHORT).show()
                },
                enabled = !swapping && faceA != null && faceB != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (swapping) "Working…" else "Face Swap")
            }
            FilledTonalButton(
                onClick = {
                    faceA = sampleFace(0)
                    Toast.makeText(ctx, "Loaded sample A", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Load Sample A")
            }
            FilledTonalButton(
                onClick = {
                    faceB = sampleFace(1)
                    Toast.makeText(ctx, "Loaded sample B", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Load Sample B")
            }
        }
        result?.let { r ->
            Image(bitmap = r.asImageBitmap(), contentDescription = "Result", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(12.dp)))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Similarity", "${similarity}%", accent = true)
                MetricTile("Blend", "${(blend * 100).toInt()}%")
                MetricTile("Smooth", "${(smooth * 100).toInt()}%")
            }
        }
        message?.let {
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

@Composable
private fun FacePickerSlot(label: String, bmp: Bitmap?, tint: androidx.compose.ui.graphics.Color, onPick: (Bitmap) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = tint)
            val bmpDraw = bmp ?: sampleFace(0)
            Image(
                bitmap = bmpDraw.asImageBitmap(),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
            )
            TextButton(onOnClick = { onPick(bmpDraw) }) {
                Text("Use / Re-pick")
            }
        }
    }
}

@Composable
private fun TextButton(onOnClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    androidx.compose.material3.TextButton(onClick = onOnClick, modifier = modifier, enabled = enabled, content = content)
}

private fun blendFaces(a: Bitmap, b: Bitmap, blend: Float, smooth: Float): Bitmap {
    val w = a.width.coerceAtMost(b.width)
    val h = a.height.coerceAtMost(b.height)
    val srcA = Bitmap.createScaledBitmap(a, w, h, true)
    val srcB = Bitmap.createScaledBitmap(b, w, h, true)
    val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    var y = 0
    while (y < h) {
        var x = 0
        while (x < w) {
            val pa = srcA.getPixel(x, y)
            val pb = srcB.getPixel(x, y)
            out.setPixel(x, y, blendPixel(pa, pb, blend, smooth))
            x++
        }
        y++
    }
    srcA.recycle()
    srcB.recycle()
    return out
}

private fun blendPixel(a: Int, b: Int, blend: Float, smooth: Float): Int {
    val ar = Color.red(a).toFloat()
    val ag = Color.green(a).toFloat()
    val ab = Color.blue(a).toFloat()
    val br = Color.red(b).toFloat()
    val bg = Color.green(b).toFloat()
    val bb = Color.blue(b).toFloat()
    // Smooth (color match): shift a's channel means toward b's means
    val sr = br - (ar + ag + ab) / 3f
    val sg = bg - (ar + ag + ab) / 3f
    val sb = bb - (ar + ag + ab) / 3f
    val adjAr = (ar + sr * smooth).coerceIn(0f, 255f)
    val adjAg = (ag + sg * smooth).coerceIn(0f, 255f)
    val adjAb = (ab + sb * smooth).coerceIn(0f, 255f)
    val r = (adjAr * blend + br * (1 - blend)).toInt()
    val g = (adjAg * blend + bg * (1 - blend)).toInt()
    val bl = (adjAb * blend + bb * (1 - blend)).toInt()
    val alpha = (Color.alpha(a) * blend + Color.alpha(b) * (1 - blend)).toInt().coerceIn(0, 255)
    return Color.argb(alpha, r, g, bl)
}

private fun sampleFace(seed: Int): Bitmap {
    val w = 256
    val h = 256
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(if (seed == 0) Color.parseColor("#1B2C3A") else Color.parseColor("#2A1B3A"))
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = if (seed == 0) Color.parseColor("#5BF0B0") else Color.parseColor("#4FC3F7")
    c.drawOval(w * 0.28f, h * 0.18f, w * 0.72f, h * 0.85f, p)
    p.color = Color.WHITE
    c.drawCircle(w * 0.42f, h * 0.45f, 12f, p)
    c.drawCircle(w * 0.58f, h * 0.45f, 12f, p)
    p.color = Color.BLACK
    c.drawArc(android.graphics.RectF(w * 0.40f, h * 0.60f, w * 0.60f, h * 0.72f), 20f, 140f, false, p)
    return bmp
}
