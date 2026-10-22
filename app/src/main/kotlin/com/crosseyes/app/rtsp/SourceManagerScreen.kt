package com.crosseyes.app.rtsp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagerScreen(onBack: () -> Unit) {
    var sources by remember { mutableStateOf(listOf(
        mapOf("name" to "Test RTSP", "url" to "rtsp://example.com/stream", "type" to "RTSP")
    )) }
    var result by remember { mutableStateOf<String?>(null) }

    FeatureFrame(title = "RTSP Source Manager", subtitle = "Configure stream sources", icon = Icons.Filled.RssFeed, onBack = onBack) {
        sources.forEach { s ->
            Card {
                Text(s["name"] as String, style = MaterialTheme.typography.titleMedium)
                Text(s["url"] as String, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StatusPill(s["type"] as String, active = true)
            }
        }

        OutlinedButton(onClick = { result = "Source configuration dialog" }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Source")
        }

        result?.let { StatusPill(it, active = true) }
    }
}
