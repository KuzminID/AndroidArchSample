package ru.marwinka.androidarchsample.feature.characters.presentation.list

import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder

/** Error kinds of the list screen; the screen picks the text. */
enum class CharactersListError {
    /** Network is unavailable. */
    NETWORK,

    /** Refresh failed for another reason. */
    REFRESH_FAILED,

    /** Favorite or sort order change was not saved. */
    ACTION_FAILED,
}

data class CharactersListUiState(
    val characters: List<Character> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    /** No data yet and the first refresh is running. */
    val isInitialLoading: Boolean = true,
    /** Refresh is running over data that is already shown. */
    val isRefreshing: Boolean = false,
    /** Last refresh failed; shown full screen when there is nothing else to show. */
    val refreshError: CharactersListError? = null,
    /** One-off snackbar message; cleared by `onMessageShown`. */
    val message: CharactersListError? = null,
)
