package pl.edu.pk.student.feature_dashboard.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

data class DashboardTile(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: DashboardAction
)

sealed class DashboardAction {
    object CheckInteractions : DashboardAction()
    object AddMedicalRecord : DashboardAction()
    object SharePrescription : DashboardAction()
    object MedicineAvailability : DashboardAction()
}

object DashboardTiles {
    val tiles = listOf(
        DashboardTile(
            id = "medicine_availability",
            title = "Medicine Availability",
            description = "Check medicine availability in nearby pharmacies",
            icon = Icons.Default.LocalPharmacy,
            action = DashboardAction.MedicineAvailability
        ),
        DashboardTile(
            id = "check_interactions",
            title = "Drug Interactions",
            description = "Check if medicines are safe together",
            icon = Icons.Default.Medication,
            action = DashboardAction.CheckInteractions
        ),
        DashboardTile(
            id = "add_record",
            title = "Medical Records",
            description = "Add prescriptions, test results, recommendations",
            icon = Icons.AutoMirrored.Filled.Assignment,
            action = DashboardAction.AddMedicalRecord
        ),
        DashboardTile(
            id = "share",
            title = "Share",
            description = "Share prescriptions & recommendations",
            icon = Icons.Default.Share,
            action = DashboardAction.SharePrescription
        )
    )
}