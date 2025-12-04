package pl.edu.pk.student.medimeow.navigation

sealed class NavDestinations(val route: String) {
    // Auth
    object Login : NavDestinations("login")
    object Signup : NavDestinations("signup")

    // Main with bottom nav
    object Main : NavDestinations("main")

    // Dashboard
    object Dashboard : NavDestinations("dashboard")

    // Medical Records
    object MedicalRecordsMenu : NavDestinations("medical_records_menu")

    object MedicalRecordDetail : NavDestinations("medical_record_detail/{recordType}") {
        fun createRoute(recordType: String) = "medical_record_detail/$recordType"
    }

    object RecordDetails : NavDestinations("record_details/{recordId}/{recordType}") {
        fun createRoute(recordId: String, recordType: String) = "record_details/$recordId/$recordType"
    }

    object AddRecord : NavDestinations("add_record/{recordType}") {
        fun createRoute(recordType: String) = "add_record/$recordType"
    }

    object ManageRecords : NavDestinations("manage_records/{recordType}") {
        fun createRoute(recordType: String) = "manage_records/$recordType"
    }

    // Share
    object Share : NavDestinations("share")

    // Profile
    object Profile : NavDestinations("profile")
    object ChangePassword : NavDestinations("change_password")

    // Settings
    object Settings : NavDestinations("settings")
}