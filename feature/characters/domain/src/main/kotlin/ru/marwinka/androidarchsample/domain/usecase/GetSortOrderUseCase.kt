package ru.marwinka.androidarchsample.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

class GetSortOrderUseCase(
    private val repository: CharacterRepository,
) {
    operator fun invoke(): Flow<SortOrder> = repository.observeSortOrder()
}
