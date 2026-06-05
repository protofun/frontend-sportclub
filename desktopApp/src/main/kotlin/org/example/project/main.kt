package org.example.project

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(420.dp, 820.dp))
    Window(
        onCloseRequest = ::exitApplication,
        title = "SportClub",
        state = windowState
    ) {
        App()
    }
}
