package ru.komplat.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.komplat.data.local.db.DatabaseHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper {
        return DatabaseHelper(context)
    }
}
