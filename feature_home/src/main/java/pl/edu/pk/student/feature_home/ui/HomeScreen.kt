package pl.edu.pk.student.feature_home.ui

import androidx.compose.runtime.Composable
import pl.edu.pk.student.feature_home.navigation.HomeNavigator

@Composable
fun HomeScreen(
    onSignOut: () -> Unit
) {
    HomeNavigator(onSignOut = onSignOut)
}