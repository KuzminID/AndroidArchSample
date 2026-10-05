package ru.marwinka.androidarchsample.feature.characters.presentation.list

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import ru.marwinka.androidarchsample.designsystem.components.FullScreenError
import ru.marwinka.androidarchsample.designsystem.components.FullScreenLoading
import ru.marwinka.androidarchsample.designsystem.theme.AppTheme
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.domain.model.SortOrder
import ru.marwinka.androidarchsample.feature.characters.presentation.R

@Composable
fun CharactersListRoute(
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharactersListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharactersListScreen(
        uiState = uiState,
        onCharacterClick = onCharacterClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onRefresh = viewModel::onRefresh,
        onMessageShown = viewModel::onMessageShown,
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
    onRefresh: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = uiState.message?.let { stringResource(it.messageRes()) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.characters_title)) },
                actions = { SortOrderButton(uiState.sortOrder, onSortOrderSelected) },
            )
        },
    ) { paddingValues ->
        val contentModifier = Modifier.fillMaxSize().padding(paddingValues)
        when {
            uiState.isInitialLoading -> {
                FullScreenLoading(contentModifier)
            }

            uiState.characters.isEmpty() && uiState.refreshError != null -> {
                FullScreenError(
                    message = stringResource(uiState.refreshError.messageRes()),
                    onRetry = onRefresh,
                    modifier = contentModifier,
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = contentModifier,
                ) {
                    if (uiState.characters.isEmpty()) {
                        Text(
                            text = stringResource(R.string.characters_empty),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
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
}

@Composable
private fun SortOrderButton(
    sortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
) {
    val (label, next) =
        when (sortOrder) {
            SortOrder.NAME_ASC -> R.string.characters_sort_name_asc to SortOrder.NAME_DESC
            SortOrder.NAME_DESC -> R.string.characters_sort_name_desc to SortOrder.NAME_ASC
        }
    TextButton(onClick = { onSortOrderSelected(next) }) {
        Text(stringResource(label))
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
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (character.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(R.string.characters_toggle_favorite),
                )
            }
        },
    )
}

@StringRes
private fun CharactersListError.messageRes(): Int =
    when (this) {
        CharactersListError.NETWORK -> R.string.characters_error_network
        CharactersListError.REFRESH_FAILED -> R.string.characters_error_refresh_failed
        CharactersListError.ACTION_FAILED -> R.string.characters_error_action_failed
    }

private val previewCharacters =
    listOf(
        Character(1, "Rick Sanchez", "Human", "", isFavorite = true),
        Character(2, "Morty Smith", "Human", "", isFavorite = false),
    )

@Composable
private fun PreviewScreen(uiState: CharactersListUiState) {
    AppTheme {
        CharactersListScreen(
            uiState = uiState,
            onCharacterClick = {},
            onFavoriteClick = {},
            onSortOrderSelected = {},
            onRefresh = {},
            onMessageShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharactersListContentPreview() {
    PreviewScreen(CharactersListUiState(characters = previewCharacters, isInitialLoading = false))
}

@Preview(showBackground = true)
@Composable
private fun CharactersListRefreshingPreview() {
    PreviewScreen(CharactersListUiState(characters = previewCharacters, isInitialLoading = false, isRefreshing = true))
}

@Preview(showBackground = true)
@Composable
private fun CharactersListLoadingPreview() {
    PreviewScreen(CharactersListUiState(isInitialLoading = true))
}

@Preview(showBackground = true)
@Composable
private fun CharactersListErrorPreview() {
    PreviewScreen(CharactersListUiState(isInitialLoading = false, refreshError = CharactersListError.NETWORK))
}

@Preview(showBackground = true)
@Composable
private fun CharactersListEmptyPreview() {
    PreviewScreen(CharactersListUiState(isInitialLoading = false))
}
