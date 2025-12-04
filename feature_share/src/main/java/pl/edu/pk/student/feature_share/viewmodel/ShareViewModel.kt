package pl.edu.pk.student.feature_share.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.edu.pk.student.core.domain.ShareableItem
import pl.edu.pk.student.feature_medical_records.data.repository.MedicalRecordsRepository
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import pl.edu.pk.student.feature_share.data.ShareFormat
import pl.edu.pk.student.feature_share.data.ShareRepository
import pl.edu.pk.student.feature_share.data.ShareResult
import javax.inject.Inject

data class ShareUiState(
    val availableRecords: List<ShareableItem> = emptyList(),
    val selectedRecords: Set<String> = emptySet(),
    val selectedFormat: ShareFormat = ShareFormat.Html,
    val includeImages: Boolean = true,
    val isLoading: Boolean = false,
    val filterType: MedicalRecordType? = null
)

sealed class ShareEvent {
    data class ShareSuccess(val message: String) : ShareEvent()
    data class ShareError(val message: String) : ShareEvent()
}

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val medicalRecordsRepository: MedicalRecordsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ShareEvent?>(null)
    val events: StateFlow<ShareEvent?> = _events.asStateFlow()

    private val testResultsFlow = medicalRecordsRepository.getRecordsByType(MedicalRecordType.TEST_RESULTS)
    private val prescriptionsFlow = medicalRecordsRepository.getRecordsByType(MedicalRecordType.PRESCRIPTIONS)
    private val recommendationsFlow = medicalRecordsRepository.getRecordsByType(MedicalRecordType.DOCTOR_RECOMMENDATIONS)

    // Combine all flows into one with explicit type
    val allRecords: StateFlow<List<ShareableItem>> = combine(
        testResultsFlow,
        prescriptionsFlow,
        recommendationsFlow
    ) { testResults, prescriptions, recommendations ->
        // Combine lists and explicitly declare as ShareableItem
        val combinedList: List<ShareableItem> = listOf(
            testResults,
            prescriptions,
            recommendations
        ).flatten()

        // Sort by timestamp
        combinedList.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            allRecords.collect { records ->
                val filteredRecords = if (_uiState.value.filterType != null) {
                    // Filter records by type - cast to MedicalRecord to access 'type' property
                    records.mapNotNull { it as? MedicalRecord }
                        .filter { it.type == _uiState.value.filterType }
                } else {
                    records
                }

                _uiState.value = _uiState.value.copy(
                    availableRecords = filteredRecords
                )
            }
        }
    }

    fun toggleRecordSelection(recordId: String) {
        _uiState.value = _uiState.value.copy(
            selectedRecords = if (recordId in _uiState.value.selectedRecords) {
                _uiState.value.selectedRecords - recordId
            } else {
                _uiState.value.selectedRecords + recordId
            }
        )
    }

    fun selectAllRecords() {
        _uiState.value = _uiState.value.copy(
            selectedRecords = _uiState.value.availableRecords.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedRecords = emptySet())
    }

    fun setFormat(format: ShareFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }

    fun setIncludeImages(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeImages = include)
    }

    fun setFilterType(type: MedicalRecordType?) {
        val filteredRecords = if (type != null) {
            // Filter by casting to MedicalRecord and checking type
            allRecords.value.mapNotNull { it as? MedicalRecord }
                .filter { it.type == type }
        } else {
            allRecords.value
        }

        _uiState.value = _uiState.value.copy(
            filterType = type,
            availableRecords = filteredRecords,
            selectedRecords = emptySet()
        )
    }

    fun shareSelectedRecords(): ShareResult? {
        val state = _uiState.value

        if (state.selectedRecords.isEmpty()) {
            _events.value = ShareEvent.ShareError("Please select at least one record to share")
            return null
        }

        _uiState.value = state.copy(isLoading = true)

        val recordsToShare = state.availableRecords.filter { it.id in state.selectedRecords }

        val result = shareRepository.shareItems(
            items = recordsToShare,
            format = state.selectedFormat,
            includeImages = state.includeImages
        )

        _uiState.value = state.copy(isLoading = false)

        return when (result) {
            is ShareResult.Success -> {
                _events.value = ShareEvent.ShareSuccess("Share prepared successfully")
                result
            }
            is ShareResult.Error -> {
                _events.value = ShareEvent.ShareError(result.message)
                null
            }
        }
    }

    fun clearEvent() {
        _events.value = null
    }
}