package ru.marwinka.androidarchsample.feature.characters.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.marwinka.androidarchsample.core.common.onFailure
import ru.marwinka.androidarchsample.feature.characters.domain.repository.CharacterRepository
import ru.marwinka.androidarchsample.feature.characters.presentation.navigation.CharacterDetailDestination
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: CharacterRepository,
    ) : ViewModel() {
        // Read by key instead of toRoute() to keep JVM tests free of Bundle.
        private val characterId: Int =
            checkNotNull(savedStateHandle.get<Int>(CharacterDetailDestination::characterId.name)) {
                "Missing ${CharacterDetailDestination::characterId.name} argument"
            }

        private val actionError = MutableStateFlow<CharacterDetailError?>(null)

        val uiState: StateFlow<CharacterDetailUiState> =
            combine(repository.observeCharacter(characterId), actionError) { character, message ->
                CharacterDetailUiState(character = character, isLoading = false, message = message)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CharacterDetailUiState(),
            )

        fun onFavoriteClick() {
            viewModelScope.launch {
                repository.toggleFavorite(characterId).onFailure {
                    actionError.value =
                        CharacterDetailError.FAVORITE_FAILED
                }
            }
        }

        fun onMessageShown() {
            actionError.value = null
        }
    }
