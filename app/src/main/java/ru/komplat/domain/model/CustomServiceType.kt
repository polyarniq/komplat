package ru.komplat.domain.model

data class CustomServiceType(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
