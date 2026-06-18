package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.window
import org.w3c.dom.events.Event

/** Browser entry point. Routes between the staff portal ([SportClubWebApp]) and the
 *  member/instructor app ([App]) based on the URL hash, so the same app that runs on
 *  Android and Desktop is also reachable in the browser at "#/app". */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        var showMemberApp by remember { mutableStateOf(window.location.hash.startsWith("#/app")) }

        DisposableEffect(Unit) {
            val listener: (Event) -> Unit = {
                showMemberApp = window.location.hash.startsWith("#/app")
            }
            window.addEventListener("hashchange", listener)
            onDispose { window.removeEventListener("hashchange", listener) }
        }

        if (showMemberApp) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { window.location.hash = "" }) {
                        Text("← Back to SportClub website", fontSize = 13.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    App()
                }
            }
        } else {
            SportClubWebApp()
        }
    }
}
