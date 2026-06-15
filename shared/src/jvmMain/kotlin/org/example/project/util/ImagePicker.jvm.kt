package org.example.project.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    return ImagePickerLauncher(
        supportsCamera = false,
        pickFromGallery = {
            val dialog = FileDialog(Frame(), "Select profile photo", FileDialog.LOAD)
            dialog.setFile("*.jpg;*.jpeg;*.png")
            dialog.isVisible = true
            val fileName = dialog.file
            val dir = dialog.directory
            if (fileName != null && dir != null) {
                val bytes = File(dir, fileName).readBytes()
                val mime = if (fileName.lowercase().endsWith(".png")) "image/png" else "image/jpeg"
                onImagePicked("data:$mime;base64,${Base64.encode(bytes)}")
            }
        },
        takePhoto = {}
    )
}

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeImageBitmap(dataUrl: String): ImageBitmap? = try {
    val bytes = Base64.decode(dataUrl.substringAfter("base64,", dataUrl))
    Image.makeFromEncoded(bytes).toComposeImageBitmap()
} catch (e: Exception) {
    null
}
