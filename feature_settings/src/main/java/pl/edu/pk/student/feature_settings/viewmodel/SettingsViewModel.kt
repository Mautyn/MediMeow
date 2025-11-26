package pl.edu.pk.student.feature_settings.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import pl.edu.pk.student.core.ui.data.ThemePreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode

    fun setDarkMode(enabled: Boolean) {
        themePreferences.setDarkMode(enabled)
    }
}