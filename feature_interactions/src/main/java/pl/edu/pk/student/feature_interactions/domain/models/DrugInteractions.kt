package pl.edu.pk.student.feature_interactions.domain.models

data class DrugInteraction(
    val drugName1: String,
    val drugName2: String,
    val severity: InteractionSeverity,
    val description: String
)

enum class InteractionSeverity(val displayName: String, val level: Int) {
    HIGH("High", 3),
    MODERATE("Moderate", 2),
    LOW("Low", 1),
    UNKNOWN("Unknown", 0);

    companion object {
        fun fromString(value: String?): InteractionSeverity {
            return when (value?.lowercase()) {
                "high", "major" -> HIGH
                "moderate" -> MODERATE
                "low", "minor" -> LOW
                else -> UNKNOWN
            }
        }
    }
}

data class Drug(
    val rxcui: String,
    val name: String,
    val genericName: String = name
)

data class InteractionGroup(
    val drugs: List<Drug>,
    val interactions: List<DrugInteraction>
)