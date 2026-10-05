package ru.marwinka.androidarchsample.feature.characters.domain.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository

/** In-memory [CharacterRepository] for tests. */
class FakeCharacterRepository(
    initialCharacters: List<Character> = emptyList(),
) : CharacterRepository {
    /** Emulates the local cache; tests may set it directly. */
    val characters = MutableStateFlow(initialCharacters)

    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    /** When set, [refresh] suspends until it completes; lets tests observe the in-progress state. */
    var refreshGate: CompletableDeferred<Unit>? = null

    /** Written to the cache by a successful [refresh]. */
    var remoteCharacters: List<Character>? = null

    var toggleFavoriteResult: AppResult<Unit> = AppResult.Success(Unit)

    var refreshCallCount: Int = 0
        private set

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
        refreshGate?.await()
        val result = refreshResult
        if (result is AppResult.Success) remoteCharacters?.let { characters.value = it }
        return result
    }

    override suspend fun toggleFavorite(id: Int): AppResult<Unit> {
        val result = toggleFavoriteResult
        if (result is AppResult.Success) {
            characters.value = characters.value.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
        return result
    }
}
