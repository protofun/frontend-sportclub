package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// MainActivity is the entry point of the Android app.
// Every Android app has exactly one Activity as its starting screen.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

// @Preview lets Android Studio render a preview without launching the app.
@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
