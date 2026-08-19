package ru.komplat.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.komplat.domain.repository.ExpenseRepository
import ru.komplat.domain.repository.UtilityCompanyRepository
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val isExporting: Boolean = false,
    val exportFile: File? = null,
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
                file.bufferedWriter().use { writer ->
                    writer.write("Период;Компания;Тип;Сумма;Оплачено;Примечание")
                    writer.newLine()

                    expenseRepository.getAllExpenses().first().forEach { expense ->
                        writer.write("${expense.period};${expense.companyName};${expense.companyType};${expense.amount};${expense.isPaid};${expense.note ?: ""}")
                        writer.newLine()
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

    fun clearExportFile() {
        _uiState.update { it.copy(exportFile = null) }
    }
}
