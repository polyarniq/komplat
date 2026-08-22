package ru.komplat.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.komplat.data.local.db.DatabaseHelper
import ru.komplat.data.repository.AttachedFileRepositoryImpl
import ru.komplat.data.repository.CustomServiceTypeRepositoryImpl
import ru.komplat.data.repository.ExpenseRepositoryImpl
import ru.komplat.data.repository.UtilityCompanyRepositoryImpl
import ru.komplat.domain.repository.AttachedFileRepository
import ru.komplat.domain.repository.CustomServiceTypeRepository
import ru.komplat.domain.repository.ExpenseRepository
import ru.komplat.domain.repository.UtilityCompanyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUtilityCompanyRepository(
        dbHelper: DatabaseHelper
    ): UtilityCompanyRepository {
        return UtilityCompanyRepositoryImpl(dbHelper)
    }

    @Provides
    @Singleton
    fun provideExpenseRepository(
        dbHelper: DatabaseHelper
    ): ExpenseRepository {
        return ExpenseRepositoryImpl(dbHelper)
    }

    @Provides
    @Singleton
    fun provideAttachedFileRepository(
        dbHelper: DatabaseHelper
    ): AttachedFileRepository {
        return AttachedFileRepositoryImpl(dbHelper)
    }

    @Provides
    @Singleton
    fun provideCustomServiceTypeRepository(
        dbHelper: DatabaseHelper
    ): CustomServiceTypeRepository {
        return CustomServiceTypeRepositoryImpl(dbHelper)
    }
}
