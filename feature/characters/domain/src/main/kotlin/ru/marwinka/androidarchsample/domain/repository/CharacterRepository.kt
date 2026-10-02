package ru.marwinka.androidarchsample.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder

/** Character repository: [observeCharacters] reads the cache, [refresh] loads from the network. */
interface CharacterRepository {
    fun observeCharacters(sortOrder: SortOrder): Flow<List<Character>>

    fun observeCharacter(id: Int): Flow<Character?>

    suspend fun refresh(): AppResult<Unit>

    fun observeSortOrder(): Flow<SortOrder>

    suspend fun setSortOrder(order: SortOrder)

    suspend fun toggleFavorite(id: Int)
}
