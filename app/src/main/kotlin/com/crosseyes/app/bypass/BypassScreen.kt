package com.crosseyes.app.bypass

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crosseyes.app.theme.MintPrimary
import com.crosseyes.app.theme.SkySecondary
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.StatusPill

private data class BypassMethod(
    val name: String,
    val desc: String,
    val tag: String,
    val updated: Boolean
)

private val METHODS = listOf(
    BypassMethod(
        "Magisk + Zygisk",
        "Hides root from Play Integrity & SafetyNet via zygisk fork. Keeps LSPosed for module hooks.",
        "root", updated = true
    ),
    BypassMethod(
        "Play Integrity hard mask",
        "Forces HARDWARE attestation + green check by masking device fingerprint + build props.",
        "integrity", updated = true
    ),
    BypassMethod(
        "TLS pinning hook",
        "Rebuilds OkHttp/Retrofit cert chain via LSPosed. Clears app-pinned CAs.",
        "network", updated = false
    ),
    BypassMethod(
        "Shared prefs tamper",
        "Writes a clean prefs file to /data/data before app launch (runs in Zygisk service).",
        "storage", updated = false
    ),
    BypassMethod(
        "Camera sensor spoof",
        "Emulates 3 sensors + gyroscope on a fake IMU to pass anti-bot motion heuristics.",
        "sensor", updated = true
    ),
    BypassMethod(
        "Frida hide",
        "Frida server + Gadget. Hides frida thread names + /proc fd links from target.",
        "debug", updated = false
    ),
    BypassMethod(
        "Play app ID re-sign",
        "Re-signs the market APK with device debug key for local install + integrity mask.",
        "package", updated = false
    ),
    BypassMethod(
        "Boot loop guard",
        "Puts device into a known-good state if a module fails to load.",
        "boot", updated = false
    )
)

private data class HowToStep(val title: String, val body: String)

private val STEPS = listOf(
    HowToStep("Prepare Magisk + Zygisk", "Install Magisk + Zygisk on top-line device. Enable Zygisk in settings. Reboot."),
    HowToStep("Deploy fingerprint module", "Install 'CrossEyes Fingerprint Mask' LSPosed module. Grant it to target app. Reboot."),
    HowToStep("Apply TLS hook", "Install 'CrossEyes TLS Unpin' module on LSPosed. Scope to target app. Reboot."),
    HowToStep("Reboot + verify", "Use the Spoof/Block feature to verify the masks apply. Keep a report for support.")
)

private data class Prompt(val title: String, val text: String)

private val PROMPTS = listOf(
    Prompt(
        "Support — root mask",
        """Hello, my Play Integrity is showing BASIC only. I have Magisk+Zygisk with the CrossEyes fingerprint
module enabled on my Pixel 8a. Attached is a full spoof report ID CE-SIG-… . Could you confirm the mask
is applied at the Zygisk layer? Thanks!"""
    ),
    Prompt(
        "Support — TLS / app not loading",
        """The target app keeps throwing a network error (SSL certificate) even after a clean install.
I have TLS Unpin scoped to the app. Here is my Block report: CE-SIG-… . Please help."""
    ),
    Prompt(
        "Support — face swap low similarity",
        """Face swap on my selfie shows ~78% similarity. I used 50% blend + 40% smoothing.
Is this in the range the model expects, or should I re-shoot with better lighting?
Report: CE-SIG-…"""
    )
)

@Composable
fun BypassScreen(onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current

    FeatureFrame(title = "Bypass Suite", subtitle = "Bypass package + prompts + docs", icon = Icons.Filled.Shield, onBack = onBack) {
        Card(label = "Bypass Methods Package") {
            Text(
                "v2.3.1 — 8 active methods · 3 updated since v2.2",
                style = MaterialTheme.typography.labelLarge,
                color = SkySecondary
            )
            METHODS.forEach { m ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(m.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            if (m.updated) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "updated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintPrimary
                                )
                            }
                        }
                        Text(m.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        m.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = SkySecondary
                    )
                }
            }
        }
        Card(label = "How to") {
            STEPS.forEachIndexed { i, s ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Text(
                        "${i + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MintPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(s.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Card(label = "Ready-to-use prompts") {
            PROMPTS.forEach { p ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            p.text.replace("…", "  ").take(120),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(p.text))
                        }
                    ) {
                        Text("Copy")
                    }
                }
            }
        }
    }
}
