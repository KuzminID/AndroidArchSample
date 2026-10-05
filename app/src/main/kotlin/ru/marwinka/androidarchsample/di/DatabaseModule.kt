package ru.marwinka.androidarchsample.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.marwinka.androidarchsample.database.AppDatabase
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterDao
import javax.inject.Singleton

internal const val DATABASE_NAME = "android_arch_sample.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // No destructive fallback: the database holds user data (favorites).
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideCharacterDao(database: AppDatabase): CharacterDao = database.characterDao()
}
