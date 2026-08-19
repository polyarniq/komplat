package ru.komplat.domain.model

data class Period(
    val year: Int,
    val month: Int // 1-12
) {
    val formatted: String get() = String.format("%04d-%02d", year, month)

    companion object {
        fun fromFormatted(formatted: String): Period {
            val parts = formatted.split("-")
            return Period(parts[0].toInt(), parts[1].toInt())
        }

        fun current(): Period {
            val now = java.util.Calendar.getInstance()
            return Period(now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH) + 1)
        }
    }

    fun previous(): Period {
        return if (month == 1) Period(year - 1, 12) else Period(year, month - 1)
    }

    fun next(): Period {
        return if (month == 12) Period(year + 1, 1) else Period(year, month + 1)
    }

    fun displayName(): String {
        val months = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        return "${months[month - 1]} $year"
    }
}
