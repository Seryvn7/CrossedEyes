package com.crosseyes.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.crosseyes.app.providers.ALL_PROVIDERS
import com.crosseyes.app.providers.ProviderCategory
import com.crosseyes.app.util.KeyVault
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vault: KeyVault, onBack: () -> Unit) {
    val providers = remember { ALL_PROVIDERS }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    FeatureFrame(
        title = "API Keys & Providers",
        subtitle = "Keys are stored locally in an AES-encrypted vault",
        icon = Icons.Filled.Key,
        onBack = onBack
    ) {
        Text(
            "Enter API keys to unlock each integrated provider. Keys are stored locally on this device in an AES-encrypted vault — they are never uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val cats = listOf(ProviderCategory.IMAGE, ProviderCategory.VIDEO, ProviderCategory.TEXT)
        cats.forEach { cat ->
            val list = providers.filter { it.category == cat }
            Card(label = cat.name) {
                list.forEach { p ->
                    KeyFieldRow(vault = vault, p = p)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    testing = true
                    testResult = null
                    scope.launch {
                        val count = ALL_PROVIDERS.count { !vault.get(it.keyField).isNullOrEmpty() }
                        delay(400)
                        testResult = if (count == 0) "No keys set — all providers run in offline fast-path mode."
                        else "$count of ${ALL_PROVIDERS.size} providers have keys configured."
                        testing = false
                    }
                },
                enabled = !testing
            ) {
                Text(if (testing) "Testing…" else "Test Keys")
            }
            testResult?.let { StatusPill(it, active = true) }
        }
    }
}

@Composable
private fun KeyFieldRow(vault: KeyVault, p: com.crosseyes.app.providers.AiProvider) {
    var visible by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf(vault.get(p.keyField) ?: "") }
    var dirty by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { v ->
                value = v
                dirty = true
                scope.launch {
                    kotlinx.coroutines.delay(350)
                    if (dirty) {
                        vault.put(p.keyField, value)
                        dirty = false
                    }
                }
            },
            label = { Text(p.displayName) },
            placeholder = { Text("paste ${p.keyPrefix}…") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            isError = value.isNotEmpty() && !value.startsWith(p.keyPrefix)
        )
        if (value.isNotEmpty() && !value.startsWith(p.keyPrefix)) {
            Text("Key is expected to start with “${p.keyPrefix}”", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error)
        }
    }
}
