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

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        // The URL hash (#/app) determines which mode to show:
        //   #/app   → App (the member/instructor app, shared with Android)
        var showMemberApp by remember {
            mutableStateOf(window.location.hash.startsWith("#/app"))
        }

        // Listen for hash changes while the page is open.
        DisposableEffect(Unit) {
            val listener: (Event) -> Unit = {
                showMemberApp = window.location.hash.startsWith("#/app")
            }
            window.addEventListener("hashchange", listener)
            onDispose { window.removeEventListener("hashchange", listener) }
        }

        if (showMemberApp) {
            // Member app mode: show a back button + the shared App() composable
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // By clicking go back to the website
                    TextButton(onClick = { window.location.hash = "" }) {
                        Text("← Back to SportClub website", fontSize = 13.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    // Composable app
                    App()
                }
            }
        } else {
            // Public website for everyone
            SportClubWebApp()
        }
    }
}
