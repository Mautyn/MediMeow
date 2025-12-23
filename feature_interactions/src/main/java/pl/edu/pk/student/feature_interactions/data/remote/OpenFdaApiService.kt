package pl.edu.pk.student.feature_interactions.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenFdaApiService {

    private val baseUrl = "https://api.fda.gov/drug"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
                prettyPrint = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("OpenFdaAPI", message)
                }
            }
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }


    suspend fun searchDrugs(name: String): DrugSearchResponse {
        return try {
            Log.d("OpenFdaAPI", "Searching for drug: $name")

            val response = client.get("$baseUrl/label.json") {
                parameter("search", "openfda.generic_name:$name")
                parameter("limit", 15)
            }

            if (response.status.value == 404) {
                Log.d("OpenFdaAPI", "No drugs found for: $name")
                return DrugSearchResponse(meta = null, results = emptyList())
            }

            response.body()
        } catch (e: ResponseException) {
            if (e.response.status.value == 404) {
                Log.d("OpenFdaAPI", "No drugs found for: $name (404)")
                DrugSearchResponse(meta = null, results = emptyList())
            } else {
                Log.e("OpenFdaAPI", "Error searching drugs: ${e.message}", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e("OpenFdaAPI", "Error searching drugs: ${e.message}", e)
            throw e
        }
    }


    suspend fun searchInteractionsFromEvents(drugNames: List<String>): InteractionsSearchResponse {
        if (drugNames.size < 2) {
            return InteractionsSearchResponse(interactions = emptyList())
        }

        return try {
            Log.d("OpenFdaAPI", "Searching adverse events for: $drugNames")

            val interactions = mutableListOf<DrugInteractionResult>()

            for (i in drugNames.indices) {
                for (j in i + 1 until drugNames.size) {
                    val drug1 = drugNames[i]
                    val drug2 = drugNames[j]

                    try {
                        val searchQuery = "patient.drug.openfda.generic_name:\"$drug1\"+AND+patient.drug.openfda.generic_name:\"$drug2\""

                        Log.d("OpenFdaAPI", "Checking: $drug1 + $drug2")

                        val response = client.get("$baseUrl/event.json") {
                            parameter("search", searchQuery)
                            parameter("count", "patient.reaction.reactionmeddrapt.exact")
                            parameter("limit", 10)
                        }

                        if (response.status.value == 200) {
                            val result: AdverseEventCountResponse = response.body()

                            val topReactions = result.results?.take(5) ?: emptyList()

                            if (topReactions.isNotEmpty()) {
                                val reactionList = topReactions
                                    .map { "${it.term} (${it.count} reports)" }
                                    .joinToString(", ")

                                val description = "Reported adverse reactions when taken together: $reactionList"

                                interactions.add(
                                    DrugInteractionResult(
                                        drug1 = drug1,
                                        drug2 = drug2,
                                        description = description,
                                        brandName = "$drug1 / $drug2",
                                        reportCount = topReactions.sumOf { it.count ?: 0 }
                                    )
                                )

                                Log.d("OpenFdaAPI", "Found interaction: $drug1 + $drug2 (${topReactions.size} reactions)")
                            }
                        }
                    } catch (e: ResponseException) {
                        if (e.response.status.value == 404) {
                            Log.d("OpenFdaAPI", "No adverse events for $drug1 + $drug2")
                        } else {
                            Log.e("OpenFdaAPI", "Error checking events: ${e.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("OpenFdaAPI", "Error checking events: ${e.message}")
                    }
                }
            }

            Log.d("OpenFdaAPI", "Found ${interactions.size} drug combinations with adverse events")

            InteractionsSearchResponse(interactions = interactions)

        } catch (e: Exception) {
            Log.e("OpenFdaAPI", "Error searching interactions: ${e.message}", e)
            throw e
        }
    }


    suspend fun searchInteractionsFromLabels(drugNames: List<String>): InteractionsSearchResponse {
        if (drugNames.size < 2) {
            return InteractionsSearchResponse(interactions = emptyList())
        }

        return try {
            Log.d("OpenFdaAPI", "Searching drug labels for interactions")

            val allInteractions = mutableListOf<DrugInteractionResult>()

            for (drug in drugNames) {
                try {
                    val response = client.get("$baseUrl/label.json") {
                        parameter("search", "openfda.generic_name:\"$drug\"")
                        parameter("limit", 1)
                    }

                    if (response.status.value == 200) {
                        val result: DrugSearchResponse = response.body()

                        result.results?.firstOrNull()?.let { label ->
                            val interactionText = label.drugInteractions?.firstOrNull() ?: ""

                            val otherDrugs = drugNames.filter { it != drug }

                            for (otherDrug in otherDrugs) {
                                if (interactionText.contains(otherDrug, ignoreCase = true)) {
                                    val sentences = interactionText.split(". ")
                                    val relevantSentences = sentences.filter {
                                        it.contains(otherDrug, ignoreCase = true)
                                    }.take(2)

                                    if (relevantSentences.isNotEmpty()) {
                                        allInteractions.add(
                                            DrugInteractionResult(
                                                drug1 = drug,
                                                drug2 = otherDrug,
                                                description = relevantSentences.joinToString(". "),
                                                brandName = label.openfda?.brandName?.firstOrNull() ?: drug,
                                                reportCount = 0
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("OpenFdaAPI", "Error reading label for $drug: ${e.message}")
                }
            }

            Log.d("OpenFdaAPI", "Found ${allInteractions.size} interactions from labels")

            InteractionsSearchResponse(
                interactions = allInteractions.distinctBy { "${it.drug1}-${it.drug2}" }
            )

        } catch (e: Exception) {
            Log.e("OpenFdaAPI", "Error searching label interactions: ${e.message}", e)
            throw e
        }
    }

    fun close() {
        client.close()
    }
}


data class DrugInteractionResult(
    val drug1: String,
    val drug2: String,
    val description: String,
    val brandName: String,
    val reportCount: Int = 0
)

data class InteractionsSearchResponse(
    val interactions: List<DrugInteractionResult>
)

@Serializable
data class AdverseEventCountResponse(
    @SerialName("meta")
    val meta: MetaResponse? = null,
    @SerialName("results")
    val results: List<CountResultResponse>? = null
)

@Serializable
data class CountResultResponse(
    @SerialName("term")
    val term: String? = null,
    @SerialName("count")
    val count: Int? = null
)