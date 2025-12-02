package pl.edu.pk.student.feature_medical_records.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.edu.pk.student.feature_medical_records.data.repository.MedicalRecordsRepository
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecord
import pl.edu.pk.student.feature_medical_records.domain.models.MedicalRecordType
import java.util.UUID
import javax.inject.Inject

sealed class MedicalRecordsUiState {
    object Loading : MedicalRecordsUiState()
    data class Success(val records: List<MedicalRecord>) : MedicalRecordsUiState()
    data class Error(val message: String) : MedicalRecordsUiState()
}

@HiltViewModel
class MedicalRecordsViewModel @Inject constructor(
    private val repository: MedicalRecordsRepository
) : ViewModel() {

    private val _currentRecordType = MutableStateFlow<MedicalRecordType?>(null)
    val currentRecordType: StateFlow<MedicalRecordType?> = _currentRecordType.asStateFlow()
    private val _imageCache = mutableMapOf<String, android.graphics.Bitmap?>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val recordsForCurrentType: StateFlow<List<MedicalRecord>> =
        _currentRecordType.value?.let { type ->
            repository.getRecordsByType(type).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        } ?: MutableStateFlow(emptyList())

    fun setCurrentRecordType(type: MedicalRecordType) {
        _currentRecordType.value = type
    }

    fun getRecordsFlow(type: MedicalRecordType): StateFlow<List<MedicalRecord>> {
        return repository.getRecordsByType(type).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    suspend fun convertImageToBase64(imageUri: Uri): Result<String> {
        _isUploadingImage.value = true
        return try {
            repository.convertImageToBase64(imageUri).also {
                _isUploadingImage.value = false
            }
        } catch (e: Exception) {
            _isUploadingImage.value = false
            Result.failure(e)
        }
    }

    fun getRecordById(type: MedicalRecordType, id: String): Flow<MedicalRecord?> {
        return getRecordsFlow(type)
            .map { records ->
                records.find { it.id == id }
            }
            .distinctUntilChanged()
    }

    fun addRecord(
        type: MedicalRecordType,
        title: String,
        content: String?,
        imageUri: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val record = MedicalRecord(
                id = UUID.randomUUID().toString(),
                type = type,
                title = title,
                content = content,
                imageUri = imageUri,
                timestamp = System.currentTimeMillis()
            )

            repository.addRecord(record).fold(
                onSuccess = {
                    _successMessage.value = "Record added successfully"
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to add record: ${error.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateRecord(record: MedicalRecord, newTitle: String, newContent: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val updatedRecord = record.copy(
                title = newTitle,
                content = newContent,
                timestamp = System.currentTimeMillis()
            )

            repository.updateRecord(updatedRecord).fold(
                onSuccess = {
                    _successMessage.value = "Record updated successfully"
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to update record: ${error.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.deleteRecord(recordId).fold(
                onSuccess = {
                    _successMessage.value = "Record deleted successfully"
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = "Failed to delete record: ${error.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    suspend fun getDecodedImage(base64String: String): Result<android.graphics.Bitmap?> {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _imageCache[base64String]?.let {
                    return@withContext Result.success(it)
                }

                val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                if (bitmap != null) {
                    _imageCache[base64String] = bitmap
                }

                Result.success(bitmap)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun clearImageCache() {
        _imageCache.values.forEach { it?.recycle() }
        _imageCache.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearImageCache()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}