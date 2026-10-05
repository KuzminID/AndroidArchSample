package ru.marwinka.androidarchsample.feature.characters.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository

/** Streams characters in the sort order the user selected. */
class ObserveCharactersUseCase(
    private val repository: CharacterRepository,
    private val settings: CharacterSettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Character>> =
        settings.observeSortOrder().flatMapLatest(repository::observeCharacters)
}
