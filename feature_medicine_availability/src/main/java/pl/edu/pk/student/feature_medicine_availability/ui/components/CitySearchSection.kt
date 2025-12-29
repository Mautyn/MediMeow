package pl.edu.pk.student.feature_medicine_availability.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CitySearchSection(
    cityQuery: String,
    onCityQueryChanged: (String) -> Unit,
    onFindPharmacies: () -> Unit,
    isLoading: Boolean,
    searchRadius: Double,
    onRadiusChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Location",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedTextField(
            value = cityQuery,
            onValueChange = onCityQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("City...") },
            leadingIcon = {
                Icon(Icons.Default.LocationCity, "City")
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Searching range",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${searchRadius.toInt()} km",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = searchRadius.toFloat(),
                    onValueChange = { onRadiusChanged(it.toDouble()) },
                    valueRange = 1f..50f,
                    steps = 48
                )
            }
        }

        Button(
            onClick = onFindPharmacies,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && cityQuery.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Find pharmacies",
                color = MaterialTheme.colorScheme.onPrimary
                )
        }
    }
}