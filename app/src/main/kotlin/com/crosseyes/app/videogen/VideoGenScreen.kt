package com.crosseyes.app.videogen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.crosseyes.app.providers.ALL_VIDEO_PROVIDERS
import com.crosseyes.app.providers.AiProvider
import com.crosseyes.app.providers.ProviderCallResult
import com.crosseyes.app.providers.ProviderCalls
import com.crosseyes.app.theme.MintPrimary
import com.crosseyes.app.theme.SkySecondary
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill
import com.crosseyes.app.util.KeyVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun VideoGenScreen(onBack: () -> Unit, vault: KeyVault) {
    val providers = ALL_VIDEO_PROVIDERS
    var providerIdx by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("a mint-glowing hummingbird crossing a neon city") }
    var duration by remember { mutableIntStateOf(5) }
    var generating by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var statusLine by remember { mutableStateOf<String?>(null) }
    var lastArtifact by remember { mutableStateOf<ProviderCallResult?>(null) }
    var localPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val provider = providers.getOrElse(providerIdx) { providers.first() }

    FeatureFrame(
        title = "AI Video Generation",
        subtitle = "CrossEyes ClavGen engine",
        icon = Icons.Filled.Videocam,
        onBack = onBack
    ) {
        StatusPill(
            if (generating) "Generating… ${progress}%" else (statusLine ?: "Ready"),
            active = generating
        )
        Card(label = "Provider & prompt") {
            ProviderPicker(providers = providers, selected = providerIdx, onSelect = { providerIdx = it })
            TextRow("Model", provider.displayName, provider.tagline, tint = MintPrimary)
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Describe the scene…") },
                modifier = Modifier.fillMaxWidth()
            )
            DurationRow(value = duration, onValueChange = { duration = it })
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalButton(
                onClick = {
                    scope.launch {
                        generating = true
                        statusLine = "Call… ${provider.displayName} (HTTP)"
                        progress = 1
                        val r = ProviderCalls.video(vault, provider.id, prompt, duration)
                        withContext(Dispatchers.Main) {
                            lastArtifact = r
                            progress = 100
                            statusLine = if (r.ok) "OK: ${r.message}" else "FAIL: ${r.message}"
                            generating = false
                        }
                    }
                },
                enabled = !generating,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (generating) "Working…" else "Provider Call")
            }
            FilledTonalButton(
                onClick = {
                    scope.launch {
                        generating = true
                        statusLine = "Rendering locally (H.264)…"
                        progress = 0
                        val ts = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                        val file = File(ctx.getDir("output", 0), "clavgen-${ts}.mp4")
                        val ok = withContext(Dispatchers.Default) {
                            VideoGen.renderVideo(width = 540, height = 720, durationSec = duration, fps = 20, outPath = file.absolutePath)
                        }
                        progress = 100
                        if (ok) {
                            localPath = file.absolutePath
                            statusLine = "Local render complete → ${file.length() / 1024} KB"
                        } else {
                            statusLine = "Local render failed"
                        }
                        generating = false
                    }
                },
                enabled = !generating,
                modifier = Modifier.weight(1f)
            ) {
                Text("Local Render")
            }
        }
        if (generating && progress > 0) {
            LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
        }
        lastArtifact?.let { r ->
            Card(label = "Provider artifact") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (r.ok) "SUCCESS" else "ERROR",
                        color = if (r.ok) MintPrimary else SkySecondary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.weight(1f))
                    r.status?.let { Text("HTTP $it", style = MaterialTheme.typography.labelLarge) }
                }
                Text(r.message, style = MaterialTheme.typography.bodyMedium)
                r.artifactUrl?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = SkySecondary)
                }
            }
        }
        localPath?.let { path ->
            Card(label = "Local file") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Videocam, null, tint = MintPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(path, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ProviderPicker(
    providers: List<AiProvider>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        providers.forEachIndexed { idx, p ->
            FilterChip(
                selected = idx == selected,
                onClick = { onSelect(idx) },
                label = { Text(p.displayName) }
            )
        }
    }
}

@Composable
private fun TextRow(label: String, value: String, sub: String, tint: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, color = tint)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DurationRow(value: Int, onValueChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row {
            Text("Duration", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("${value}s", style = MaterialTheme.typography.bodyMedium, color = MintPrimary)
        }
        Slider(value = value.toFloat(), onValueChange = { onValueChange(it.toInt()) }, valueRange = 2f..10f, steps = 7)
    }
}
