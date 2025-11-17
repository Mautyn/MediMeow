package pl.edu.pk.student.feature_home.ui.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileSection(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: ProfileAction
)

sealed class ProfileAction {
    object ViewTestResults : ProfileAction()
    object ViewPrescriptions : ProfileAction()
    object ViewDoctorRecommendations : ProfileAction()
    object ChangePassword : ProfileAction()
}

object ProfileSections {
    val sections = listOf(
        ProfileSection(
            id = "test_results",
            title = "Test Results",
            description = "View your medical test results",
            icon = Icons.Default.Science,
            action = ProfileAction.ViewTestResults
        ),
        ProfileSection(
            id = "prescriptions",
            title = "Prescriptions",
            description = "Manage your prescriptions",
            icon = Icons.Default.Medication,
            action = ProfileAction.ViewPrescriptions
        ),
        ProfileSection(
            id = "recommendations",
            title = "Doctor's Recommendations",
            description = "View medical recommendations",
            icon = Icons.AutoMirrored.Filled.Assignment,
            action = ProfileAction.ViewDoctorRecommendations
        ),
        ProfileSection(
            id = "change_password",
            title = "Change Password",
            description = "Update your account password",
            icon = Icons.Default.Lock,
            action = ProfileAction.ChangePassword
        )
    )
}