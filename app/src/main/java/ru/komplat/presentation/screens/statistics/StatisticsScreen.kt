package ru.komplat.presentation.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selectors
            Text(
                text = "Сравнение периодов",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period 1 dropdown
                var expanded1 by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded1,
                    onExpandedChange = { expanded1 = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.period1,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Период 1") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded1,
                        onDismissRequest = { expanded1 = false }
                    ) {
                        uiState.periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    viewModel.updatePeriod1(period)
                                    expanded1 = false
                                }
                            )
                        }
                    }
                }

                // Period 2 dropdown
                var expanded2 by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded2,
                    onExpandedChange = { expanded2 = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.period2,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Период 2") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded2,
                        onDismissRequest = { expanded2 = false }
                    ) {
                        uiState.periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    viewModel.updatePeriod2(period)
                                    expanded2 = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison results
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.period1.isNotBlank() && uiState.period2.isNotBlank()) {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Period 1 total
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.period1,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = currencyFormat.format(uiState.period1Total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Period 2 total
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.period2,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = currencyFormat.format(uiState.period2Total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Difference card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.difference >= 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Разница",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = currencyFormat.format(uiState.difference),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            @Suppress("DEPRECATION")
                            Icon(
                                imageVector = if (uiState.difference >= 0)
                                    Icons.Default.TrendingUp
                                else
                                    Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (uiState.difference >= 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%.1f%%", uiState.percentageChange),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed comparison by company
                Text(
                    text = "Детальное сравнение",
                    style = MaterialTheme.typography.titleMedium
                )

                // This would show a table comparing expenses by company
                // For now, we'll show a placeholder
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Здесь будет таблица сравнения по статьям расходов",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
