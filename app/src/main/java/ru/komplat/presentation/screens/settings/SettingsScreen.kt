package ru.komplat.presentation.screens.settings

import android.content.Intent
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
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                text = "Экспорт данных",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { viewModel.exportToCsv() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Экспорт в CSV")
                }
            }

            if (uiState.exportFile != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Файл экспорта готов:")
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

            Spacer(modifier = Modifier.height(16.dp))

            // About section
            Text(
                text = "О приложении",
                style = MaterialTheme.typography.titleMedium
            )

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "KomPlat - Учёт коммунальных платежей")
                    Text(text = "Версия 1.0.0")
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
