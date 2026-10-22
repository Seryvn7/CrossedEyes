package com.crosseyes.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crosseyes.app.bypass.BypassScreen
import com.crosseyes.app.cam.CameraEmulatorScreen
import com.crosseyes.app.deepleave.DeepLiveScreen
import com.crosseyes.app.doc.DocumentGenScreen
import com.crosseyes.app.doc.InfoFormScreen
import com.crosseyes.app.face.FaceSwapScreen
import com.crosseyes.app.frame.FrameOptScreen
import com.crosseyes.app.loc.MockLocationScreen
import com.crosseyes.app.rep.ReportsScreen
import com.crosseyes.app.rtsp.SourceManagerScreen
import com.crosseyes.app.rtsp.PreviewScreen
import com.crosseyes.app.rtsp.MonitoringScreen
import com.crosseyes.app.stream.StreamSettingsScreen
import com.crosseyes.app.videogen.VideoGenScreen
import com.crosseyes.app.theme.CrossEyesTheme
import com.crosseyes.app.util.KeyVault

enum class Feature {
    CAMERA, DOCGEN, DEEPLIVE, VIDEOGEN, FRAMEOPT,
    MOCKLOC, SPOOF, BLOCK, FACE, BYPASS, SETTINGS,
    INFO_FORM, SOURCE_MGR, STREAM_SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vault = KeyVault(applicationContext)
        setContent {
            CrossEyesTheme {
                MainApp(vault)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(vault: KeyVault) {
    var current by remember { mutableStateOf<Feature?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    when (val f = current) {
        null -> {
            Column(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> HomeScreen(onNav = { current = it })
                    1 -> SourceManagerScreen(onBack = { selectedTab = 0 })
                    2 -> PreviewScreen(onBack = { selectedTab = 0 })
                    3 -> MonitoringScreen(onBack = { selectedTab = 0 })
                    4 -> SettingsScreen(vault = vault, onBack = { selectedTab = 0 })
                }
                Spacer(Modifier.height(8.dp))
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Folder, contentDescription = "Sources") },
                        label = { Text("Sources") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Visibility, contentDescription = "Preview") },
                        label = { Text("Preview") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Description, contentDescription = "Monitoring") },
                        label = { Text("Monitor") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 }
                    )
                }
            }
        }
        Feature.SETTINGS -> SettingsScreen(vault = vault, onBack = { current = null })
        Feature.CAMERA -> CameraEmulatorScreen(onBack = { current = null })
        Feature.DOCGEN -> DocumentGenScreen(onBack = { current = null })
        Feature.INFO_FORM -> InfoFormScreen(onBack = { current = null })
        Feature.DEEPLIVE -> DeepLiveScreen(onBack = { current = null })
        Feature.VIDEOGEN -> VideoGenScreen(onBack = { current = null }, vault = vault)
        Feature.FRAMEOPT -> FrameOptScreen(onBack = { current = null })
        Feature.MOCKLOC -> MockLocationScreen(onBack = { current = null })
        Feature.SPOOF, Feature.BLOCK -> ReportsScreen(f, onBack = { current = null })
        Feature.FACE -> FaceSwapScreen(onBack = { current = null }, vault = vault)
        Feature.BYPASS -> BypassScreen(onBack = { current = null })
        Feature.SOURCE_MGR -> SourceManagerScreen(onBack = { current = null })
        Feature.STREAM_SETTINGS -> StreamSettingsScreen(onBack = { current = null })
    }
}

@Composable
fun HomeScreen(onNav: (Feature) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Cross Eyes",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Multi-service AI toolkit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val tiles = listOf(
            Triple(Feature.CAMERA, Icons.Filled.Videocam, "Camera Emulator"),
            Triple(Feature.DOCGEN, Icons.Filled.Description, "Document Generator"),
            Triple(Feature.INFO_FORM, Icons.Filled.Person, "Info Form"),
            Triple(Feature.DEEPLIVE, Icons.Filled.Bolt, "Deep Live"),
            Triple(Feature.VIDEOGEN, Icons.Filled.Videocam, "Video Gen"),
            Triple(Feature.FRAMEOPT, Icons.Filled.Tune, "Frame Optimize"),
            Triple(Feature.MOCKLOC, Icons.Filled.LocationOn, "Mock Location"),
            Triple(Feature.SPOOF, Icons.Filled.SimCardAlert, "Spoof GPS"),
            Triple(Feature.BLOCK, Icons.Filled.Block, "Block GPS"),
            Triple(Feature.FACE, Icons.Filled.Face, "Face Swap"),
            Triple(Feature.BYPASS, Icons.Filled.LockOpen, "Bypass"),
            Triple(Feature.SOURCE_MGR, Icons.Filled.RssFeed, "RTSP Sources"),
            Triple(Feature.STREAM_SETTINGS, Icons.Filled.Cast, "Stream Output"),
            Triple(Feature.SETTINGS, Icons.Filled.Settings, "API Keys")
        )

        tiles.forEach { (feat, icon, title) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNav(feat) },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
