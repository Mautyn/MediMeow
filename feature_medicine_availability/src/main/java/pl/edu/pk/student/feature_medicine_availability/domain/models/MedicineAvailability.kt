package pl.edu.pk.student.feature_medicine_availability.domain.models

data class Medicine(
    val id: String = "",
    val barcode: String = "",
    val name: String = "",
    val manufacturer: String = "",
    val dosage: String = "",
    val activeSubstance: String = "",
    val category: MedicineCategory = MedicineCategory.OTHER
)

enum class MedicineCategory(val displayName: String) {
    PAINKILLER("Przeciwbólowe"),
    ANTIBIOTIC("Antybiotyki"),
    CARDIOVASCULAR("Kardiologiczne"),
    GASTROINTESTINAL("Żołądkowo-jelitowe"),
    RESPIRATORY("Układu oddechowego"),
    DERMATOLOGICAL("Dermatologiczne"),
    VITAMINS("Witaminy i suplementy"),
    OTHER("Inne")
}

data class Pharmacy(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phoneNumber: String = "",
    val openingHours: Map<String, String> = emptyMap(),
    val verified: Boolean = false
)

data class MedicineStock(
    val id: String = "",
    val medicineId: String = "",
    val pharmacyId: String = "",
    val barcode: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val available: Boolean = true
)

data class PharmacyWithStock(
    val pharmacy: Pharmacy,
    val stock: MedicineStock,
    val medicine: Medicine,
    val distanceKm: Double? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double
)