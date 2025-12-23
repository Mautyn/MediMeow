package pl.edu.pk.student.feature_interactions.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.edu.pk.student.core.ui.util.DeviceConfiguration
import pl.edu.pk.student.feature_interactions.ui.components.DrugSearchField
import pl.edu.pk.student.feature_interactions.ui.components.InteractionCard
import pl.edu.pk.student.feature_interactions.ui.components.SelectedDrugChip
import pl.edu.pk.student.feature_interactions.viewmodel.InteractionsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractionsScreen(
    onBack: () -> Unit,
    viewModel: InteractionsViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.value.errorMessage) {
        state.value.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drug Interactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT,
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                MobileLayout(
                    state = state.value,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onDrugSelected = viewModel::onDrugSelected,
                    onDrugRemoved = viewModel::onDrugRemoved,
                    onCheckInteractions = viewModel::checkInteractions,
                    onClearAll = viewModel::clearAll,
                    onDismissSearchResults = viewModel::dismissSearchResults,
                    modifier = contentModifier
                )
            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                TabletDesktopLayout(
                    state = state.value,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onDrugSelected = viewModel::onDrugSelected,
                    onDrugRemoved = viewModel::onDrugRemoved,
                    onCheckInteractions = viewModel::checkInteractions,
                    onClearAll = viewModel::clearAll,
                    onDismissSearchResults = viewModel::dismissSearchResults,
                    modifier = contentModifier
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MobileLayout(
    state: pl.edu.pk.student.feature_interactions.viewmodel.InteractionsState,
    onSearchQueryChanged: (String) -> Unit,
    onDrugSelected: (pl.edu.pk.student.feature_interactions.domain.models.Drug) -> Unit,
    onDrugRemoved: (pl.edu.pk.student.feature_interactions.domain.models.Drug) -> Unit,
    onCheckInteractions: () -> Unit,
    onClearAll: () -> Unit,
    onDismissSearchResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        InfoCard()

        Spacer(modifier = Modifier.height(16.dp))

        DrugSearchField(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChanged,
            searchResults = state.searchResults,
            onDrugSelected = {
                onDrugSelected(it)
                onDismissSearchResults()
            },
            isSearching = state.isSearching,
            showResults = state.showResults
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedDrugs.isNotEmpty()) {
            Text(
                text = "Selected Medications (${state.selectedDrugs.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedDrugs.forEach { drug ->
                    SelectedDrugChip(
                        drug = drug,
                        onRemove = { onDrugRemoved(drug) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCheckInteractions,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedDrugs.size >= 2 && !state.isLoadingInteractions
            ) {
                if (state.isLoadingInteractions) {
                    CircularProgressIndicator()
                } else {
                    Text("Check Interactions")
                }
            }

            if (state.selectedDrugs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClearAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All")
                }
            }
        }

        if (state.interactions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            InteractionsResults(interactions = state.interactions)
        } else if (state.selectedDrugs.size >= 2 && !state.isLoadingInteractions) {
            Spacer(modifier = Modifier.height(24.dp))

            NoInteractionsFound()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TabletDesktopLayout(
    state: pl.edu.pk.student.feature_interactions.viewmodel.InteractionsState,
    onSearchQueryChanged: (String) -> Unit,
    onDrugSelected: (pl.edu.pk.student.feature_interactions.domain.models.Drug) -> Unit,
    onDrugRemoved: (pl.edu.pk.student.feature_interactions.domain.models.Drug) -> Unit,
    onCheckInteractions: () -> Unit,
    onClearAll: () -> Unit,
    onDismissSearchResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            InfoCard()

            Spacer(modifier = Modifier.height(24.dp))

            DrugSearchField(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChanged,
                searchResults = state.searchResults,
                onDrugSelected = {
                    onDrugSelected(it)
                    onDismissSearchResults()
                },
                isSearching = state.isSearching,
                showResults = state.showResults,
                modifier = Modifier.widthIn(max = 600.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.selectedDrugs.isNotEmpty()) {
                Text(
                    text = "Selected Medications (${state.selectedDrugs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.selectedDrugs.forEach { drug ->
                        SelectedDrugChip(
                            drug = drug,
                            onRemove = { onDrugRemoved(drug) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCheckInteractions,
                    modifier = Modifier.widthIn(max = 300.dp),
                    enabled = state.selectedDrugs.size >= 2 && !state.isLoadingInteractions
                ) {
                    if (state.isLoadingInteractions) {
                        CircularProgressIndicator()
                    } else {
                        Text("Check Interactions")
                    }
                }

                if (state.selectedDrugs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onClearAll,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text("Clear All")
                    }
                }
            }

            if (state.interactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                InteractionsResults(interactions = state.interactions)
            } else if (state.selectedDrugs.size >= 2 && !state.isLoadingInteractions) {
                Spacer(modifier = Modifier.height(32.dp))

                NoInteractionsFound()
            }
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Check Drug Interactions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Search and select at least 2 medications to check for potential interactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun InteractionsResults(
    interactions: List<pl.edu.pk.student.feature_interactions.domain.models.DrugInteraction>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Found ${interactions.size} interaction(s)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        interactions.forEach { interaction ->
            InteractionCard(interaction = interaction)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NoInteractionsFound(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No Interactions Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "No known interactions were found between the selected medications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}