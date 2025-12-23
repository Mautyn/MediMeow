package pl.edu.pk.student.medimeow

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import pl.edu.pk.student.feature_auth.ui.login.LoginScreen
import pl.edu.pk.student.feature_auth.ui.register.SignupScreen
import pl.edu.pk.student.feature_auth.ui.passwordChange.PasswordChangeScreen
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import pl.edu.pk.student.feature_medical_records.ui.ManageRecordsScreen
import pl.edu.pk.student.feature_medical_records.ui.MedicalRecordsMenuScreen
import pl.edu.pk.student.feature_medical_records.ui.MedicalRecordDetailScreen
import pl.edu.pk.student.feature_medical_records.ui.AddRecordScreen
import pl.edu.pk.student.feature_medical_records.ui.RecordDetailsScreen
import pl.edu.pk.student.feature_share.ui.ShareScreen
import pl.edu.pk.student.medimeow.navigation.MainScreen
import pl.edu.pk.student.medimeow.navigation.NavDestinations

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    authState: AuthState,
    onSignOut: () -> Unit
) {
    val startDestination = when (authState) {
        AuthState.Authenticated -> NavDestinations.Main.route
        else -> NavDestinations.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(400)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(400)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(400)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(400)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(400)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(400)
                    )
        }
    ) {
        composable(NavDestinations.Login.route) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(NavDestinations.Signup.route)
                }
            )
        }

        composable(NavDestinations.Signup.route) {
            SignupScreen(
                onNavigateToLogin = {
                    navController.navigate(NavDestinations.Login.route) {
                        popUpTo(NavDestinations.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(NavDestinations.Main.route) {
            MainScreen(
                onNavigateToMedicalRecords = {
                    navController.navigate(NavDestinations.MedicalRecordsMenu.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(NavDestinations.ChangePassword.route)
                },
                onNavigateToShare = {
                    navController.navigate(NavDestinations.Share.route)
                },
                onNavigateToInteractions = {
                    navController.navigate(NavDestinations.Interactions.route)
                },
                onSignOut = onSignOut
            )
        }

        composable(NavDestinations.MedicalRecordsMenu.route) {
            MedicalRecordsMenuScreen(
                onBack = { navController.popBackStack() },
                onRecordTypeSelected = { recordType ->
                    navController.navigate(
                        NavDestinations.MedicalRecordDetail.createRoute(recordType.name)
                    )
                }
            )
        }

        composable(
            route = NavDestinations.MedicalRecordDetail.route,
            arguments = listOf(navArgument("recordType") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            MedicalRecordDetailScreen(
                recordType = recordType,
                onBack = { navController.popBackStack() },
                onAddRecord = {
                    navController.navigate(NavDestinations.AddRecord.createRoute(recordType.name))
                },
                onManageRecords = {
                    navController.navigate(NavDestinations.ManageRecords.createRoute(recordType.name))
                },
                onRecordClick = { record ->
                    navController.navigate(
                        NavDestinations.RecordDetails.createRoute(record.id, recordType.name)
                    )
                }
            )
        }

        composable(
            route = NavDestinations.RecordDetails.route,
            arguments = listOf(
                navArgument("recordId") { type = NavType.StringType },
                navArgument("recordType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            val viewModel: pl.edu.pk.student.feature_medical_records.viewmodel.MedicalRecordsViewModel =
                androidx.hilt.navigation.compose.hiltViewModel()

            val recordFlow = remember(recordType, recordId) {
                viewModel.getRecordById(recordType, recordId)
            }

            val recordState by recordFlow.collectAsStateWithLifecycle(
                initialValue = null,
                minActiveState = androidx.lifecycle.Lifecycle.State.STARTED
            )

            when (val record = recordState) {
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Loading record...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                else -> {
                    androidx.compose.runtime.key(record.id) {
                        RecordDetailsScreen(
                            record = record,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        composable(
            route = NavDestinations.AddRecord.route,
            arguments = listOf(navArgument("recordType") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            AddRecordScreen(
                recordType = recordType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavDestinations.ManageRecords.route,
            arguments = listOf(navArgument("recordType") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            ManageRecordsScreen(
                recordType = recordType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.Share.route) {
            ShareScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ChangePassword.route) {
            PasswordChangeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.Interactions.route) {
            pl.edu.pk.student.feature_interactions.ui.screens.InteractionsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}