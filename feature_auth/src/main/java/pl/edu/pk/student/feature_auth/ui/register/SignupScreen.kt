    package pl.edu.pk.student.feature_auth.ui.register

    import android.util.Log
    import android.widget.Toast
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.WindowInsets
    import androidx.compose.foundation.layout.displayCutout
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.heightIn
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.systemBars
    import androidx.compose.foundation.layout.widthIn
    import androidx.compose.foundation.layout.windowInsetsPadding
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.unit.dp
    import pl.edu.pk.student.core.ui.util.DeviceConfiguration
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.TextButton
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.platform.LocalContext
    import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import pl.edu.pk.student.core.R
    import pl.edu.pk.student.feature_auth.utils.consume
    import pl.edu.pk.student.feature_auth.viewmodel.AuthState
    import pl.edu.pk.student.feature_auth.viewmodel.AuthViewModel

    @Composable
    fun SignupScreen(
        viewModel: AuthViewModel = hiltViewModel(),
        onNavigateToLogin: () -> Unit
    ) {
        val state = viewModel.state.collectAsStateWithLifecycle()
        val context = LocalContext.current

        state.value.registerSuccess.consume {
            Toast.makeText(context, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
            onNavigateToLogin()
        }

        state.value.registerError.consume { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }


        SignupView(
            state = state.value,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onRegister = viewModel::registerUser,
        )
    }


    @Composable
    fun SignupView(
        state: AuthState = AuthState(),
        onEmailChanged: (String) -> Unit,
        onPasswordChanged: (String) -> Unit,
        onRegister: () -> Unit,
    ) {

        var showTermsDialog by remember { mutableStateOf(false) }
        var showPrivacyDialog by remember { mutableStateOf(false) }

        val handleTermsOfUseClick = {
            Log.d("RegisterScreen", "Terms of Use clicked! Showing dialog...")
            showTermsDialog = true
        }

        val handlePrivacyPolicyClick = {
            Log.d("RegisterScreen", "Privacy Policy clicked! Showing dialog...")
            showPrivacyDialog = true
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            val rootModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp
                    )
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    horizontal = 42.dp,
                    vertical = 36.dp
                )

            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
            when (deviceConfiguration) {
                DeviceConfiguration.MOBILE_PORTRAIT -> {
                    Column(
                        modifier = rootModifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SignupHeaderSection(
                            modifier = Modifier
                                .fillMaxWidth(),
                            alignment = Alignment.CenterHorizontally,
                            R.drawable.cat_image_03
                        )
                        SignupFormSection(
                            emailText = state.email,
                            onEmailTextChange = onEmailChanged,
                            emailError = state.emailError,
                            passwordText = state.password,
                            onPasswordTextChange = onPasswordChanged,
                            passwordError = state.passwordError,
                            onTermsOfUseClicked = handleTermsOfUseClick,
                            onPrivacyPolicyClicked = handlePrivacyPolicyClick,
                            onClick = onRegister,
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
                        SignupHeaderSection(
                            modifier = Modifier
                                .weight(1f),
                            alignment = Alignment.Start,
                            R.drawable.cat_image_03
                        )
                        SignupFormSection(
                            emailText = state.email,
                            onEmailTextChange = onEmailChanged,
                            emailError = state.emailError,
                            passwordText = state.password,
                            onPasswordTextChange = onPasswordChanged,
                            passwordError = state.passwordError,
                            onTermsOfUseClicked = handleTermsOfUseClick,
                            onPrivacyPolicyClicked = handlePrivacyPolicyClick,
                            onClick = onRegister,
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
                            .padding(top = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ) {
                        SignupHeaderSection(
                            modifier = Modifier
                                .widthIn(max = 400.dp)
                                .heightIn(max = 300.dp),
                            alignment = Alignment.CenterHorizontally,
                            R.drawable.cat_image_03
                        )
                        SignupFormSection(
                            emailText = state.email,
                            onEmailTextChange = onEmailChanged,
                            emailError = state.emailError,
                            passwordText = state.password,
                            onPasswordTextChange = onPasswordChanged,
                            passwordError = state.passwordError,
                            onTermsOfUseClicked = handleTermsOfUseClick,
                            onPrivacyPolicyClicked = handlePrivacyPolicyClick,
                            onClick = onRegister,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
            if (showTermsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsDialog = false },
                    title = { Text("Terms of Use") },
                    text = { Text("Here are the terms of use...") },
                    confirmButton = {
                        TextButton(onClick = { showTermsDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("Privacy Policy") },
                    text = { Text("Here is the privacy policy...") },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

