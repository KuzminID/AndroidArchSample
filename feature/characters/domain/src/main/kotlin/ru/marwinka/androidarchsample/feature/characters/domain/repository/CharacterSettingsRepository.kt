package ru.marwinka.androidarchsample.feature.characters.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder

/** User settings of the characters feature. */
interface CharacterSettingsRepository {
    fun observeSortOrder(): Flow<SortOrder>

    suspend fun setSortOrder(order: SortOrder): AppResult<Unit>
}
