package ru.komplat.presentation.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.komplat.domain.model.CompanyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showServiceTypeDropdown by remember { mutableStateOf(false) }
    var showCompanyDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId > 0) {
            viewModel.loadExpenseById(expenseId)
        }
    }

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId > 0) "Редактировать расход" else "Новый расход") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (expenseId > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            // Service type selector
            ExposedDropdownMenuBox(
                expanded = showServiceTypeDropdown,
                onExpandedChange = { showServiceTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = getTypeDisplayName(uiState.selectedServiceType),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип услуги") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showServiceTypeDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showServiceTypeDropdown,
                    onDismissRequest = { showServiceTypeDropdown = false }
                ) {
                    CompanyType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getTypeDisplayName(type)) },
                            onClick = {
                                viewModel.updateServiceType(type)
                                showServiceTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Company selector (filtered by service type)
            ExposedDropdownMenuBox(
                expanded = showCompanyDropdown,
                onExpandedChange = { showCompanyDropdown = it }
            ) {
                val selectedCompany = uiState.filteredCompanies.find { it.id == uiState.selectedCompanyId }
                OutlinedTextField(
                    value = if (selectedCompany != null) {
                        if (selectedCompany.type == CompanyType.OTHER && !selectedCompany.customType.isNullOrBlank()) {
                            "${selectedCompany.name} (${selectedCompany.customType})"
                        } else {
                            selectedCompany.name
                        }
                    } else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Компания") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCompanyDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCompanyDropdown,
                    onDismissRequest = { showCompanyDropdown = false }
                ) {
                    uiState.filteredCompanies.forEach { company ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(company.name)
                                    if (company.type == CompanyType.OTHER && !company.customType.isNullOrBlank()) {
                                        Text(
                                            text = company.customType,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.updateCompanyId(company.id)
                                showCompanyDropdown = false
                            }
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Period
            OutlinedTextField(
                value = uiState.period,
                onValueChange = { viewModel.updatePeriod(it) },
                label = { Text("Период (YYYY-MM)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("Примечание") },
                modifier = Modifier.fillMaxWidth()
            )

            // Paid status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isPaid,
                    onCheckedChange = { viewModel.updateIsPaid(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Оплачено")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить")
                }
            }

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить расход") },
            text = { Text("Вы уверены, что хотите удалить этот расход?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
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
