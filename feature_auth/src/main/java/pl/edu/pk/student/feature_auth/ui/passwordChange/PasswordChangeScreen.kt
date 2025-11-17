package pl.edu.pk.student.feature_auth.ui.passwordChange

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.pk.student.feature_auth.ui.components.MediButton
import pl.edu.pk.student.feature_auth.ui.components.MediTextField
import pl.edu.pk.student.feature_auth.utils.consume
import pl.edu.pk.student.feature_auth.viewmodel.ChangePasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    state.value.changePasswordSuccess.consume {
        Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_LONG).show()
        onBack()
    }

    state.value.changePasswordError.consume { errorMessage ->
        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter your current password and choose a new one",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                MediTextField(
                    text = state.value.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChanged,
                    label = "Current Password",
                    labelColor = MaterialTheme.colorScheme.onBackground,
                    hint = "Enter current password",
                    isInputSecret = true,
                    isError = state.value.currentPasswordError != null,
                    errorMessage = state.value.currentPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                MediTextField(
                    text = state.value.newPassword,
                    onValueChange = viewModel::onNewPasswordChanged,
                    label = "New Password",
                    labelColor = MaterialTheme.colorScheme.onBackground,
                    hint = "Enter new password",
                    isInputSecret = true,
                    isError = state.value.newPasswordError != null,
                    errorMessage = state.value.newPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                MediTextField(
                    text = state.value.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    label = "Confirm New Password",
                    labelColor = MaterialTheme.colorScheme.onBackground,
                    hint = "Re-enter new password",
                    isInputSecret = true,
                    isError = state.value.confirmPasswordError != null,
                    errorMessage = state.value.confirmPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                MediButton(
                    text = if (state.value.isLoading) "Changing..." else "Change Password",
                    onClick = { viewModel.changePassword() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}