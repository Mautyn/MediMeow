package pl.edu.pk.student.feature_dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.pk.student.core.ui.util.DeviceConfiguration
import pl.edu.pk.student.feature_dashboard.domain.models.DashboardAction
import pl.edu.pk.student.feature_dashboard.domain.models.DashboardTiles
import pl.edu.pk.student.feature_dashboard.ui.components.DashboardTileComponent

@Composable
fun DashboardScreen(
    onTileClick: (DashboardAction) -> Unit = {}
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    val columns = when (deviceConfiguration) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 2
        DeviceConfiguration.MOBILE_LANDSCAPE -> 3
        DeviceConfiguration.TABLET_PORTRAIT -> 3
        DeviceConfiguration.TABLET_LANDSCAPE -> 4
        DeviceConfiguration.DESKTOP -> 4
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1200.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "MediMeow",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Meow your way to better health 🐱",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 1200.dp)
        ) {
            if (deviceConfiguration == DeviceConfiguration.MOBILE_PORTRAIT ||
                deviceConfiguration == DeviceConfiguration.MOBILE_LANDSCAPE) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((200 * ((DashboardTiles.tiles.size + columns - 1) / columns)).dp)
                ) {
                    items(DashboardTiles.tiles) { tile ->
                        DashboardTileComponent(
                            tile = tile,
                            onClick = { onTileClick(tile.action) }
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardTiles.tiles.chunked(columns).forEach { rowTiles ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowTiles.forEach { tile ->
                                DashboardTileComponent(
                                    tile = tile,
                                    onClick = { onTileClick(tile.action) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowTiles.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}