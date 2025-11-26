package pl.edu.pk.student.medimeow

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import pl.edu.pk.student.feature_auth.ui.login.LoginScreen
import pl.edu.pk.student.feature_auth.ui.register.SignupScreen
import pl.edu.pk.student.feature_auth.ui.passwordChange.PasswordChangeScreen
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import pl.edu.pk.student.feature_medical_records.ui.MedicalRecordsMenuScreen
import pl.edu.pk.student.feature_medical_records.ui.MedicalRecordDetailScreen
import pl.edu.pk.student.feature_medical_records.ui.AddRecordScreen
import pl.edu.pk.student.feature_medical_records.ui.ViewRecordsScreen
import pl.edu.pk.student.feature_medical_records.ui.ManageRecordsScreen
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
    // Temporary in-memory storage for medical records
    var medicalRecords by remember {
        mutableStateOf<Map<MedicalRecordType, List<MedicalRecord>>>(
            mapOf(
                MedicalRecordType.TEST_RESULTS to emptyList(),
                MedicalRecordType.PRESCRIPTIONS to emptyList(),
                MedicalRecordType.DOCTOR_RECOMMENDATIONS to emptyList()
            )
        )
    }

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
        // =============== AUTH FLOW ===============
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

        // =============== MAIN APP ===============
        composable(NavDestinations.Main.route) {
            MainScreen(
                onNavigateToMedicalRecords = {
                    navController.navigate(NavDestinations.MedicalRecordsMenu.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(NavDestinations.ChangePassword.route)
                },
                onSignOut = onSignOut
            )
        }

        // =============== MEDICAL RECORDS FLOW ===============
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
                onViewRecords = {
                    navController.navigate(NavDestinations.ViewRecords.createRoute(recordType.name))
                },
                onManageRecords = {
                    navController.navigate(NavDestinations.ManageRecords.createRoute(recordType.name))
                }
            )
        }

        composable(
            route = NavDestinations.AddRecord.route,
            arguments = listOf(navArgument("recordType") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            AddRecordScreen(
                recordType = recordType,
                onBack = { navController.popBackStack() },
                onSave = { title, content, imageUri ->
                    val newRecord = MedicalRecord(
                        id = java.util.UUID.randomUUID().toString(),
                        type = recordType,
                        title = title,
                        content = content,
                        imageUri = imageUri
                    )

                    medicalRecords = medicalRecords.toMutableMap().apply {
                        this[recordType] = (this[recordType] ?: emptyList()) + newRecord
                    }
                }
            )
        }

        composable(
            route = NavDestinations.ViewRecords.route,
            arguments = listOf(navArgument("recordType") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordTypeName = backStackEntry.arguments?.getString("recordType")
            val recordType = MedicalRecordType.valueOf(recordTypeName ?: "TEST_RESULTS")

            ViewRecordsScreen(
                recordType = recordType,
                records = medicalRecords[recordType] ?: emptyList(),
                onBack = { navController.popBackStack() },
                onRecordClick = { record ->
                    // TODO: Navigate to record details
                }
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
                records = medicalRecords[recordType] ?: emptyList(),
                onBack = { navController.popBackStack() },
                onDeleteRecord = { record ->
                    medicalRecords = medicalRecords.toMutableMap().apply {
                        this[recordType] = (this[recordType] ?: emptyList()).filter { it.id != record.id }
                    }
                },
                onUpdateRecord = { record, newTitle, newContent ->
                    medicalRecords = medicalRecords.toMutableMap().apply {
                        this[recordType] = (this[recordType] ?: emptyList()).map {
                            if (it.id == record.id) {
                                it.copy(title = newTitle, content = newContent)
                            } else it
                        }
                    }
                }
            )
        }

        // =============== PROFILE FLOW ===============
        composable(NavDestinations.ChangePassword.route) {
            PasswordChangeScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}