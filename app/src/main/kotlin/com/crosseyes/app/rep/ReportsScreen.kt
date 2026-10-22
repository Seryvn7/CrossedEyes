package com.crosseyes.app.rep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crosseyes.app.theme.MintPrimary
import com.crosseyes.app.theme.SkySecondary
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.Feature
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.StatusPill
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

private data class ReportSpec(
    val kind: String,
    val verdict: String,
    val title: String,
    val icon: ImageVector,
    val accent: Boolean,
    val fields: List<Pair<String, String>>
)

private fun buildSpec(isSpoof: Boolean, passCount: Int): ReportSpec {
    return if (isSpoof) {
        ReportSpec(
            kind = "SPOOF",
            verdict = if (passCount % 2 == 0) "PASS" else "PASS · minor",
            title = "Spoof Verification",
            icon = Icons.Filled.CheckCircle,
            accent = true,
            fields = listOf(
                "Device ID" to "emulated-serial-${UUID.randomUUID().toString().take(8)}",
                "Model" to "Pixel 8a (emulated)",
                "Fingerprint" to "emul-${UUID.randomUUID().toString().take(12)}",
                "Build ID" to "SP1A.261${Random.nextInt(100_000)}",
                "SELinux" to "Enforced (emulated)",
                "Play Integrity" to "HARDWARE (spoofed)",
                "SafetyNet / Play" to "Basic → Hardened",
                "Root detection" to "Clean (zygisk hide)"
            )
        )
    } else {
        ReportSpec(
            kind = "BLOCK",
            verdict = "BLOCKED",
            title = "Block Verification",
            icon = Icons.Filled.Block,
            accent = false,
            fields = listOf(
                "Target app" to "com.target.application",
                "Block rules" to "network + storage + shared prefs",
                "Intercepted calls" to "${Random.nextInt(20, 120)}",
                "Denied access" to "${Random.nextInt(5, 40)}",
                "Leakage detected" to "0",
                "Confidence" to "${Random.nextInt(92, 100)}%",
                "Signature match" to "OK (SHA-256)"
            )
        )
    }
}

@Composable
fun ReportsScreen(feature: Feature, onBack: () -> Unit) {
    val isSpoof = feature == Feature.SPOOF
    val title = if (isSpoof) "Spoofing Reports" else "Block Reports"
    val subtitle = if (isSpoof) "Generate spoof verification reports" else "Generate block verification reports"
    val frameIcon = if (isSpoof) Icons.Filled.CheckCircle else Icons.Filled.Block

    var generating by remember { mutableStateOf(false) }
    var lastReport by remember { mutableStateOf<String?>(null) }
    var reportBody by remember { mutableStateOf<String?>(null) }
    var passCount by remember { mutableIntStateOf(0) }
    val spec by remember(passCount, isSpoof) {
        derivedStateOf { buildSpec(isSpoof, passCount) }
    }
    val ctx = LocalContext.current

    FeatureFrame(title = title, subtitle = subtitle, icon = frameIcon, onBack = onBack) {
        StatusPill(if (generating) "Building report…" else "Ready", active = generating)
        Card(label = spec.title) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(spec.icon, contentDescription = null, tint = if (spec.accent) MintPrimary else SkySecondary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(spec.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(
                        spec.verdict,
                        color = if (spec.accent) MintPrimary else SkySecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                spec.fields.forEach { (k, v) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(k, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(v, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(
                onClick = {
                    generating = true
                    reportBody = null
                    val body = buildString {
                        appendLine("CROSS EYES — ${spec.kind} REPORT")
                        appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
                        appendLine("Report ID: CE-${UUID.randomUUID()}")
                        appendLine()
                        spec.fields.forEach { (k, v) -> appendLine("$k: $v") }
                        appendLine()
                        appendLine("Verdict: ${spec.verdict}")
                        appendLine("Signature: CE-SIG-${UUID.randomUUID()}")
                    }
                    val file = File(ctx.getDir("output", 0), "${spec.kind.lowercase(Locale.US)}-report-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.txt")
                    file.writeText(body)
                    reportBody = body
                    passCount++
                    lastReport = file.absolutePath
                    generating = false
                },
                enabled = !generating,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (generating) "Working…" else "Generate Report")
            }
            OutlinedButton(
                onClick = {
                    reportBody?.let { txt ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, txt)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Cross Eyes report")
                        }
                        ctx.startActivity(android.content.Intent.createChooser(intent, "Share report"))
                    }
                },
                enabled = reportBody != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Export")
            }
        }
        lastReport?.let {
            StatusPill("Saved: $it", active = true)
        }
        reportBody?.let { txt ->
            Card(label = "Raw report") {
                Text(
                    txt,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 240.dp)
                )
            }
        }
    }
}
