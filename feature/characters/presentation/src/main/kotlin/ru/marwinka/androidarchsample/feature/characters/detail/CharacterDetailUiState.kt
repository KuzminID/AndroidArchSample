package ru.marwinka.androidarchsample.feature.characters.detail

import ru.marwinka.androidarchsample.domain.model.Character

data class CharacterDetailUiState(
    val character: Character? = null,
    val isLoading: Boolean = false,
)
