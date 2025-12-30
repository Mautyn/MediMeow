package pl.edu.pk.student.feature_medical_records.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import pl.edu.pk.student.feature_medical_records.viewmodel.MedicalRecordsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRecordsScreen(
    recordType: MedicalRecordType,
    onBack: () -> Unit,
    viewModel: MedicalRecordsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var recordToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<MedicalRecord?>(null) }

    val records by viewModel.getRecordsFlow(recordType).collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Manage ${recordType.title}",
                    color = MaterialTheme.colorScheme.onPrimary
                ) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (isLoading && records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    recordType.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text("No records to manage")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records) { record ->
                    ManageRecordCard(
                        record = record,
                        onEdit = { recordToEdit = record },
                        onDelete = { recordToDelete = record }
                    )
                }
            }
        }
    }

    recordToEdit?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { recordToEdit = null },
            onSave = { newTitle, newContent ->
                viewModel.updateRecord(record, newTitle, newContent)
                recordToEdit = null
            }
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Record") },
            text = { Text("Are you sure you want to delete \"${record.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(record.id, record.type)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ManageRecordCard(
    record: MedicalRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (record.content != null) {
                    Text(
                        record.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    dateFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun EditRecordDialog(
    record: MedicalRecord,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var title by remember { mutableStateOf(record.title) }
    var content by remember { mutableStateOf(record.content ?: "") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (record.isTextRecord) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(title, if (record.isTextRecord) content else null)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}