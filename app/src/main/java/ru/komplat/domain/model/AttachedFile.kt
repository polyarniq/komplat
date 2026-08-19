package ru.komplat.domain.model

enum class FileType {
    RECEIPT,
    INVOICE,
    OTHER
}

data class AttachedFile(
    val id: Long = 0,
    val expenseId: Long? = null,
    val companyId: Long? = null,
    val filePath: String,
    val fileName: String,
    val fileType: FileType,
    val mimeType: String,
    val fileSize: Long,
    val createdAt: Long = System.currentTimeMillis()
)
