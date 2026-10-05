package ru.marwinka.androidarchsample.feature.characters.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import ru.marwinka.androidarchsample.feature.characters.presentation.detail.CharacterDetailRoute
import ru.marwinka.androidarchsample.feature.characters.presentation.list.CharactersListRoute

@Serializable
data object CharactersListDestination

@Serializable
data class CharacterDetailDestination(
    val characterId: Int,
)

fun NavGraphBuilder.charactersNavGraph(navController: NavHostController) {
    composable<CharactersListDestination> {
        CharactersListRoute(
            onCharacterClick = { characterId ->
                navController.navigate(CharacterDetailDestination(characterId))
            },
        )
    }
    composable<CharacterDetailDestination> {
        CharacterDetailRoute(onBackClick = navController::popBackStack)
    }
}
