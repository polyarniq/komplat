package ru.komplat.presentation.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.komplat.domain.model.CompanyType
import ru.komplat.domain.model.Expense
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on service type
            Icon(
                imageVector = getIconForType(expense.serviceType),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Service type, company name and period
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (expense.serviceType == CompanyType.OTHER && !expense.companyCustomType.isNullOrBlank()) expense.companyCustomType else getTypeDisplayName(expense.serviceType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = expense.companyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = expense.period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount and status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (expense.isPaid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Оплачено",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getIconForType(type: CompanyType): ImageVector {
    return when (type) {
        CompanyType.GAS -> Icons.Default.LocalFireDepartment
        CompanyType.ELECTRICITY -> Icons.Default.Bolt
        CompanyType.WATER -> Icons.Default.WaterDrop
        CompanyType.HEATING -> Icons.Default.Thermostat
        CompanyType.ELEVATOR -> Icons.Default.Elevator
        CompanyType.GARBAGE -> Icons.Default.Delete
        CompanyType.MAINTENANCE -> Icons.Default.Construction
        CompanyType.INTERNET -> Icons.Default.Wifi
        CompanyType.TV -> Icons.Default.Tv
        CompanyType.OTHER -> Icons.Default.Business
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
