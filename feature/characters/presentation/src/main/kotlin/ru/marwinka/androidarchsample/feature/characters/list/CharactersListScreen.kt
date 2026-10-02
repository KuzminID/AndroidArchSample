package ru.marwinka.androidarchsample.feature.characters.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coil3.compose.AsyncImage
import ru.marwinka.androidarchsample.core.ui.components.FullScreenError
import ru.marwinka.androidarchsample.core.ui.components.FullScreenLoading
import ru.marwinka.androidarchsample.core.ui.theme.AppTheme
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.R

@Composable
fun CharactersListRoute(
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharactersListViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(checkNotNull(
        LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharactersListScreen(
        uiState = uiState,
        onCharacterClick = onCharacterClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onRetry = viewModel::refresh,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersListScreen(
    uiState: CharactersListUiState,
    onCharacterClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
    onRetry: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Errors over a non-empty list are shown as a snackbar.
    val errorMessage = uiState.error?.let { stringResource(it.messageRes()) }
    LaunchedEffect(errorMessage, uiState.characters.isEmpty()) {
        if (errorMessage != null && uiState.characters.isNotEmpty()) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.characters_title)) },
                actions = {
                    val nextOrder =
                        if (uiState.sortOrder ==
                            SortOrder.NAME_ASC
                        ) {
                            SortOrder.NAME_DESC
                        } else {
                            SortOrder.NAME_ASC
                        }
                    IconButton(onClick = { onSortOrderSelected(nextOrder) }) {
                        Text(
                            stringResource(
                                if (uiState.sortOrder == SortOrder.NAME_ASC) {
                                    R.string.characters_sort_name_asc
                                } else {
                                    R.string.characters_sort_name_desc
                                },
                            ),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading && uiState.characters.isEmpty() -> FullScreenLoading()
                errorMessage != null && uiState.characters.isEmpty() ->
                    FullScreenError(message = errorMessage, onRetry = onRetry)

                else ->
                    LazyColumn {
                        items(uiState.characters, key = { it.id }) { character ->
                            CharacterRow(
                                character = character,
                                onClick = { onCharacterClick(character.id) },
                                onFavoriteClick = { onFavoriteClick(character.id) },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun CharacterRow(
    character: Character,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        headlineContent = { Text(character.name) },
        supportingContent = { Text(character.species) },
        leadingContent = {
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.size(48.dp),
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (character.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(R.string.characters_toggle_favorite),
                    )
                }
            }
        },
    )
}

private val previewCharacters =
    listOf(
        Character(1, "Rick Sanchez", "Human", "", isFavorite = true),
        Character(2, "Morty Smith", "Human", "", isFavorite = false),
    )

@Preview(showBackground = true)
@Composable
private fun CharactersListScreenPreview() {
    AppTheme {
        CharactersListScreen(
            uiState = CharactersListUiState(characters = previewCharacters),
            onCharacterClick = {},
            onFavoriteClick = {},
            onSortOrderSelected = {},
            onRetry = {},
            onErrorShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharactersListScreenLoadingPreview() {
    AppTheme {
        CharactersListScreen(
            uiState = CharactersListUiState(isLoading = true),
            onCharacterClick = {},
            onFavoriteClick = {},
            onSortOrderSelected = {},
            onRetry = {},
            onErrorShown = {},
        )
    }
}

private fun CharactersListError.messageRes(): Int =
    when (this) {
        CharactersListError.NETWORK -> R.string.characters_error_network
        CharactersListError.REFRESH_FAILED -> R.string.characters_error_refresh_failed
    }
