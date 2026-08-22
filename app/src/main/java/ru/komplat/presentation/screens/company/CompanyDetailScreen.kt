package ru.komplat.presentation.screens.company

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.CustomServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    companyId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CompanyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(companyId) {
        if (companyId > 0) {
            viewModel.loadCompany(companyId)
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
                title = { Text(if (companyId > 0) "Редактировать компанию" else "Новая компания") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (companyId > 0) {
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
            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Название компании") },
                modifier = Modifier.fillMaxWidth()
            )

            // Type selector
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = if (uiState.type == CompanyType.OTHER && uiState.customType.isNotBlank()) uiState.customType else getTypeDisplayName(uiState.type),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип услуги") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    // Built-in types
                    CompanyType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getTypeDisplayName(type)) },
                            onClick = {
                                viewModel.updateType(type)
                                showTypeDropdown = false
                            }
                        )
                    }
                    // Custom types from database
                    if (uiState.customServiceTypes.isNotEmpty()) {
                        Divider()
                        uiState.customServiceTypes.forEach { customType ->
                            DropdownMenuItem(
                                text = { Text(customType.name) },
                                onClick = {
                                    viewModel.updateType(CompanyType.OTHER)
                                    viewModel.updateCustomType(customType.name)
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Custom type field (shown when "Другое" is selected)
            if (uiState.type == CompanyType.OTHER) {
                OutlinedTextField(
                    value = uiState.customType,
                    onValueChange = { viewModel.updateCustomType(it) },
                    label = { Text("Свой тип услуги") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Account number
            OutlinedTextField(
                value = uiState.accountNumber,
                onValueChange = { viewModel.updateAccountNumber(it) },
                label = { Text("Номер лицевого счёта") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )

            // Phone
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { viewModel.updatePhone(it) },
                label = { Text("Телефон") },
                modifier = Modifier.fillMaxWidth()
            )

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            // Website
            OutlinedTextField(
                value = uiState.website,
                onValueChange = { viewModel.updateWebsite(it) },
                label = { Text("Сайт") },
                modifier = Modifier.fillMaxWidth()
            )

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
            title = { Text("Удалить компанию") },
            text = { Text("Вы уверены, что хотите удалить эту компанию и все связанные расходы?") },
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
