package ru.marwinka.androidarchsample.feature.characters.domain.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterSettingsRepository

/** In-memory [CharacterSettingsRepository] for tests. */
class FakeCharacterSettingsRepository(
    initialSortOrder: SortOrder = SortOrder.NAME_ASC,
) : CharacterSettingsRepository {
    private val sortOrder = MutableStateFlow(initialSortOrder)

    var setSortOrderResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observeSortOrder(): Flow<SortOrder> = sortOrder

    override suspend fun setSortOrder(order: SortOrder): AppResult<Unit> {
        val result = setSortOrderResult
        if (result is AppResult.Success) sortOrder.value = order
        return result
    }
}
