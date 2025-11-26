package pl.edu.pk.student.feature_settings.ui


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SettingItem {
    data class Toggle(
        val id: String,
        val title: String,
        val description: String,
        val icon: ImageVector,
        val isEnabled: Boolean = false
    ) : SettingItem()

    data class Navigation(
        val id: String,
        val title: String,
        val description: String,
        val icon: ImageVector,
        val action: SettingAction
    ) : SettingItem()
}

sealed class SettingAction {
    object ChangeLanguage : SettingAction()
    object PrivacyPolicy : SettingAction()
    object About : SettingAction()
}

object SettingsConfig {
    val appSettings = listOf(
        SettingItem.Toggle(
            id = "dark_mode",
            title = "Dark Mode",
            description = "Enable dark theme",
            icon = Icons.Default.DarkMode,
            isEnabled = false
        ),
        SettingItem.Toggle(
            id = "notifications",
            title = "Notifications",
            description = "Receive medication reminders",
            icon = Icons.Default.Notifications,
            isEnabled = true
        )
    )

    val generalSettings = listOf(
        SettingItem.Navigation(
            id = "language",
            title = "Language",
            description = "Change app language",
            icon = Icons.Default.Language,
            action = SettingAction.ChangeLanguage
        ),
        SettingItem.Navigation(
            id = "privacy",
            title = "Privacy Policy",
            description = "View privacy policy",
            icon = Icons.Default.Security,
            action = SettingAction.PrivacyPolicy
        ),
        SettingItem.Navigation(
            id = "about",
            title = "About",
            description = "App information and version",
            icon = Icons.Default.Info,
            action = SettingAction.About
        )
    )
}
