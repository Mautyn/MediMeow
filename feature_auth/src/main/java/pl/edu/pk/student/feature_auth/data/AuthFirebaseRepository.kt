package pl.edu.pk.student.feature_auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthFirebaseRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthRepository.AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { AuthRepository.AuthResult.Success(it) }
                ?: AuthRepository.AuthResult.Error("Login succeeded but user is null.")
        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Incorrect password."
                is FirebaseAuthInvalidUserException -> "No account found with this email."
                else -> e.message ?: "Login failed."
            }
            AuthRepository.AuthResult.Error(message)
        }
    }

    override suspend fun register(email: String, password: String): AuthRepository.AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { AuthRepository.AuthResult.Success(it) }
                ?: AuthRepository.AuthResult.Error("Signup succeeded but user is null.")
        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthUserCollisionException -> "This email is already registered."
                else -> e.message ?: "Signup failed."
            }
            AuthRepository.AuthResult.Error(message)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}