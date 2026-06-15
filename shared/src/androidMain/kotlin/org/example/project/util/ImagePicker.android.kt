package org.example.project.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val MAX_DIMENSION = 512

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.let { bitmap -> onImagePicked(bitmap.toDataUrl()) }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { onImagePicked(it.toDataUrl()) }
    }

    return ImagePickerLauncher(
        supportsCamera = true,
        pickFromGallery = { galleryLauncher.launch("image/*") },
        takePhoto = { cameraLauncher.launch(null) }
    )
}

@OptIn(ExperimentalEncodingApi::class)
private fun Bitmap.toDataUrl(): String {
    val scaled = if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
        val ratio = minOf(MAX_DIMENSION.toFloat() / width, MAX_DIMENSION.toFloat() / height)
        Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    } else this
    val stream = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return "data:image/jpeg;base64,${Base64.encode(stream.toByteArray())}"
}

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeImageBitmap(dataUrl: String): ImageBitmap? = try {
    val bytes = Base64.decode(dataUrl.substringAfter("base64,", dataUrl))
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
} catch (e: Exception) {
    null
}
