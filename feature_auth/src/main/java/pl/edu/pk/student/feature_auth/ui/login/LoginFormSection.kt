package pl.edu.pk.student.feature_auth.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.feature_auth.ui.components.MediButton
import pl.edu.pk.student.feature_auth.ui.components.MediLink
import pl.edu.pk.student.feature_auth.ui.components.MediTextField


@Composable
fun LoginFormSection(
    emailText: String,
    onEmailTextChange: (String) -> Unit,
    emailError: String?,
    passwordText: String,
    onPasswordTextChange: (String) -> Unit,
    passwordError: String?,
    onButtonClick: () -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
)
{

    Column(
        modifier = modifier
            .padding(horizontal = 18.dp)
    ) {
        MediTextField(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = "Email",
            labelColor = MaterialTheme.colorScheme.onPrimary,
            hint = "Enter your email",
            isInputSecret = false,
            isError = emailError != null,
            errorMessage = emailError,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        MediTextField(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = "Password",
            labelColor = MaterialTheme.colorScheme.onPrimary,
            hint = "Enter your password",
            isInputSecret = true,
            isError = emailError != null,
            errorMessage = emailError,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        MediButton(
            text = "Log in",
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        MediLink(
            text = "Don't have an account? Sign up!",
            onClick = { onLinkClick() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }
}