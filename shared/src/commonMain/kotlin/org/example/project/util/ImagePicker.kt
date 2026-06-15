package org.example.project.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Result of [rememberImagePicker]. [pickFromGallery] and [takePhoto] trigger the platform's
 * native picker; the picked image is delivered as a data URL (e.g. "data:image/jpeg;base64,...")
 * to the `onImagePicked` callback. [supportsCamera] indicates whether [takePhoto] is available
 * on this platform.
 */
class ImagePickerLauncher(
    val supportsCamera: Boolean,
    val pickFromGallery: () -> Unit,
    val takePhoto: () -> Unit
)

@Composable
expect fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher

/** Decodes a "data:image/...;base64,..." (or raw base64) string into a bitmap, or null if unsupported/invalid. */
expect fun decodeImageBitmap(dataUrl: String): ImageBitmap?
