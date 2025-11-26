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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRecordsScreen(
    recordType: MedicalRecordType,
    records: List<MedicalRecord>,
    onBack: () -> Unit,
    onDeleteRecord: (MedicalRecord) -> Unit,
    onUpdateRecord: (MedicalRecord, String, String?) -> Unit,
) {
    var recordToDelete by remember { mutableStateOf<MedicalRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Manage ${recordType.title}",
                )},
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
        if (records.isEmpty()) {
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
                        record,
                        onDelete = { recordToDelete = record }
                    )
                }
            }
        }
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Record") },
            text = { Text("Are you sure you want to delete \"${record.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(record)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
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
private fun ManageRecordCard(record: MedicalRecord, onDelete: () -> Unit) {
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
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}