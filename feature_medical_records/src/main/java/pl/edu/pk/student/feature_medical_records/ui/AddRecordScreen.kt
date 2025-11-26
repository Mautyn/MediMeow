package pl.edu.pk.student.feature_medical_records.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    recordType: MedicalRecordType,
    onBack: () -> Unit,
    onSave: (title: String, content: String?, imageUri: String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add ${recordType.title}") },
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            text = "Title",
                            color = MaterialTheme.colorScheme.onPrimary
                        ) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(
                            text = "Content",
                            color = MaterialTheme.colorScheme.onPrimary
                        ) },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary

                    )
                )

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                        } else if (content.isBlank()) {
                            Toast.makeText(context, "Please enter content", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(title, content, null)
                            Toast.makeText(context, "Record saved!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Record",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}