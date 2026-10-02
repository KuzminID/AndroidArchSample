package ru.marwinka.androidarchsample.domain.usecase

import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

class RefreshCharactersUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refresh()
}
