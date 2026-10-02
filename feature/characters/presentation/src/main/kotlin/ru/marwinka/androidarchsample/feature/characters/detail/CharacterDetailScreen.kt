package ru.marwinka.androidarchsample.feature.characters.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coil3.compose.AsyncImage
import ru.marwinka.androidarchsample.core.ui.components.FullScreenLoading
import ru.marwinka.androidarchsample.core.ui.theme.AppTheme
import ru.marwinka.androidarchsample.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.R

@Composable
fun CharacterDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterDetailViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(checkNotNull(
        LocalViewModelStoreOwner.current) {
                "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
            }, null),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    uiState: CharacterDetailUiState,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(uiState.character?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.characters_back),
                        )
                    }
                },
                actions = {
                    if (uiState.character != null) {
                        IconButton(onClick = onFavoriteClick) {
                            val icon =
                                if (uiState.character.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                }
                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(R.string.characters_toggle_favorite),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val character = uiState.character
            when {
                uiState.isLoading -> FullScreenLoading()
                character == null ->
                    Text(
                        text = stringResource(R.string.characters_not_found),
                        modifier = Modifier.align(Alignment.Center),
                    )

                else ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = character.imageUrl,
                            contentDescription = character.name,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().height(240.dp),
                        )
                        Text(
                            text = character.name,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Text(text = character.species)
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailScreenPreview() {
    AppTheme {
        CharacterDetailScreen(
            uiState =
                CharacterDetailUiState(
                    character = Character(1, "Rick Sanchez", "Human", "", isFavorite = true),
                ),
            onBackClick = {},
            onFavoriteClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailScreenLoadingPreview() {
    AppTheme {
        CharacterDetailScreen(
            uiState = CharacterDetailUiState(isLoading = true),
            onBackClick = {},
            onFavoriteClick = {},
        )
    }
}
