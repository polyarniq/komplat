package ru.komplat.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.Expense
import ru.komplat.domain.repository.ExpenseRepository
import ru.komplat.domain.repository.UtilityCompanyRepository
import java.io.File
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
    private val companyRepository: UtilityCompanyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportToCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            try {
                val file = File(context.cacheDir, "komplat_export.csv")
                file.outputStream().use { output ->
                    // Write UTF-8 BOM for Excel compatibility
                    output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                    output.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write("Период;Компания;Тип услуги;Сумма;Оплачено;Примечание")
                        writer.newLine()

                        expenseRepository.getAllExpenses().first().forEach { expense ->
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
                _uiState.update {
                    it.copy(isExporting = false, exportFile = file)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, error = e.message)
                }
            }
        }
    }

    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, importResult = null) }
            try {
                var importedCount = 0
                var skippedCount = 0

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    val lines = reader.readLines()

                    if (lines.isEmpty()) return@use

                    // Remove BOM from first line if present
                    val firstLine = if (lines[0].startsWith("\uFEFF")) {
                        lines[0].substring(1)
                    } else {
                        lines[0]
                    }

                    // Combine first line with rest
                    val allLines = listOf(firstLine) + lines.drop(1)

                    // Skip header (first line), process data lines
                    for (i in 1 until allLines.size) {
                        val line = allLines[i].trim()
                        if (line.isBlank()) continue

                        val parts = line.split(";")
                        if (parts.size >= 5) {
                            val period = parts[0].trim()
                            val companyName = parts[1].trim()
                            val serviceTypeName = parts[2].trim()
                            val amount = parts[3].trim().toDoubleOrNull()
                            val isPaid = parts[4].trim().lowercase() == "true"
                            val note = parts.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }

                            if (amount != null && period.isNotBlank() && companyName.isNotBlank()) {
                                // Find company by name
                                val companies = companyRepository.getAllCompanies().first()
                                val company = companies.find { it.name == companyName }

                                if (company != null) {
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
                                } else {
                                    skippedCount++
                                }
                            } else {
                                skippedCount++
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = "Импортировано: $importedCount, пропущено: $skippedCount"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, error = "Ошибка импорта: ${e.message}")
                }
            }
        }
    }

    fun clearExportFile() {
        _uiState.update { it.copy(exportFile = null) }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
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
