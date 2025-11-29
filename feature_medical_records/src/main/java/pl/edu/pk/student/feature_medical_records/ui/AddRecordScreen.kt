package pl.edu.pk.student.feature_medical_records.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import pl.edu.pk.student.feature_medical_records.viewmodel.MedicalRecordsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    recordType: MedicalRecordType,
    onBack: () -> Unit,
    viewModel: MedicalRecordsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var base64Image by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUploadingImage by viewModel.isUploadingImage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Konwertuj do Base64 immediately
            scope.launch {
                viewModel.convertImageToBase64(uri).fold(
                    onSuccess = { base64 ->
                        base64Image = base64
                        Toast.makeText(context, "Image processed successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Failed to process image: ${error.message}", Toast.LENGTH_LONG).show()
                        selectedImageUri = null
                    }
                )
            }
        }
    }

    // Obsługa sukcesu
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            onBack()
        }
    }

    // Obsługa błędów
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add ${recordType.title}") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isLoading && !isUploadingImage) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Enter a title for this record") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !isUploadingImage,
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    placeholder = { Text("Enter detailed information") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !isUploadingImage,
                    maxLines = 10
                )

                // Image selection section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Attach Image (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (selectedImageUri != null) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // Wyświetl z URI (przed konwersją) lub z Base64 (po konwersji)
                                if (base64Image != null) {
                                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Selected image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    // Pokazuj oryginalny URI podczas przetwarzania
                                    coil.compose.AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Remove button
                                IconButton(
                                    onClick = {
                                        selectedImageUri = null
                                        base64Image = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(50)
                                        )
                                        .size(32.dp),
                                    enabled = !isUploadingImage
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Upload progress
                                if (isUploadingImage) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && !isUploadingImage
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, "Add image")
                                Spacer(Modifier.width(8.dp))
                                Text("Select Image")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        when {
                            title.isBlank() -> {
                                Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            }
                            content.isBlank() && base64Image == null -> {
                                Toast.makeText(context, "Please enter content or add an image", Toast.LENGTH_SHORT).show()
                            }
                            isUploadingImage -> {
                                Toast.makeText(context, "Please wait for image processing to complete", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                viewModel.addRecord(
                                    type = recordType,
                                    title = title,
                                    content = content.ifBlank { null },
                                    imageUri = base64Image
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && !isUploadingImage
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "Saving..." else "Save Record")
                }
            }
        }
    }
}