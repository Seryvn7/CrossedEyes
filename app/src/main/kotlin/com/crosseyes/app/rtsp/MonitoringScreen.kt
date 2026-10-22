package com.crosseyes.app.rtsp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Bolt

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill

@Composable
fun MonitoringScreen(onBack: () -> Unit) {
    FeatureFrame(title = "Monitoring", subtitle = "Stream health and metrics", icon = Icons.Filled.Shield, onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "FPS", value = "—")
            MetricTile(label = "Resolution", value = "—")
            MetricTile(label = "Latency", value = "—", accent = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "Dropped", value = "—")
            MetricTile(label = "Uptime", value = "—")
        }
        Card {
            Text("Stream metrics will appear here when a source is connected.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StatusPill("No active stream", active = false)
    }
}
