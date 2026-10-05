package ru.marwinka.androidarchsample.feature.characters.presentation.detail

import ru.marwinka.androidarchsample.feature.characters.domain.model.Character

/** Error kinds of the detail screen; the screen picks the text. */
enum class CharacterDetailError {
    /** Favorite change was not saved. */
    FAVORITE_FAILED,
}

data class CharacterDetailUiState(
    val character: Character? = null,
    /** No emission from the cache yet. */
    val isLoading: Boolean = true,
    /** One-off snackbar message; cleared by `onMessageShown`. */
    val message: CharacterDetailError? = null,
)
