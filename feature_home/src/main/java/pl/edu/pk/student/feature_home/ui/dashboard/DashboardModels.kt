package pl.edu.pk.student.feature_home.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
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
    object SearchMedicine : DashboardAction()
    object CheckInteractions : DashboardAction()
    object AddMedicalRecord : DashboardAction()
    object SharePrescription : DashboardAction()
}

object DashboardTiles {
    val tiles = listOf(
        DashboardTile(
            id = "search_medicine",
            title = "Search Medicine",
            description = "Find medicine availability",
            icon = Icons.Default.Search,
            action = DashboardAction.SearchMedicine
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