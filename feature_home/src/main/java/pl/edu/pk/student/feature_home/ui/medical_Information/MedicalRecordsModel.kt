package pl.edu.pk.student.feature_home.ui.medical_Information

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.vector.ImageVector

enum class MedicalRecordType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    TEST_RESULTS(
        "Test Results",
        "Laboratory and diagnostic test results",
        Icons.Default.Science
    ),
    PRESCRIPTIONS(
        "Prescriptions",
        "Medical prescriptions from doctors",
        Icons.Default.Medication
    ),
    DOCTOR_RECOMMENDATIONS(
        "Doctor's Recommendations",
        "Medical advice and recommendations",
        Icons.Default.Assignment
    )
}

data class MedicalRecord(
    val id: String,
    val type: MedicalRecordType,
    val title: String,
    val content: String? = null, // null for images
    val imageUri: String? = null, // null for text
    val timestamp: Long = System.currentTimeMillis()
) {
    val isTextRecord: Boolean get() = content != null
    val isImageRecord: Boolean get() = imageUri != null
}

sealed class MedicalRecordAction {
    data class Add(val type: MedicalRecordType) : MedicalRecordAction()
    data class View(val type: MedicalRecordType) : MedicalRecordAction()
    data class Manage(val type: MedicalRecordType) : MedicalRecordAction()
}