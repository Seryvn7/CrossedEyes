package com.crosseyes.app.stream

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.MetricTile
import com.crosseyes.app.ui.StatusPill

@Composable
fun StreamSettingsScreen(onBack: () -> Unit) {
    FeatureFrame(title = "Stream Output", subtitle = "RTMP/HTTP/RTSP broadcast", icon = Icons.Filled.Cast, onBack = onBack) {
        Card(label = "Stream URL") {
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("rtmp://...") }, modifier = Modifier.fillMaxWidth())
        }
        Card(label = "Protocol") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("RTMP", "HTTP", "RTSP").forEach { p ->
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text(p)
                    }
                }
            }
        }
        Card {
            Row {
                OutlinedButton(onClick = {}) {
                    Text("Start Streaming")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(label = "Viewers", value = "0")
            MetricTile(label = "Bitrate", value = "—", accent = true)
        }
    }
}
