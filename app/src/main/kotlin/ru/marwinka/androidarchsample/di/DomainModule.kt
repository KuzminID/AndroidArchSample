package ru.marwinka.androidarchsample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository
import ru.marwinka.androidarchsample.feature.characters.domain.usecase.ObserveCharactersUseCase

/** Use cases carry no DI annotations, so they are provided here, unscoped. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun provideObserveCharacters(
        repository: CharacterRepository,
        settings: CharacterSettingsRepository,
    ) = ObserveCharactersUseCase(repository, settings)
}
