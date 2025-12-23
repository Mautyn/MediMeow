package pl.edu.pk.student.feature_interactions.data.repository

import android.util.Log
import pl.edu.pk.student.feature_interactions.data.remote.OpenFdaApiService
import pl.edu.pk.student.feature_interactions.domain.models.Drug
import pl.edu.pk.student.feature_interactions.domain.models.DrugInteraction
import pl.edu.pk.student.feature_interactions.domain.models.InteractionSeverity
import javax.inject.Inject

class InteractionsRepositoryImpl @Inject constructor(
    private val apiService: OpenFdaApiService
) : InteractionsRepository {

    private val drugNameCache = mutableMapOf<String, String>()

    override suspend fun searchDrugs(query: String): Result<List<Drug>> {
        return try {
            val response = apiService.searchDrugs(query)
            val drugs = mutableListOf<Drug>()

            response.results?.forEach { label ->
                label.openfda?.genericName?.forEach { genericName ->
                    val brandName = label.openfda.brandName?.firstOrNull() ?: genericName
                    val rxcui = label.openfda.rxcui?.firstOrNull() ?: ""

                    val drugId = rxcui.ifEmpty { genericName.hashCode().toString() }

                    drugNameCache[drugId] = genericName

                    val displayName = if (brandName != genericName && brandName.isNotEmpty()) {
                        "$genericName ($brandName)"
                    } else {
                        genericName
                    }

                    drugs.add(
                        Drug(
                            rxcui = drugId,
                            name = displayName
                        )
                    )
                }
            }

            val uniqueDrugs = drugs.distinctBy { it.rxcui }.take(15)
            Log.d("InteractionsRepo", "Found ${uniqueDrugs.size} drugs for query: $query")

            Result.success(uniqueDrugs)
        } catch (e: Exception) {
            Log.e("InteractionsRepo", "Error searching drugs", e)
            Result.failure(e)
        }
    }

    override suspend fun getDrugInteractions(rxcuiList: List<String>): Result<List<DrugInteraction>> {
        return try {
            if (rxcuiList.isEmpty()) {
                return Result.success(emptyList())
            }

            Log.d("InteractionsRepo", "Checking interactions for: $rxcuiList")

            val drugNames = rxcuiList.mapNotNull { rxcui ->
                drugNameCache[rxcui]
            }

            if (drugNames.isEmpty()) {
                Log.w("InteractionsRepo", "No drug names found in cache for rxcui list")
                return Result.success(emptyList())
            }

            Log.d("InteractionsRepo", "Searching interactions for drug names: $drugNames")

            val eventInteractions = try {
                val eventResponse = apiService.searchInteractionsFromEvents(drugNames)
                eventResponse.interactions.map { interaction ->
                    DrugInteraction(
                        drugName1 = interaction.drug1.replaceFirstChar { it.uppercase() },
                        drugName2 = interaction.drug2.replaceFirstChar { it.uppercase() },
                        severity = parseSeverityFromReports(interaction.reportCount),
                        description = interaction.description
                    )
                }
            } catch (e: Exception) {
                Log.e("InteractionsRepo", "Error getting adverse event interactions: ${e.message}")
                emptyList()
            }

            val labelInteractions = if (eventInteractions.isEmpty()) {
                try {
                    Log.d("InteractionsRepo", "No adverse events found, trying drug labels...")
                    val labelResponse = apiService.searchInteractionsFromLabels(drugNames)
                    labelResponse.interactions.map { interaction ->
                        DrugInteraction(
                            drugName1 = interaction.drug1.replaceFirstChar { it.uppercase() },
                            drugName2 = interaction.drug2.replaceFirstChar { it.uppercase() },
                            severity = parseSeverityFromText(interaction.description),
                            description = cleanDescription(interaction.description)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("InteractionsRepo", "Error getting label interactions: ${e.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }

            val allInteractions = eventInteractions + labelInteractions

            Log.d("InteractionsRepo", "Found ${allInteractions.size} total interactions")

            Result.success(allInteractions)

        } catch (e: Exception) {
            Log.e("InteractionsRepo", "Error getting interactions", e)
            Result.failure(e)
        }
    }

    override suspend fun getSpellingSuggestions(query: String): Result<List<String>> {
        return Result.success(emptyList())
    }


    private fun parseSeverityFromReports(reportCount: Int): InteractionSeverity {
        return when {
            reportCount >= 100 -> InteractionSeverity.HIGH
            reportCount >= 20 -> InteractionSeverity.MODERATE
            reportCount > 0 -> InteractionSeverity.LOW
            else -> InteractionSeverity.UNKNOWN
        }
    }


    private fun parseSeverityFromText(description: String): InteractionSeverity {
        val lowercaseDesc = description.lowercase()

        return when {
            lowercaseDesc.contains("contraindicated") ||
                    lowercaseDesc.contains("should not") ||
                    lowercaseDesc.contains("do not") ||
                    lowercaseDesc.contains("avoid") ||
                    lowercaseDesc.contains("severe") ||
                    lowercaseDesc.contains("fatal") ||
                    lowercaseDesc.contains("life-threatening") ||
                    lowercaseDesc.contains("serious") ||
                    lowercaseDesc.contains("death") ||
                    lowercaseDesc.contains("significantly increase") ||
                    lowercaseDesc.contains("significantly decrease") -> InteractionSeverity.HIGH

            lowercaseDesc.contains("caution") ||
                    lowercaseDesc.contains("moderate") ||
                    lowercaseDesc.contains("monitor") ||
                    lowercaseDesc.contains("may increase") ||
                    lowercaseDesc.contains("may decrease") ||
                    lowercaseDesc.contains("adjust") ||
                    lowercaseDesc.contains("dosage") ||
                    lowercaseDesc.contains("use with care") ||
                    lowercaseDesc.contains("clinical monitoring") -> InteractionSeverity.MODERATE

            lowercaseDesc.contains("minor") ||
                    lowercaseDesc.contains("potential") ||
                    lowercaseDesc.contains("unlikely") -> InteractionSeverity.LOW

            else -> InteractionSeverity.UNKNOWN
        }
    }

    private fun cleanDescription(description: String): String {
        return description
            .trim()
            .take(500)
            .replace(Regex("\\s+"), " ")
            .let { if (it.length == 500 && !it.endsWith(".")) "$it..." else it }
    }
}