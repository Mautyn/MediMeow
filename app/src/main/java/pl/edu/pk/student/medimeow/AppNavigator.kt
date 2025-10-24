package pl.edu.pk.student.medimeow


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
        startDestination = startDestination
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
                onNavigateToLogin = { navController.navigate(NavDestinations.Login.route) }
            )
        }

        // feature_home
        composable(NavDestinations.Home.route) {
            HomeScreen(
                onSignOut = onSignOut
            )
        }

        /*// feature_profile
        composable(AppDestination.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }*/
    }
}