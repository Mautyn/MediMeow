package pl.edu.pk.student.feature_interactions.data.repository

import pl.edu.pk.student.feature_interactions.domain.models.Drug
import pl.edu.pk.student.feature_interactions.domain.models.DrugInteraction

interface InteractionsRepository {
    suspend fun searchDrugs(query: String): Result<List<Drug>>
    suspend fun getDrugInteractions(rxcuiList: List<String>): Result<List<DrugInteraction>>
    suspend fun getSpellingSuggestions(query: String): Result<List<String>>
}