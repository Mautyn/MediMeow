package pl.edu.pk.student.medimeow.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.edu.pk.student.feature_dashboard.domain.models.DashboardAction
import pl.edu.pk.student.feature_dashboard.ui.DashboardScreen
import pl.edu.pk.student.feature_profile.ui.ProfileScreen
import pl.edu.pk.student.feature_settings.ui.SettingsScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    onNavigateToMedicalRecords: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToInteractions: () -> Unit,
    onNavigateToMedicineAvailability: () -> Unit,
    onSignOut: () -> Unit,
    bottomNavController: NavHostController = rememberNavController()
) {
    val items = listOf(
        BottomNavItem(
            route = "dashboard",
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = "profile",
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        ),
        BottomNavItem(
            route = "settings",
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onTileClick = { action ->
                        when (action) {
                            DashboardAction.AddMedicalRecord -> onNavigateToMedicalRecords()
                            DashboardAction.MedicineAvailability -> onNavigateToMedicineAvailability()
                            DashboardAction.CheckInteractions -> onNavigateToInteractions()
                            DashboardAction.SharePrescription -> onNavigateToShare()
                        }
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onNavigateToChangePassword = onNavigateToChangePassword,
                    onSignOut = onSignOut
                )
            }

            composable("settings") {
                SettingsScreen(
                    onSignOut = onSignOut
                )
            }
        }
    }
}