package ru.marwinka.androidarchsample.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

/** Streams characters sorted by the selected sort order. */
class GetCharactersUseCase(
    private val repository: CharacterRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Character>> =
        repository.observeSortOrder().flatMapLatest { sortOrder ->
            repository.observeCharacters(sortOrder)
        }
}
