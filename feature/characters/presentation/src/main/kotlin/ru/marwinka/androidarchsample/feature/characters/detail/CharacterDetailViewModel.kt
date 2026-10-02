package ru.marwinka.androidarchsample.feature.characters.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.marwinka.androidarchsample.domain.usecase.GetCharacterDetailUseCase
import ru.marwinka.androidarchsample.domain.usecase.ToggleFavoriteUseCase
import ru.marwinka.androidarchsample.feature.characters.navigation.CharacterDetailDestination
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getCharacterDetail: GetCharacterDetailUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
    ) : ViewModel() {
        // Read by key instead of toRoute() to keep JVM tests free of Bundle.
        private val characterId: Int =
            checkNotNull(savedStateHandle.get<Int>(CharacterDetailDestination::characterId.name)) {
                "Missing ${CharacterDetailDestination::characterId.name} argument"
            }

        val uiState: StateFlow<CharacterDetailUiState> =
            getCharacterDetail(characterId)
                .map { character -> CharacterDetailUiState(character = character, isLoading = false) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = CharacterDetailUiState(isLoading = true),
                )

        fun onFavoriteClick() {
            viewModelScope.launch { toggleFavorite(characterId) }
        }
    }
