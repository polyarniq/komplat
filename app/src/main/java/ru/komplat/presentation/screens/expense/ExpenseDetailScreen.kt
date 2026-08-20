package ru.komplat.presentation.screens.expense

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.komplat.domain.model.AttachedFile
import ru.komplat.domain.model.CompanyType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showServiceTypeDropdown by remember { mutableStateOf(false) }
    var showCompanyDropdown by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            val file = File(photoUri!!.path!!)
            viewModel.attachFile(
                filePath = file.absolutePath,
                fileName = file.name,
                mimeType = "image/jpeg",
                fileSize = file.length()
            )
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            val fileName = getFileName(context, selectedUri)
            val file = File(context.filesDir, "attachments/${fileName}")
            file.parentFile?.mkdirs()
            context.contentResolver.openInputStream(selectedUri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.attachFile(
                filePath = file.absolutePath,
                fileName = fileName,
                mimeType = context.contentResolver.getType(selectedUri) ?: "application/octet-stream",
                fileSize = file.length()
            )
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createImageUri(context)
            photoUri?.let { cameraLauncher.launch(it) }
        }
    }

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

            // File attachments section
            Text(
                text = "Прикреплённые файлы",
                style = MaterialTheme.typography.titleMedium
            )

            // Buttons for attaching files
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Камера")
                }
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Файл")
                }
            }

            // List of saved files (for existing expenses)
            uiState.files.forEach { file ->
                AttachedFileCard(
                    file = file,
                    onView = { openFile(context, file) },
                    onDelete = { viewModel.deleteAttachedFile(file.id) }
                )
            }

            // List of pending files (for new expenses)
            uiState.pendingFiles.forEachIndexed { index, pendingFile ->
                PendingFileCard(
                    pendingFile = pendingFile,
                    onView = { openPendingFile(context, pendingFile) },
                    onDelete = { viewModel.deletePendingFile(index) }
                )
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

private fun createImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.filesDir, "attachments").apply { mkdirs() }
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private fun getFileName(context: Context, uri: Uri): String {
    var fileName = "file_${System.currentTimeMillis()}"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            fileName = cursor.getString(nameIndex) ?: fileName
        }
    }
    return fileName
}

private fun openFile(context: Context, file: AttachedFile) {
    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(file.filePath))
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(fileUri, file.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

@Composable
private fun AttachedFileCard(
    file: AttachedFile,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    text = formatFileSize(file.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onView) {
                Icon(Icons.Default.Visibility, contentDescription = "Просмотреть")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить файл") },
            text = { Text("Вы уверены, что хотите удалить этот файл?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
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

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes Б"
        bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} МБ"
    }
}

private fun openPendingFile(context: Context, pendingFile: PendingFile) {
    val file = File(pendingFile.filePath)
    if (file.exists()) {
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, pendingFile.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}

@Composable
private fun PendingFileCard(
    pendingFile: PendingFile,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (pendingFile.mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pendingFile.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    text = formatFileSize(pendingFile.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Будет сохранён",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onView) {
                Icon(Icons.Default.Visibility, contentDescription = "Просмотреть")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить файл") },
            text = { Text("Вы уверены, что хотите удалить этот файл?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
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
