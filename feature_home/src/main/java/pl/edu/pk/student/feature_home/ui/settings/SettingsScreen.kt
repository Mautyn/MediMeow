package pl.edu.pk.student.feature_home.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.feature_home.ui.settings.components.SettingNavigationItem
import pl.edu.pk.student.feature_home.ui.settings.components.SettingToggleItem

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit
) {
    val context = LocalContext.current

    // State for toggle settings
    var isDarkModeEnabled by remember { mutableStateOf(false) }
    var areNotificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App Settings Section
            Text(
                text = "App Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            SettingsConfig.appSettings.forEach { setting ->
                when (setting) {
                    is SettingItem.Toggle -> {
                        val isEnabled = when (setting.id) {
                            "dark_mode" -> isDarkModeEnabled
                            "notifications" -> areNotificationsEnabled
                            else -> setting.isEnabled
                        }

                        SettingToggleItem(
                            setting = setting.copy(isEnabled = isEnabled),
                            onToggle = { newValue ->
                                when (setting.id) {
                                    "dark_mode" -> {
                                        isDarkModeEnabled = newValue
                                        Toast.makeText(
                                            context,
                                            "Dark mode: ${if (newValue) "Enabled" else "Disabled"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    "notifications" -> {
                                        areNotificationsEnabled = newValue
                                        Toast.makeText(
                                            context,
                                            "Notifications: ${if (newValue) "Enabled" else "Disabled"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                    is SettingItem.Navigation -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // General Settings Section
            Text(
                text = "General",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            SettingsConfig.generalSettings.forEach { setting ->
                when (setting) {
                    is SettingItem.Navigation -> {
                        SettingNavigationItem(
                            setting = setting,
                            onClick = {
                                when (setting.action) {
                                    SettingAction.ChangeLanguage -> {
                                        Toast.makeText(
                                            context,
                                            "Language settings - Coming soon!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    SettingAction.PrivacyPolicy -> {
                                        Toast.makeText(
                                            context,
                                            "Privacy Policy - Coming soon!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    SettingAction.About -> {
                                        Toast.makeText(
                                            context,
                                            "MediMeow v1.0 - Meow your way to better health!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                    is SettingItem.Toggle -> {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Version
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}