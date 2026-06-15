package org.example.project.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher =
    ImagePickerLauncher(supportsCamera = false, pickFromGallery = {}, takePhoto = {})

actual fun decodeImageBitmap(dataUrl: String): ImageBitmap? = null
