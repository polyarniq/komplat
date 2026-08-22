package ru.komplat.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.AttachedFile
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.AttachedFileRepository
import ru.komplat.domain.repository.ExpenseRepository
import ru.komplat.domain.repository.UtilityCompanyRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportFile: File? = null,
    val importResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val companyRepository: UtilityCompanyRepository,
    private val fileRepository: AttachedFileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Export CSV only
    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            try {
                val file = File(context.cacheDir, "komplat_export.csv")
                writeCsvFile(file)
                _uiState.update { it.copy(isExporting = false, exportFile = file) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    // Export full backup (ZIP with CSV + files)
    fun exportBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            try {
                val backupFile = File(context.cacheDir, "komplat_backup.zip")
                val csvFile = File(context.cacheDir, "backup_data.csv")
                val attachmentsDir = File(context.cacheDir, "backup_attachments").apply { mkdirs() }

                // Write CSV
                writeCsvFile(csvFile)

                // Copy attached files
                val allFiles = fileRepository.getAllFiles().first()
                val fileMapping = mutableMapOf<String, String>() // old path -> new name

                allFiles.forEachIndexed { index, attachedFile ->
                    val sourceFile = File(attachedFile.filePath)
                    if (sourceFile.exists()) {
                        val ext = attachedFile.fileName.substringAfterLast(".", "")
                        val newName = "file_${index}.${ext}"
                        val destFile = File(attachmentsDir, newName)
                        sourceFile.copyTo(destFile, overwrite = true)
                        fileMapping[attachedFile.filePath] = newName
                    }
                }

                // Write file mapping
                val mappingFile = File(context.cacheDir, "file_mapping.txt")
                mappingFile.bufferedWriter().use { writer ->
                    fileMapping.forEach { (oldPath, newName) ->
                        writer.write("$oldPath|$newName")
                        writer.newLine()
                    }
                }

                // Create ZIP
                ZipOutputStream(FileOutputStream(backupFile)).use { zip ->
                    // Add CSV
                    zip.putNextEntry(ZipEntry("data.csv"))
                    FileInputStream(csvFile).use { it.copyTo(zip) }
                    zip.closeEntry()

                    // Add file mapping
                    zip.putNextEntry(ZipEntry("file_mapping.txt"))
                    FileInputStream(mappingFile).use { it.copyTo(zip) }
                    zip.closeEntry()

                    // Add attachment files
                    attachmentsDir.listFiles()?.forEach { file ->
                        zip.putNextEntry(ZipEntry("attachments/${file.name}"))
                        FileInputStream(file).use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }

                // Cleanup temp files
                csvFile.delete()
                mappingFile.delete()
                attachmentsDir.deleteRecursively()

                _uiState.update { it.copy(isExporting = false, exportFile = backupFile) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = "Ошибка экспорта: ${e.message}") }
            }
        }
    }

    // Import CSV with duplicate checking
    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, importResult = null) }
            try {
                val companies = companyRepository.getAllCompanies().first()
                val existingExpenses = expenseRepository.getAllExpenses().first()

                var importedCount = 0
                var skippedDuplicate = 0
                var skippedNoCompany = 0
                var errorCount = 0

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader(Charsets.UTF_8).readText()
                    val lines = normalizeContent(content)

                    for (i in 1 until lines.size) {
                        val line = lines[i].trim()
                        if (line.isBlank()) continue

                        val parts = line.split(";")
                        if (parts.size >= 5) {
                            val period = parts[0].trim()
                            val companyNameRaw = parts[1].trim()
                            val serviceTypeName = parts[2].trim()
                            val amount = parts[3].trim().toDoubleOrNull()
                            val isPaid = parts[4].trim().lowercase() == "true"
                            val note = parts.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }

                            if (amount != null && period.isNotBlank() && companyNameRaw.isNotBlank()) {
                                val company = findCompany(companies, companyNameRaw)

                                if (company != null) {
                                    // Check for duplicate
                                    val isDuplicate = existingExpenses.any { existing ->
                                        existing.period == period &&
                                        existing.companyId == company.id &&
                                        existing.amount == amount
                                    }

                                    if (isDuplicate) {
                                        skippedDuplicate++
                                    } else {
                                        try {
                                            val serviceType = parseServiceType(serviceTypeName)
                                            val expense = Expense(
                                                companyId = company.id,
                                                serviceType = serviceType,
                                                amount = amount,
                                                period = period,
                                                isPaid = isPaid,
                                                note = note
                                            )
                                            expenseRepository.insertExpense(expense)
                                            importedCount++
                                        } catch (e: Exception) {
                                            errorCount++
                                        }
                                    }
                                } else {
                                    skippedNoCompany++
                                }
                            }
                        }
                    }
                }

                val result = buildString {
                    append("Импортировано: $importedCount")
                    if (skippedDuplicate > 0) append(", дубликатов: $skippedDuplicate")
                    if (skippedNoCompany > 0) append(", не найдено компаний: $skippedNoCompany")
                    if (errorCount > 0) append(", ошибок: $errorCount")
                }
                _uiState.update { it.copy(isImporting = false, importResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = "Ошибка импорта: ${e.message}") }
            }
        }
    }

    // Import full backup (ZIP with CSV + files)
    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, importResult = null) }
            try {
                val companies = companyRepository.getAllCompanies().first()
                val existingExpenses = expenseRepository.getAllExpenses().first()
                val existingFiles = fileRepository.getAllFiles().first()

                var importedExpenses = 0
                var skippedDuplicate = 0
                var importedFiles = 0
                var skippedFileDuplicate = 0
                var errorCount = 0

                val tempDir = File(context.cacheDir, "backup_import").apply { mkdirs() }

                // Extract ZIP
                val fileMapping = mutableMapOf<String, String>() // old path -> new name
                var csvContent = ""

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            when {
                                entry.name == "data.csv" -> {
                                    csvContent = zip.bufferedReader(Charsets.UTF_8).readText()
                                }
                                entry.name == "file_mapping.txt" -> {
                                    zip.bufferedReader().readLines().forEach { line ->
                                        val parts = line.split("|")
                                        if (parts.size == 2) {
                                            fileMapping[parts[0]] = parts[1]
                                        }
                                    }
                                }
                                entry.name.startsWith("attachments/") -> {
                                    val fileName = entry.name.removePrefix("attachments/")
                                    val file = File(tempDir, fileName)
                                    FileOutputStream(file).use { zip.copyTo(it) }
                                }
                            }
                            zip.closeEntry()
                            entry = zip.nextEntry
                        }
                    }
                }

                // Import expenses from CSV
                if (csvContent.isNotBlank()) {
                    val lines = normalizeContent(csvContent)

                    for (i in 1 until lines.size) {
                        val line = lines[i].trim()
                        if (line.isBlank()) continue

                        val parts = line.split(";")
                        if (parts.size >= 5) {
                            val period = parts[0].trim()
                            val companyNameRaw = parts[1].trim()
                            val serviceTypeName = parts[2].trim()
                            val amount = parts[3].trim().toDoubleOrNull()
                            val isPaid = parts[4].trim().lowercase() == "true"
                            val note = parts.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }

                            if (amount != null && period.isNotBlank() && companyNameRaw.isNotBlank()) {
                                val company = findCompany(companies, companyNameRaw)

                                if (company != null) {
                                    val isDuplicate = existingExpenses.any { existing ->
                                        existing.period == period &&
                                        existing.companyId == company.id &&
                                        existing.amount == amount
                                    }

                                    if (isDuplicate) {
                                        skippedDuplicate++
                                    } else {
                                        try {
                                            val serviceType = parseServiceType(serviceTypeName)
                                            val expense = Expense(
                                                companyId = company.id,
                                                serviceType = serviceType,
                                                amount = amount,
                                                period = period,
                                                isPaid = isPaid,
                                                note = note
                                            )
                                            expenseRepository.insertExpense(expense)
                                            importedExpenses++
                                        } catch (e: Exception) {
                                            errorCount++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Import files
                val attachmentFiles = tempDir.listFiles()
                if (attachmentFiles != null) {
                for (file in attachmentFiles) {
                    val isDuplicate = existingFiles.any { it.fileName == file.name && it.fileSize == file.length() }

                    if (isDuplicate) {
                        skippedFileDuplicate++
                    } else {
                        try {
                            // Find the expense this file belongs to
                            val oldPath = fileMapping.entries.find { entry -> entry.value == file.name }?.key
                            val oldFile = existingFiles.find { it.filePath == oldPath }

                            if (oldFile != null) {
                                val newFilePath = File(context.filesDir, "attachments/${file.name}").apply {
                                    parentFile?.mkdirs()
                                    file.copyTo(this, overwrite = true)
                                }

                                val attachedFile = AttachedFile(
                                    expenseId = oldFile.expenseId,
                                    companyId = oldFile.companyId,
                                    filePath = newFilePath.absolutePath,
                                    fileName = file.name,
                                    fileType = oldFile.fileType,
                                    mimeType = oldFile.mimeType,
                                    fileSize = file.length()
                                )
                                fileRepository.insertFile(attachedFile)
                                importedFiles++
                            }
                        } catch (e: Exception) {
                            errorCount++
                        }
                    }
                }
                } // end if attachmentFiles != null

                // Cleanup
                tempDir.deleteRecursively()

                val result = buildString {
                    append("Расходов: импортировано $importedExpenses")
                    if (skippedDuplicate > 0) append(", дубликатов $skippedDuplicate")
                    append(". Файлов: импортировано $importedFiles")
                    if (skippedFileDuplicate > 0) append(", дубликатов $skippedFileDuplicate")
                    if (errorCount > 0) append(". Ошибок: $errorCount")
                }
                _uiState.update { it.copy(isImporting = false, importResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = "Ошибка импорта: ${e.message}") }
            }
        }
    }

    fun clearExportFile() {
        _uiState.update { it.copy(exportFile = null) }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    private suspend fun writeCsvFile(file: File) {
        val expenses = expenseRepository.getAllExpenses().first()
        file.outputStream().use { output ->
            output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            output.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("Период;Компания;Тип услуги;Сумма;Оплачено;Примечание")
                writer.newLine()

                expenses.forEach { expense ->
                    val serviceTypeName = if (expense.serviceType == CompanyType.OTHER && !expense.companyCustomType.isNullOrBlank()) {
                        expense.companyCustomType
                    } else {
                        getServiceTypeName(expense.serviceType)
                    }
                    writer.write("${expense.period};${expense.companyName};$serviceTypeName;${expense.amount};${expense.isPaid};${expense.note ?: ""}")
                    writer.newLine()
                }
            }
        }
    }

    private fun normalizeContent(content: String): List<String> {
        return content
            .replace("\uFEFF", "")
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .split("\n")
    }

    private fun findCompany(companies: List<ru.komplat.domain.model.UtilityCompany>, name: String): ru.komplat.domain.model.UtilityCompany? {
        val nameNorm = name.replace("\u201C", "\"").replace("\u201D", "\"").replace(Regex("\\s+"), " ").trim()
        return companies.find { it.name == name }
            ?: companies.find { it.name.trim() == name }
            ?: companies.find {
                it.name.replace("\u201C", "\"").replace("\u201D", "\"").replace(Regex("\\s+"), " ").trim() == nameNorm
            }
            ?: companies.find { it.name.contains(name, ignoreCase = true) }
            ?: companies.find { name.contains(it.name, ignoreCase = true) }
    }

    private fun getServiceTypeName(type: CompanyType): String {
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

    private fun parseServiceType(name: String): CompanyType {
        return when (name) {
            "Газ" -> CompanyType.GAS
            "Электричество" -> CompanyType.ELECTRICITY
            "Вода" -> CompanyType.WATER
            "Отопление" -> CompanyType.HEATING
            "Лифт" -> CompanyType.ELEVATOR
            "Мусор" -> CompanyType.GARBAGE
            "Капитальный ремонт" -> CompanyType.MAINTENANCE
            "Интернет" -> CompanyType.INTERNET
            "Телевидение" -> CompanyType.TV
            else -> CompanyType.OTHER
        }
    }
}
