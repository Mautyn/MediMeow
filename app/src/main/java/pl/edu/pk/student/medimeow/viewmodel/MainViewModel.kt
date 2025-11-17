package pl.edu.pk.student.medimeow.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.edu.pk.student.core.ui.data.ThemePreferences
import pl.edu.pk.student.feature_auth.data.AuthRepository
import pl.edu.pk.student.medimeow.AuthState
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                _authState.value = AuthState.Unauthenticated
            } else {
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}