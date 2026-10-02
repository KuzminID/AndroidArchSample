package ru.marwinka.androidarchsample.domain.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.domain.repository.CharacterRepository

/** In-memory [CharacterRepository] for tests. */
class FakeCharacterRepository(
    initialCharacters: List<Character> = emptyList(),
) : CharacterRepository {
    private val characters = MutableStateFlow(initialCharacters)
    private val sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCallCount: Int = 0

    override fun observeCharacters(sortOrder: SortOrder): Flow<List<Character>> =
        characters.map { list ->
            when (sortOrder) {
                SortOrder.NAME_ASC -> list.sortedBy { it.name }
                SortOrder.NAME_DESC -> list.sortedByDescending { it.name }
            }
        }

    override fun observeCharacter(id: Int): Flow<Character?> =
        characters.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCallCount++
        return refreshResult
    }

    override fun observeSortOrder(): Flow<SortOrder> = sortOrder

    override suspend fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    override suspend fun toggleFavorite(id: Int) {
        characters.value =
            characters.value.map {
                if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
            }
    }
}
