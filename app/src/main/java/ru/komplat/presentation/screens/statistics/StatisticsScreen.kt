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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.Expense
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

                // Detailed comparison by service type
                Text(
                    text = "Детальное сравнение",
                    style = MaterialTheme.typography.titleMedium
                )

                DetailedComparisonTable(
                    period1Expenses = uiState.period1Expenses,
                    period2Expenses = uiState.period2Expenses,
                    period1 = uiState.period1,
                    period2 = uiState.period2,
                    currencyFormat = currencyFormat
                )
            }
        }
    }
}

data class ServiceTypeComparison(
    val serviceTypeName: String,
    val period1Amount: Double,
    val period2Amount: Double
) {
    val difference: Double get() = period2Amount - period1Amount
}

@Composable
private fun DetailedComparisonTable(
    period1Expenses: List<Expense>,
    period2Expenses: List<Expense>,
    period1: String,
    period2: String,
    currencyFormat: NumberFormat
) {
    // Group expenses by service type
    val period1ByType = period1Expenses.groupBy { expense ->
        if (expense.serviceType == CompanyType.OTHER && !expense.companyCustomType.isNullOrBlank()) {
            expense.companyCustomType
        } else {
            getTypeDisplayName(expense.serviceType)
        }
    }.mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

    val period2ByType = period2Expenses.groupBy { expense ->
        if (expense.serviceType == CompanyType.OTHER && !expense.companyCustomType.isNullOrBlank()) {
            expense.companyCustomType
        } else {
            getTypeDisplayName(expense.serviceType)
        }
    }.mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

    // Combine all service types
    val allTypes = (period1ByType.keys + period2ByType.keys).sorted()

    if (allTypes.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Нет данных для сравнения",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Статья",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = period1,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = period2,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "+/-",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Data rows
            allTypes.forEach { type ->
                val amount1 = period1ByType[type] ?: 0.0
                val amount2 = period2ByType[type] ?: 0.0
                val diff = amount2 - amount1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormat.format(amount1),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = currencyFormat.format(amount2),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (diff > 0) "+${currencyFormat.format(diff)}" else currencyFormat.format(diff),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            diff > 0 -> MaterialTheme.colorScheme.error
                            diff < 0 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (type != allTypes.last()) {
                    Divider(modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

private fun getTypeDisplayName(type: CompanyType): String {
    return when (type) {
        CompanyType.GAS -> "Газ"
        CompanyType.ELECTRICITY -> "Электричество"
        CompanyType.WATER -> "Вода"
        CompanyType.HEATING -> "Отопление"
        CompanyType.ELEVATOR -> "Лифт"
        CompanyType.GARBAGE -> "Мусор"
        CompanyType.MAINTENANCE -> "Капитальный ремонт"
        CompanyType.INTERNET -> "Интернет"
        CompanyType.TV -> "Телевидение"
        CompanyType.OTHER -> "Другое"
    }
}
