package pl.edu.pk.student.feature_medicine_availability.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.pk.student.core.ui.util.DeviceConfiguration
import pl.edu.pk.student.feature_medicine_availability.domain.models.*
import pl.edu.pk.student.feature_medicine_availability.ui.components.*
import pl.edu.pk.student.feature_medicine_availability.viewmodel.MedicineAvailabilityState
import pl.edu.pk.student.feature_medicine_availability.viewmodel.MedicineAvailabilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineAvailabilityScreen(
    onBack: () -> Unit,
    viewModel: MedicineAvailabilityViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Medicine availability",
                    color = MaterialTheme.colorScheme.onPrimary
                    )},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    if (state.selectedMedicine != null || state.cityQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                MobileLayout(
                    state = state,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCityQueryChanged = viewModel::onCityQueryChanged,
                    onMedicineSelected = viewModel::selectMedicine,
                    onBarcodeScanned = viewModel::searchByBarcode,
                    onFindPharmacies = viewModel::findPharmaciesInCity,
                    onCheckAvailability = viewModel::checkAvailabilityForSelectedMedicine,
                    onRadiusChanged = viewModel::onSearchRadiusChanged,
                    modifier = Modifier.padding(padding)
                )
            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                TabletDesktopLayout(
                    state = state,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCityQueryChanged = viewModel::onCityQueryChanged,
                    onMedicineSelected = viewModel::selectMedicine,
                    onBarcodeScanned = viewModel::searchByBarcode,
                    onFindPharmacies = viewModel::findPharmaciesInCity,
                    onCheckAvailability = viewModel::checkAvailabilityForSelectedMedicine,
                    onRadiusChanged = viewModel::onSearchRadiusChanged,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun MobileLayout(
    state: MedicineAvailabilityState,
    onSearchQueryChanged: (String) -> Unit,
    onCityQueryChanged: (String) -> Unit,
    onMedicineSelected: (Medicine) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFindPharmacies: () -> Unit,
    onCheckAvailability: () -> Unit,
    onRadiusChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard()

        MedicineSearchSection(
            searchQuery = state.searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            searchResults = state.searchResults,
            selectedMedicine = state.selectedMedicine,
            onMedicineSelected = onMedicineSelected,
            onBarcodeScanned = onBarcodeScanned,
            isSearching = state.isSearching
        )

        if (state.selectedMedicine != null) {
            Spacer(modifier = Modifier.height(8.dp))

            CitySearchSection(
                cityQuery = state.cityQuery,
                onCityQueryChanged = onCityQueryChanged,
                onFindPharmacies = onFindPharmacies,
                isLoading = state.isLoadingLocation,
                searchRadius = state.searchRadius,
                onRadiusChanged = onRadiusChanged
            )
        }

        if (state.nearbyPharmacies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCheckAvailability,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isCheckingAvailability
            ) {
                if (state.isCheckingAvailability) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Check availability")
            }

            Spacer(modifier = Modifier.height(8.dp))

            AvailabilityResultsSection(
                availability = state.availability,
                isLoading = state.isCheckingAvailability
            )
        }
    }
}

@Composable
private fun TabletDesktopLayout(
    state: MedicineAvailabilityState,
    onSearchQueryChanged: (String) -> Unit,
    onCityQueryChanged: (String) -> Unit,
    onMedicineSelected: (Medicine) -> Unit,
    onBarcodeScanned: (String) -> Unit,
    onFindPharmacies: () -> Unit,
    onCheckAvailability: () -> Unit,
    onRadiusChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard()

            MedicineSearchSection(
                searchQuery = state.searchQuery,
                onSearchQueryChanged = onSearchQueryChanged,
                searchResults = state.searchResults,
                selectedMedicine = state.selectedMedicine,
                onMedicineSelected = onMedicineSelected,
                onBarcodeScanned = onBarcodeScanned,
                isSearching = state.isSearching
            )

            if (state.selectedMedicine != null) {
                CitySearchSection(
                    cityQuery = state.cityQuery,
                    onCityQueryChanged = onCityQueryChanged,
                    onFindPharmacies = onFindPharmacies,
                    isLoading = state.isLoadingLocation,
                    searchRadius = state.searchRadius,
                    onRadiusChanged = onRadiusChanged
                )

                if (state.nearbyPharmacies.isNotEmpty()) {
                    Button(
                        onClick = onCheckAvailability,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isCheckingAvailability
                    ) {
                        if (state.isCheckingAvailability) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Sprawdź dostępność")
                    }
                }
            }
        }

        if (state.nearbyPharmacies.isNotEmpty()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                AvailabilityResultsSection(
                    availability = state.availability,
                    isLoading = state.isCheckingAvailability,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Check the availability of the drug",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                "Find a medicine by name or barcode, then check its availability at pharmacies in your area.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AvailabilityResultsSection(
    availability: List<PharmacyWithStock>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Results (${availability.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (availability.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "Unavailable",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "The medicine is not available in nearby pharmacies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            availability.forEach { item ->
                PharmacyAvailabilityCard(item)
            }
        }
    }
}