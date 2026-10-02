package ru.marwinka.androidarchsample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.domain.usecase.GetCharacterDetailUseCase
import ru.marwinka.androidarchsample.domain.usecase.GetCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.GetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.RefreshCharactersUseCase
import ru.marwinka.androidarchsample.domain.usecase.SetSortOrderUseCase
import ru.marwinka.androidarchsample.domain.usecase.ToggleFavoriteUseCase

/** Provides domain use cases. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun provideGetCharacters(repository: CharacterRepository) = GetCharactersUseCase(repository)

    @Provides
    fun provideGetCharacterDetail(repository: CharacterRepository) = GetCharacterDetailUseCase(repository)

    @Provides
    fun provideGetSortOrder(repository: CharacterRepository) = GetSortOrderUseCase(repository)

    @Provides
    fun provideRefreshCharacters(repository: CharacterRepository) = RefreshCharactersUseCase(repository)

    @Provides
    fun provideSetSortOrder(repository: CharacterRepository) = SetSortOrderUseCase(repository)

    @Provides
    fun provideToggleFavorite(repository: CharacterRepository) = ToggleFavoriteUseCase(repository)
}
