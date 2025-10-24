package pl.edu.pk.student.feature_auth.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.pk.student.core.ui.util.DeviceConfiguration
import pl.edu.pk.student.core.R
import pl.edu.pk.student.feature_auth.viewmodel.AuthState
import pl.edu.pk.student.feature_auth.viewmodel.AuthViewModel
import pl.edu.pk.student.feature_auth.utils.consume


@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToSignup: () -> Unit,
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    state.value.loginError.consume { errorMessage ->
        Toast.makeText(context, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
    }

    state.value.loginSuccess.consume {
        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
    }

    LoginView(
        state = state.value,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLogin = viewModel::loginUser,
        onNavigateToSignup = onNavigateToSignup ,
    )
}

@Composable
fun LoginView(
    state: AuthState = AuthState(),
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
){

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp
            ))
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = 42.dp,
                vertical = 36.dp
            )

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
        when(deviceConfiguration){
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LoginHeaderSection(
                        modifier = Modifier
                            .fillMaxWidth(),
                        alignment = Alignment.CenterHorizontally,
                        R.drawable.cat_image_03
                    )
                    LoginFormSection(
                        emailText = state.email,
                        onEmailTextChange = onEmailChanged,
                        emailError = state.emailError,
                        passwordText = state.password,
                        onPasswordTextChange = onPasswordChanged,
                        passwordError = state.passwordError,
                        onButtonClick = onLogin,
                        onLinkClick = onNavigateToSignup,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(
                            horizontal = 56.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    LoginHeaderSection(
                        modifier = Modifier
                            .weight(1f),
                        alignment = Alignment.Start,
                        R.drawable.cat_image_03
                    )
                    LoginFormSection(
                        emailText = state.email,
                        onEmailTextChange = onEmailChanged,
                        emailError = state.emailError,
                        passwordText = state.password,
                        onPasswordTextChange = onPasswordChanged,
                        passwordError = state.passwordError,
                        onButtonClick = onLogin,
                        onLinkClick = onNavigateToSignup,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column(
                    modifier = rootModifier
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginHeaderSection(
                        modifier = Modifier
                            .widthIn(max = 540.dp),
                        alignment = Alignment.CenterHorizontally,
                        R.drawable.cat_image_03
                    )
                    LoginFormSection(
                        emailText = state.email,
                        onEmailTextChange = onEmailChanged,
                        emailError = state.emailError,
                        passwordText = state.password,
                        onPasswordTextChange = onPasswordChanged,
                        passwordError = state.passwordError,
                        onButtonClick = onLogin,
                        onLinkClick = onNavigateToSignup,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}