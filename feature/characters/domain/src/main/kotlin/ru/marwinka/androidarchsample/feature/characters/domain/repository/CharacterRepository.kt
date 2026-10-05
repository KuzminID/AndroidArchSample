package ru.marwinka.androidarchsample.feature.characters.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.marwinka.androidarchsample.core.common.AppResult
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder

/** Characters: `observe*` read the local cache, [refresh] loads it from the network. */
interface CharacterRepository {
    fun observeCharacters(sortOrder: SortOrder): Flow<List<Character>>

    fun observeCharacter(id: Int): Flow<Character?>

    suspend fun refresh(): AppResult<Unit>

    /** Adds the character to favorites or removes it from them. */
    suspend fun toggleFavorite(id: Int): AppResult<Unit>
}
