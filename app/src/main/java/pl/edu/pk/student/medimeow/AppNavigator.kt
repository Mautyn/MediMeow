package pl.edu.pk.student.medimeow

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.edu.pk.student.feature_auth.ui.login.LoginScreen
import pl.edu.pk.student.feature_auth.ui.register.SignupScreen
import pl.edu.pk.student.feature_home.ui.HomeScreen

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

sealed class NavDestinations(val route: String) {
    object Login : NavDestinations("login")
    object Signup : NavDestinations("signup")
    object Home : NavDestinations("home")
}

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    authState: AuthState,
    onSignOut: () -> Unit
) {
    val startDestination = when (authState) {
        AuthState.Authenticated -> NavDestinations.Home.route
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
        // feature_auth
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

        // feature_home - nested navigation with bottom bar
        composable(NavDestinations.Home.route) {
            HomeScreen(
                onSignOut = onSignOut
            )
        }
    }
}