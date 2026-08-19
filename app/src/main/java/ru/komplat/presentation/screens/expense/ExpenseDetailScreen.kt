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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId > 0) {
            // Load existing expense - would need to fetch from repository
            // For now, we'll assume the expense is passed or loaded elsewhere
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
            // Company selector
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {}
            ) {
                OutlinedTextField(
                    value = uiState.companies.find { it.id == uiState.selectedCompanyId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Компания") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
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
