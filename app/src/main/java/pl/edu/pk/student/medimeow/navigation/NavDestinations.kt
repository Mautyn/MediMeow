package pl.edu.pk.student.medimeow.navigation

sealed class NavDestinations(val route: String) {
    object Login : NavDestinations("login")
    object Signup : NavDestinations("signup")

    object Main : NavDestinations("main")

    object Dashboard : NavDestinations("dashboard")

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

    object Share : NavDestinations("share")

    object Profile : NavDestinations("profile")
    object ChangePassword : NavDestinations("change_password")

    object Settings : NavDestinations("settings")

    object Interactions : NavDestinations("interactions")

}