package pl.edu.pk.student.feature_home.navigation

sealed class HomeDestinations(val route: String) {
    object Dashboard : HomeDestinations("dashboard")
    object Profile : HomeDestinations("profile")
    object Settings : HomeDestinations("settings")
}