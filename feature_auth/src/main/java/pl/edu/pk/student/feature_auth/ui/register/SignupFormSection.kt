package pl.edu.pk.student.feature_auth.ui.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.feature_auth.ui.components.MediButton
import pl.edu.pk.student.feature_auth.ui.components.MediCheckBox
import pl.edu.pk.student.feature_auth.ui.components.MediLink
import pl.edu.pk.student.feature_auth.ui.components.MediTextField

@Composable
fun SignupFormSection(
    emailText: String,
    onEmailTextChange: (String) -> Unit,
    emailError: String?,
    passwordText: String,
    onPasswordTextChange: (String) -> Unit,
    passwordError: String?,
    onTermsOfUseClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
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
            isError = passwordError != null,
            errorMessage = passwordError,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        var isAgreementChecked by remember { mutableStateOf(false) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            MediCheckBox(
                checked = isAgreementChecked,
                onCheckedChange = { newState -> isAgreementChecked = newState },
            )
            FlowRow(modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)) {
                Text(
                    text = "Accept ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                MediLink(onClick = onTermsOfUseClicked, text = "terms of use")
                Text(
                    text = " and ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                MediLink(onClick = onPrivacyPolicyClicked, text = "private policy")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MediButton(
            text = "Continue",
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        )
    }
}