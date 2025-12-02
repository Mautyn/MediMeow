package pl.edu.pk.student.feature_medical_records.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface ImageLoadState {
    data object Loading : ImageLoadState
    data object Success : ImageLoadState
    data object Error : ImageLoadState
}

@Composable
fun Base64Image(
    base64String: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showLoadingIndicator: Boolean = true,
    showErrorMessage: Boolean = true
) {
    val imageKey = remember(base64String) { base64String.hashCode() }

    var bitmap by remember(imageKey) { mutableStateOf<Bitmap?>(null) }
    var loadState by remember(imageKey) { mutableStateOf<ImageLoadState>(ImageLoadState.Loading) }

    LaunchedEffect(imageKey) {
        if (bitmap == null) {
            loadState = ImageLoadState.Loading

            try {
                val decodedBitmap = withContext(Dispatchers.IO) {
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                }

                if (decodedBitmap != null) {
                    bitmap = decodedBitmap
                    loadState = ImageLoadState.Success
                } else {
                    loadState = ImageLoadState.Error
                }
            } catch (e: Exception) {
                loadState = ImageLoadState.Error
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (loadState) {
            ImageLoadState.Loading -> {
                if (showLoadingIndicator) {
                    CircularProgressIndicator()
                }
            }
            ImageLoadState.Error -> {
                if (showErrorMessage) {
                    Text(
                        "Failed to load image",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            ImageLoadState.Success -> {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = contentDescription,
                        modifier = modifier,
                        contentScale = contentScale
                    )
                }
            }
        }
    }
}