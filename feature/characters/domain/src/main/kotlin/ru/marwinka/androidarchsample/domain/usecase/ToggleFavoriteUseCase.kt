package ru.marwinka.androidarchsample.domain.usecase

import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

class ToggleFavoriteUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(id: Int) = repository.toggleFavorite(id)
}
