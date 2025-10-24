package pl.edu.pk.student.feature_auth.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.edu.pk.student.feature_auth.data.AuthRepository
import pl.edu.pk.student.feature_auth.utils.OneTimeEvent
import java.util.regex.Pattern
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginError: OneTimeEvent<String>? = null,
    val loginSuccess: OneTimeEvent<Unit>? = null,
    val registerError: OneTimeEvent<String>? = null,
    val registerSuccess: OneTimeEvent<Unit>? = null
)


sealed class ValidationResult {
    object Success : ValidationResult()
    data class EmailError(val message: String) : ValidationResult()
    data class PasswordError(val message: String) : ValidationResult()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val passwordPattern = Pattern.compile(
        "^" +
                "(?=.*[0-9])" +                 // at least 1 digit
                "(?=.*[a-z])" +                 // at least 1 lower case letter
                "(?=.*[A-Z])" +                 // at least 1 upper case letter
                "(?=.*[a-zA-Z])" +              // any letter (optional, covered by a-z and A-Z)
                "(?=.*[!@#$%^&+=])" +           // at least 1 special character
                "(?=\\S+$)" +                   // no white spaces
                ".{8,16}" +                     // at least 8 characters and max 16 characters
                "$"
    )

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }


    fun loginUser() {
        when (val validation = validateEmailAndPassword()) {
            is ValidationResult.Success -> {
                executeLogin()
            }

            is ValidationResult.EmailError -> {
                _state.update {
                    it.copy(
                        emailError = validation.message,
                        passwordError = null,
                        isLoading = false
                    )
                }
            }

            is ValidationResult.PasswordError -> {
                _state.update {
                    it.copy(
                        passwordError = validation.message,
                        emailError = null,
                        isLoading = false
                    )
                }
            }
        }
    }


    fun registerUser() {
        when (val validation = validateEmailAndPassword()) {
            is ValidationResult.Success -> {
                executeRegister()
            }

            is ValidationResult.EmailError -> {
                _state.update {
                    it.copy(
                        emailError = validation.message,
                        passwordError = null,
                        isLoading = false
                    )
                }
            }

            is ValidationResult.PasswordError -> {
                _state.update {
                    it.copy(
                        passwordError = validation.message,
                        emailError = null,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun validateEmailAndPassword(): ValidationResult {
        val current = _state.value

        _state.update { it.copy(emailError = null, passwordError = null) }

        if (current.email.isBlank()) {
            return ValidationResult.EmailError("Email cannot be empty")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
            return ValidationResult.EmailError("Enter valid Email address!")
        }

        if (current.password.isBlank()) {
            return ValidationResult.PasswordError("Password cannot be empty")
        }

        if (!passwordPattern.matcher(current.password).matches()) {
            return ValidationResult.PasswordError(
                "Password must contain:\n" +
                        "• at least 1 digit\n" +
                        "• at least 1 lower case letter\n" +
                        "• at least 1 upper case letter\n" +
                        "• at least 1 special character (!@#$%^&+=)\n" +
                        "• no white spaces\n" +
                        "• between 8 and 16 characters"
            )
        }
        return ValidationResult.Success
    }


    private fun executeLogin() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, loginError = null) }

        when (val result = authRepository.login(_state.value.email, _state.value.password)) {
            is AuthRepository.AuthResult.Error -> {
                _state.update {
                    it.copy(isLoading = false, loginError = OneTimeEvent(result.message))
                }
            }

            is AuthRepository.AuthResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        emailError = null,
                        passwordError = null,
                        loginSuccess = OneTimeEvent(Unit)
                    )
                }
            }
        }
    }

    private fun executeRegister() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, registerError = null) }

        when (val result = authRepository.register(_state.value.email, _state.value.password)) {
            is AuthRepository.AuthResult.Error -> {
                _state.update {
                    it.copy(isLoading = false, registerError = OneTimeEvent(result.message))
                }
            }

            is AuthRepository.AuthResult.Success -> {
                authRepository.signOut()

                _state.update {
                    it.copy(
                        isLoading = false,
                        registerSuccess = OneTimeEvent(Unit),
                        emailError = null,
                        passwordError = null
                    )
                }
            }
        }
    }
}