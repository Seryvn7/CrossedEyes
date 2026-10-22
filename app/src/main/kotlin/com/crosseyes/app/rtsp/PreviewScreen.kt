package com.crosseyes.app.rtsp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill

@Composable
fun PreviewScreen(onBack: () -> Unit) {
    FeatureFrame(title = "Preview", subtitle = "Live video feed preview", icon = Icons.Default.Visibility, onBack = onBack) {
        Card {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f),
                contentAlignment = Alignment.Center
            ) {
                Text("No stream active", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "Source", value = "—")
            MetricTile(label = "Status", value = "Ready", accent = true)
        }
        StatusPill("Connect a source to begin", active = false)
    }
}
