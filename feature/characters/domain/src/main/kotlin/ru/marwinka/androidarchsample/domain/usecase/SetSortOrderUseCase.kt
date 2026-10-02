package ru.marwinka.androidarchsample.domain.usecase

import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

class SetSortOrderUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(order: SortOrder) = repository.setSortOrder(order)
}
