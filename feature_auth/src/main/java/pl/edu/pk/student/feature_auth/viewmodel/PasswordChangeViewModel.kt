package pl.edu.pk.student.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.edu.pk.student.feature_auth.utils.OneTimeEvent
import java.util.regex.Pattern
import javax.inject.Inject

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val changePasswordError: OneTimeEvent<String>? = null,
    val changePasswordSuccess: OneTimeEvent<Unit>? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val passwordPattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                "(?=.*[!@#$%^&+=])" +
                "(?=\\S+$)" +
                ".{8,16}" +
                "$"
    )

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    fun onCurrentPasswordChanged(password: String) {
        _state.update { it.copy(currentPassword = password, currentPasswordError = null) }
    }

    fun onNewPasswordChanged(password: String) {
        _state.update { it.copy(newPassword = password, newPasswordError = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _state.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun changePassword() {
        val current = _state.value

        // Clear previous errors
        _state.update {
            it.copy(
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null
            )
        }

        // Validate current password
        if (current.currentPassword.isBlank()) {
            _state.update { it.copy(currentPasswordError = "Current password cannot be empty") }
            return
        }

        // Validate new password
        if (current.newPassword.isBlank()) {
            _state.update { it.copy(newPasswordError = "New password cannot be empty") }
            return
        }

        if (!passwordPattern.matcher(current.newPassword).matches()) {
            _state.update {
                it.copy(
                    newPasswordError = "Password must contain:\n" +
                            "• at least 1 digit\n" +
                            "• at least 1 lower case letter\n" +
                            "• at least 1 upper case letter\n" +
                            "• at least 1 special character (!@#$%^&+=)\n" +
                            "• no white spaces\n" +
                            "• between 8 and 16 characters"
                )
            }
            return
        }

        // Validate confirm password
        if (current.confirmPassword != current.newPassword) {
            _state.update { it.copy(confirmPasswordError = "Passwords do not match") }
            return
        }

        // Execute password change
        executePasswordChange()
    }

    private fun executePasswordChange() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        val user = firebaseAuth.currentUser
        if (user == null || user.email == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    changePasswordError = OneTimeEvent("User not logged in")
                )
            }
            return@launch
        }

        try {
            // Re-authenticate user
            val credential = EmailAuthProvider.getCredential(
                user.email!!,
                _state.value.currentPassword
            )
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(_state.value.newPassword).await()

            _state.update {
                it.copy(
                    isLoading = false,
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = "",
                    changePasswordSuccess = OneTimeEvent(Unit)
                )
            }
        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Current password is incorrect"
                is FirebaseAuthRecentLoginRequiredException -> "Please sign in again to change password"
                else -> e.message ?: "Failed to change password"
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    changePasswordError = OneTimeEvent(message)
                )
            }
        }
    }
}