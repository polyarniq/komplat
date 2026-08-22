package ru.komplat.presentation.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToServiceTypes: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromCsv(it) }
    }

    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
            // Export section
            Text(
                text = "Экспорт",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { viewModel.exportToCsv() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Экспорт в CSV")
            }

            Button(
                onClick = { viewModel.exportBackup() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Archive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Резервная копия (ZIP)")
            }

            if (uiState.isExporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.exportFile != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Файл готов: ${uiState.exportFile?.name}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val file = uiState.exportFile
                                if (file != null) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/octet-stream"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Поделиться файлом"))
                                }
                                viewModel.clearExportFile()
                            }
                        ) {
                            Text("Поделиться")
                        }
                    }
                }
            }

            Divider()

            // Import section
            Text(
                text = "Импорт",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = { csvImportLauncher.launch("text/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isImporting
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Импорт из CSV")
            }

            Button(
                onClick = { backupImportLauncher.launch("application/zip") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isImporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Восстановить из копии")
            }

            if (uiState.isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.importResult != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.importResult ?: "",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearImportResult() }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }
                }
            }

            Divider()

            // Error message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Divider()

            // Service types management
            Text(
                text = "Справочники",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = onNavigateToServiceTypes,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Label, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Типы услуг")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About section
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleMedium
            )

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "KomPlat - Учёт коммунальных платежей")
                    Text(text = "Версия 1.1.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Приложение для ведения учёта коммунальных расходов.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
