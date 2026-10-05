package ru.marwinka.androidarchsample.feature.characters.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterApi
import ru.marwinka.androidarchsample.feature.characters.data.repository.CharacterRepositoryImpl
import ru.marwinka.androidarchsample.feature.characters.data.repository.CharacterSettingsRepositoryImpl
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CharacterDataModule {
    @Binds
    @Singleton
    abstract fun bindCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindCharacterSettingsRepository(impl: CharacterSettingsRepositoryImpl): CharacterSettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideCharacterApi(retrofit: Retrofit): CharacterApi = retrofit.create()
    }
}
