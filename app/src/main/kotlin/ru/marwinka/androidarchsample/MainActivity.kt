package ru.marwinka.androidarchsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.marwinka.androidarchsample.core.ui.theme.AppTheme
import ru.marwinka.androidarchsample.feature.characters.navigation.CharactersListDestination
import ru.marwinka.androidarchsample.feature.characters.navigation.charactersNavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = CharactersListDestination,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    charactersNavGraph(navController)
                }
            }
        }
    }
}
