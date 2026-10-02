package ru.marwinka.androidarchsample.feature.characters.list

import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder

/** Error shown on the list screen. */
enum class CharactersListError {
    /** Network is unavailable. */
    NETWORK,
    REFRESH_FAILED,
}

data class CharactersListUiState(
    val characters: List<Character> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val isLoading: Boolean = false,
    val error: CharactersListError? = null,
)
