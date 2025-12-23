package pl.edu.pk.student.feature_interactions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.edu.pk.student.feature_interactions.data.repository.InteractionsRepository
import pl.edu.pk.student.feature_interactions.domain.models.Drug
import pl.edu.pk.student.feature_interactions.domain.models.DrugInteraction
import javax.inject.Inject

data class InteractionsState(
    val searchQuery: String = "",
    val searchResults: List<Drug> = emptyList(),
    val selectedDrugs: List<Drug> = emptyList(),
    val interactions: List<DrugInteraction> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingInteractions: Boolean = false,
    val errorMessage: String? = null,
    val showResults: Boolean = false
)

@HiltViewModel
class InteractionsViewModel @Inject constructor(
    private val repository: InteractionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InteractionsState())
    val state: StateFlow<InteractionsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, errorMessage = null) }

        searchJob?.cancel()

        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                searchDrugs(query)
            }
        } else {
            _state.update { it.copy(searchResults = emptyList(), showResults = false) }
        }
    }

    private fun searchDrugs(query: String) = viewModelScope.launch {
        _state.update { it.copy(isSearching = true, errorMessage = null) }

        repository.searchDrugs(query)
            .onSuccess { drugs ->
                _state.update {
                    it.copy(
                        searchResults = drugs.take(10),
                        isSearching = false,
                        showResults = drugs.isNotEmpty()
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = "Failed to search drugs: ${error.message}",
                        showResults = false
                    )
                }
            }
    }

    fun onDrugSelected(drug: Drug) {
        val currentSelected = _state.value.selectedDrugs

        if (!currentSelected.contains(drug)) {
            val newSelected = currentSelected + drug
            _state.update {
                it.copy(
                    selectedDrugs = newSelected,
                    searchQuery = "",
                    searchResults = emptyList(),
                    showResults = false
                )
            }

            if (newSelected.size >= 2) {
                checkInteractions()
            }
        }
    }

    fun onDrugRemoved(drug: Drug) {
        val newSelected = _state.value.selectedDrugs - drug
        _state.update { it.copy(selectedDrugs = newSelected) }

        if (newSelected.size >= 2) {
            checkInteractions()
        } else {
            _state.update { it.copy(interactions = emptyList()) }
        }
    }

    fun checkInteractions() = viewModelScope.launch {
        val selectedDrugs = _state.value.selectedDrugs

        if (selectedDrugs.size < 2) {
            _state.update {
                it.copy(
                    errorMessage = "Please select at least 2 drugs to check interactions"
                )
            }
            return@launch
        }

        _state.update { it.copy(isLoadingInteractions = true, errorMessage = null) }

        val rxcuiList = selectedDrugs.map { it.rxcui }

        repository.getDrugInteractions(rxcuiList)
            .onSuccess { interactions ->
                _state.update {
                    it.copy(
                        interactions = interactions.sortedByDescending { interaction ->
                            interaction.severity.level
                        },
                        isLoadingInteractions = false,
                        errorMessage = if (interactions.isEmpty()) {
                            null
                        } else {
                            null
                        }
                    )
                }
            }
            .onFailure { error ->
                val message = when {
                    error.message?.contains("404") == true ->
                        "No interaction data available for these medications"
                    error.message?.contains("timeout") == true ->
                        "Connection timeout. Please try again."
                    error.message?.contains("Unable to resolve host") == true ->
                        "No internet connection. Please check your connection."
                    else ->
                        "Failed to check interactions. Please try again."
                }
                _state.update {
                    it.copy(
                        isLoadingInteractions = false,
                        errorMessage = message
                    )
                }
            }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun clearAll() {
        _state.update {
            InteractionsState()
        }
    }

    fun dismissSearchResults() {
        _state.update { it.copy(showResults = false, searchResults = emptyList()) }
    }
}