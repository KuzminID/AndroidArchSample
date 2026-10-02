package ru.marwinka.androidarchsample.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

class GetCharacterDetailUseCase(
    private val repository: CharacterRepository,
) {
    operator fun invoke(id: Int): Flow<Character?> = repository.observeCharacter(id)
}
