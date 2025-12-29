package pl.edu.pk.student.feature_medicine_availability.viewmodel

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
import pl.edu.pk.student.feature_medicine_availability.data.repository.MedicineAvailabilityRepository
import pl.edu.pk.student.feature_medicine_availability.domain.models.*
import javax.inject.Inject
import kotlin.math.pow

data class MedicineAvailabilityState(
    val searchQuery: String = "",
    val cityQuery: String = "",
    val selectedMedicine: Medicine? = null,
    val searchResults: List<Medicine> = emptyList(),
    val nearbyPharmacies: List<Pharmacy> = emptyList(),
    val availability: List<PharmacyWithStock> = emptyList(),
    val userLocation: Location? = null,
    val isSearching: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val isCheckingAvailability: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchRadius: Double = 10.0
)

@HiltViewModel
class MedicineAvailabilityViewModel @Inject constructor(
    private val repository: MedicineAvailabilityRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MedicineAvailabilityState())
    val state: StateFlow<MedicineAvailabilityState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, errorMessage = null) }

        searchJob?.cancel()

        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                searchMedicines(query)
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun onCityQueryChanged(city: String) {
        _state.update { it.copy(cityQuery = city, errorMessage = null) }
    }

    fun onSearchRadiusChanged(radius: Double) {
        _state.update { it.copy(searchRadius = radius) }
    }

    private fun searchMedicines(query: String) = viewModelScope.launch {
        _state.update { it.copy(isSearching = true, errorMessage = null) }

        repository.searchMedicineByName(query)
            .onSuccess { medicines ->
                _state.update {
                    it.copy(
                        searchResults = medicines,
                        isSearching = false
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = "Błąd wyszukiwania: ${error.message}"
                    )
                }
            }
    }

    fun searchByBarcode(barcode: String) = viewModelScope.launch {
        _state.update { it.copy(isSearching = true, errorMessage = null) }

        repository.searchMedicineByBarcode(barcode)
            .onSuccess { medicine ->
                if (medicine != null) {
                    _state.update {
                        it.copy(
                            selectedMedicine = medicine,
                            searchQuery = medicine.name,
                            isSearching = false,
                            successMessage = "Znaleziono: ${medicine.name}"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = "Nie znaleziono leku o kodzie: $barcode"
                        )
                    }
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isSearching = false,
                        errorMessage = "Błąd skanowania: ${error.message}"
                    )
                }
            }
    }

    fun selectMedicine(medicine: Medicine) {
        _state.update {
            it.copy(
                selectedMedicine = medicine,
                searchQuery = medicine.name,
                searchResults = emptyList()
            )
        }
    }

    fun findPharmaciesInCity() = viewModelScope.launch {
        val city = _state.value.cityQuery

        if (city.isBlank()) {
            _state.update { it.copy(errorMessage = "Podaj nazwę miasta") }
            return@launch
        }

        _state.update { it.copy(isLoadingLocation = true, errorMessage = null) }

        repository.getLocationFromCity(city)
            .onSuccess { location ->
                if (location != null) {
                    _state.update { it.copy(userLocation = location) }
                    findNearbyPharmacies(location)
                } else {
                    _state.update {
                        it.copy(
                            isLoadingLocation = false,
                            errorMessage = "Nie znaleziono miasta: $city"
                        )
                    }
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoadingLocation = false,
                        errorMessage = "Błąd lokalizacji: ${error.message}"
                    )
                }
            }
    }

    private fun findNearbyPharmacies(location: Location) = viewModelScope.launch {
        val radius = _state.value.searchRadius

        repository.findNearbyPharmacies(location, radius)
            .onSuccess { pharmacies ->
                _state.update {
                    it.copy(
                        nearbyPharmacies = pharmacies,
                        isLoadingLocation = false,
                        successMessage = "Znaleziono ${pharmacies.size} aptek w promieniu ${radius}km"
                    )
                }

                _state.value.selectedMedicine?.let { medicine ->
                    checkAvailability(medicine, pharmacies)
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoadingLocation = false,
                        errorMessage = "Błąd wyszukiwania aptek: ${error.message}"
                    )
                }
            }
    }

    fun checkAvailabilityForSelectedMedicine() {
        val medicine = _state.value.selectedMedicine
        val pharmacies = _state.value.nearbyPharmacies

        if (medicine == null) {
            _state.update { it.copy(errorMessage = "Wybierz lek") }
            return
        }

        if (pharmacies.isEmpty()) {
            _state.update { it.copy(errorMessage = "Brak aptek w okolicy") }
            return
        }

        checkAvailability(medicine, pharmacies)
    }

    private fun checkAvailability(
        medicine: Medicine,
        pharmacies: List<Pharmacy>
    ) = viewModelScope.launch {
        _state.update { it.copy(isCheckingAvailability = true, errorMessage = null) }

        val pharmacyIds = pharmacies.map { it.id }

        repository.checkMedicineAvailability(medicine.id, pharmacyIds)
            .onSuccess { availability ->
                val location = _state.value.userLocation
                val availabilityWithDistance = if (location != null) {
                    availability.map { item ->
                        val distance = calculateDistance(
                            location.latitude, location.longitude,
                            item.pharmacy.latitude, item.pharmacy.longitude
                        )
                        item.copy(distanceKm = distance)
                    }.sortedBy { it.distanceKm }
                } else {
                    availability
                }

                _state.update {
                    it.copy(
                        availability = availabilityWithDistance,
                        isCheckingAvailability = false,
                        successMessage = if (availability.isNotEmpty()) {
                            "Lek dostępny w ${availability.size} aptekach"
                        } else {
                            "Brak dostępności w pobliskich aptekach"
                        }
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isCheckingAvailability = false,
                        errorMessage = "Błąd sprawdzania dostępności: ${error.message}"
                    )
                }
            }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = kotlin.math.sin(dLat / 2).pow(2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2).pow(2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearSearch() {
        _state.update {
            MedicineAvailabilityState(
                searchRadius = it.searchRadius
            )
        }
    }
}