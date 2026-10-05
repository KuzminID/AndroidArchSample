package ru.marwinka.androidarchsample.feature.characters.presentation.detail

import androidx.annotation.StringRes
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import ru.marwinka.androidarchsample.designsystem.components.FullScreenLoading
import ru.marwinka.androidarchsample.designsystem.theme.AppTheme
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character
import ru.marwinka.androidarchsample.feature.characters.presentation.R

@Composable
fun CharacterDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

@Composable
fun CharacterDetailScreen(
    uiState: CharacterDetailUiState,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
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
        topBar = { DetailTopBar(uiState.character, onBackClick, onFavoriteClick) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val character = uiState.character
            when {
                uiState.isLoading -> {
                    FullScreenLoading()
                }

                character == null -> {
                    Text(
                        text = stringResource(R.string.characters_not_found),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    CharacterDetails(character)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTopBar(
    character: Character?,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(character?.name.orEmpty()) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.characters_back),
                )
            }
        },
        actions = {
            if (character != null) {
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

@Composable
private fun CharacterDetails(character: Character) {
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

@StringRes
private fun CharacterDetailError.messageRes(): Int =
    when (this) {
        CharacterDetailError.FAVORITE_FAILED -> R.string.characters_error_action_failed
    }

@Composable
private fun PreviewScreen(uiState: CharacterDetailUiState) {
    AppTheme {
        CharacterDetailScreen(uiState = uiState, onBackClick = {}, onFavoriteClick = {}, onMessageShown = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailContentPreview() {
    PreviewScreen(
        CharacterDetailUiState(
            character = Character(1, "Rick Sanchez", "Human", "", isFavorite = true),
            isLoading = false,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailLoadingPreview() {
    PreviewScreen(CharacterDetailUiState(isLoading = true))
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailNotFoundPreview() {
    PreviewScreen(CharacterDetailUiState(isLoading = false))
}
