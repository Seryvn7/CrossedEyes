package com.crosseyes.app.doc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.crosseyes.app.ui.Card
import com.crosseyes.app.ui.FeatureFrame
import com.crosseyes.app.ui.StatusPill

data class PersonInfo(
    var firstName: String = "",
    var middleName: String = "",
    var lastName: String = "",
    var streetAddress: String = "",
    var city: String = "",
    var state: String = "",
    var zipCode: String = "",
    var hairColor: String = "",
    var eyeColor: String = "",
    var height: String = "",
    var weight: String = "",
    var issueDate: String = "",
    var expiryDate: String = "",
    var noIssueDate: Boolean = false,
    var noExpiryDate: Boolean = false
)

@Composable
fun InfoFormScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var info = remember { PersonInfo() }
    var saved by remember { mutableStateOf(false) }

    FeatureFrame(
        title = "Personal Info Form",
        subtitle = "Fill out once, use across all identity documents",
        icon = Icons.Filled.Person
    ) {
        Card(label = "Name") {
            TextField(
                value = info.firstName,
                onValueChange = { info = info.copy(firstName = it) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = info.middleName,
                    onValueChange = { info = info.copy(middleName = it) },
                    label = { Text("Middle Name") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = info.lastName,
                    onValueChange = { info = info.copy(lastName = it) },
                    label = { Text("Last Name") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Card(label = "Address") {
            TextField(
                value = info.streetAddress,
                onValueChange = { info = info.copy(streetAddress = it) },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = info.city,
                    onValueChange = { info = info.copy(city = it) },
                    label = { Text("City") },
                    modifier = Modifier.weight(2f)
                )
                TextField(
                    value = info.state,
                    onValueChange = { info = info.copy(state = it) },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = info.zipCode,
                    onValueChange = { info = info.copy(zipCode = it) },
                    label = { Text("ZIP") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Card(label = "Physical Description", modifier = Modifier.height(60.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = info.hairColor,
                    onValueChange = { info = info.copy(hairColor = it) },
                    label = { Text("Hair Color") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = info.eyeColor,
                    onValueChange = { info = info.copy(eyeColor = it) },
                    label = { Text("Eye Color") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = info.height,
                    onValueChange = { info = info.copy(height = it) },
                    label = { Text("Height") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 5'10\"") }
                )
                TextField(
                    value = info.weight,
                    onValueChange = { info = info.copy(weight = it) },
                    label = { Text("Weight") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. 170 lbs") }
                )
            }
        }

        Card(label = "Document Dates") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = info.noIssueDate,
                    onCheckedChange = { info = info.copy(noIssueDate = it) }
                )
                Text("No Issue Date (leave blank)")
            }
            TextField(
                value = info.issueDate,
                onValueChange = { info = info.copy(issueDate = it) },
                label = { Text("Issue Date") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("MM/DD/YYYY") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = info.noExpiryDate,
                    onCheckedChange = { info = info.copy(noExpiryDate = it) }
                )
                Text("No Expiry Date (leave blank)")
            }
            TextField(
                value = info.expiryDate,
                onValueChange = { info = info.copy(expiryDate = it) },
                label = { Text("Expiry Date") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("MM/DD/YYYY") }
            )
        }

        Row {
            FilledTonalButton(
                onClick = {
                    val prefs = ctx.getSharedPreferences("person_info", 0).edit()
                    prefs.putString("first_name", info.firstName)
                    prefs.putString("middle_name", info.middleName)
                    prefs.putString("last_name", info.lastName)
                    prefs.putString("street", info.streetAddress)
                    prefs.putString("city", info.city)
                    prefs.putString("state", info.state)
                    prefs.putString("zip", info.zipCode)
                    prefs.putString("hair", info.hairColor)
                    prefs.putString("eyes", info.eyeColor)
                    prefs.putString("height", info.height)
                    prefs.putString("weight", info.weight)
                    prefs.putString("issue", info.issueDate)
                    prefs.putString("expiry", info.expiryDate)
                    prefs.putBoolean("no_issue", info.noIssueDate)
                    prefs.putBoolean("no_expiry", info.noExpiryDate)
                    prefs.apply()
                    saved = true
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Info")
            }
        }

        if (saved) {
            StatusPill("Personal info saved", active = true)
        }
    }
}
