package ru.komplat.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.komplat.domain.model.CustomServiceType

interface CustomServiceTypeRepository {
    fun getAll(): Flow<List<CustomServiceType>>
    suspend fun getById(id: Long): CustomServiceType?
    suspend fun insert(type: CustomServiceType): Long
    suspend fun update(type: CustomServiceType)
    suspend fun deleteById(id: Long)
}
