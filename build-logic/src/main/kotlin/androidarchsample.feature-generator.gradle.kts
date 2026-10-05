// Scaffolds a feature like feature/characters. Usage: ./gradlew newFeature -PfeatureName=movies
// Applied to the root project.

tasks.register("newFeature") {
    group = "template"
    description = "Scaffolds feature/<name>/{domain,data,presentation} modules wired like feature/characters."

    val featureNameProperty = providers.gradleProperty("featureName")
    val root = layout.projectDirectory.asFile
    val basePackage = "ru.marwinka.androidarchsample"

    doLast {
        val rawName =
            featureNameProperty.orNull
                ?: throw GradleException("Pass -PfeatureName=<name>, e.g. ./gradlew newFeature -PfeatureName=movies")

        if (!rawName.matches(Regex("^[a-z][a-z0-9]*([-_][a-z0-9]+)*$"))) {
            throw GradleException(
                "featureName must be lowercase words separated by '-' or '_' (e.g. 'movies', 'movie-list'), got '$rawName'",
            )
        }

        val words = rawName.split('-', '_')
        val packageSegment = words.joinToString("")
        val className = words.joinToString("") { it.replaceFirstChar(Char::uppercase) }
        val functionName = className.replaceFirstChar(Char::lowercase)
        val featurePackage = "$basePackage.feature.$packageSegment"
        val featurePath = ":feature:$rawName"

        val featureDir = root.resolve("feature/$rawName")
        if (featureDir.exists()) throw GradleException("$featureDir already exists")

        fun write(
            path: String,
            content: String,
        ) = featureDir.resolve(path).apply { parentFile.mkdirs() }.writeText(content.trimIndent() + "\n")

        fun source(
            layer: String,
            subPackage: String,
            fileName: String,
            content: String,
        ) {
            val pkg = listOf("$featurePackage.$layer", subPackage).filter { it.isNotEmpty() }.joinToString(".")
            write("$layer/src/main/kotlin/${pkg.replace('.', '/')}/$fileName", content)
        }

        // domain
        write(
            "domain/build.gradle.kts",
            """
            plugins {
                id("androidarchsample.jvm.library")
            }

            dependencies {
                api(project(":core:common"))

                testImplementation(project(":core:testing"))
                testImplementation(libs.turbine)
            }
            """,
        )
        source(
            "domain",
            "repository",
            "${className}Repository.kt",
            """
            package $featurePackage.domain.repository

            import kotlinx.coroutines.flow.Flow
            import ru.marwinka.androidarchsample.core.common.AppResult

            /** Placeholder contract: replace the item type with the feature's model. */
            interface ${className}Repository {
                fun observeItems(): Flow<List<String>>

                suspend fun refresh(): AppResult<Unit>
            }
            """,
        )

        // data
        write(
            "data/build.gradle.kts",
            """
            plugins {
                id("androidarchsample.android.library")
                id("androidarchsample.hilt")
            }

            android {
                namespace = "$featurePackage.data"
            }

            dependencies {
                implementation(project(":core:common"))
                implementation(project("$featurePath:domain"))

                testImplementation(project(":core:testing"))
                testImplementation(libs.turbine)
            }
            """,
        )
        source(
            "data",
            "repository",
            "${className}RepositoryImpl.kt",
            """
            package $featurePackage.data.repository

            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf
            import ru.marwinka.androidarchsample.core.common.AppResult
            import $featurePackage.domain.repository.${className}Repository
            import javax.inject.Inject

            internal class ${className}RepositoryImpl
                @Inject
                constructor() : ${className}Repository {
                    // Placeholder: read from this feature's DAO; fill it from its API in refresh() via appResultOf
                    override fun observeItems(): Flow<List<String>> = flowOf(emptyList())

                    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
                }
            """,
        )
        source(
            "data",
            "di",
            "${className}DataModule.kt",
            """
            package $featurePackage.data.di

            import dagger.Binds
            import dagger.Module
            import dagger.hilt.InstallIn
            import dagger.hilt.components.SingletonComponent
            import $featurePackage.data.repository.${className}RepositoryImpl
            import $featurePackage.domain.repository.${className}Repository
            import javax.inject.Singleton

            @Module
            @InstallIn(SingletonComponent::class)
            internal abstract class ${className}DataModule {
                @Binds
                @Singleton
                abstract fun bind${className}Repository(impl: ${className}RepositoryImpl): ${className}Repository
            }
            """,
        )

        // presentation
        write(
            "presentation/build.gradle.kts",
            """
            plugins {
                id("androidarchsample.android.library")
                id("androidarchsample.android.compose")
                id("androidarchsample.hilt")
                alias(libs.plugins.kotlin.serialization)
            }

            android {
                namespace = "$featurePackage.presentation"

                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                implementation(project(":core:common"))
                implementation(project(":design-system"))
                implementation(project("$featurePath:domain"))

                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.hilt.navigation.compose)

                testImplementation(project(":core:testing"))
                testImplementation(libs.turbine)
            }
            """,
        )
        source(
            "presentation",
            "",
            "${className}UiState.kt",
            """
            package $featurePackage.presentation

            data class ${className}UiState(
                val items: List<String> = emptyList(),
                val isInitialLoading: Boolean = true,
            )
            """,
        )
        source(
            "presentation",
            "",
            "${className}ViewModel.kt",
            """
            package $featurePackage.presentation

            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewModelScope
            import dagger.hilt.android.lifecycle.HiltViewModel
            import kotlinx.coroutines.flow.SharingStarted
            import kotlinx.coroutines.flow.StateFlow
            import kotlinx.coroutines.flow.map
            import kotlinx.coroutines.flow.stateIn
            import $featurePackage.domain.repository.${className}Repository
            import javax.inject.Inject

            // Placeholder: add refresh and errors like CharactersListViewModel
            @HiltViewModel
            class ${className}ViewModel
                @Inject
                constructor(
                    repository: ${className}Repository,
                ) : ViewModel() {
                    val uiState: StateFlow<${className}UiState> =
                        repository
                            .observeItems()
                            .map { items -> ${className}UiState(items = items, isInitialLoading = false) }
                            .stateIn(
                                scope = viewModelScope,
                                started = SharingStarted.WhileSubscribed(5_000),
                                initialValue = ${className}UiState(),
                            )
                }
            """,
        )
        source(
            "presentation",
            "",
            "${className}Screen.kt",
            """
            package $featurePackage.presentation

            import androidx.compose.foundation.layout.Box
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.getValue
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.tooling.preview.Preview
            import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            import ru.marwinka.androidarchsample.designsystem.components.FullScreenLoading
            import ru.marwinka.androidarchsample.designsystem.theme.AppTheme

            @Composable
            fun ${className}Route(
                modifier: Modifier = Modifier,
                viewModel: ${className}ViewModel = hiltViewModel(),
            ) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                ${className}Screen(uiState = uiState, modifier = modifier)
            }

            @Composable
            fun ${className}Screen(
                uiState: ${className}UiState,
                modifier: Modifier = Modifier,
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    if (uiState.isInitialLoading) {
                        FullScreenLoading()
                    }
                    // Placeholder: render $className content
                }
            }

            @Preview(showBackground = true)
            @Composable
            private fun ${className}ScreenLoadingPreview() {
                AppTheme {
                    ${className}Screen(uiState = ${className}UiState(isInitialLoading = true))
                }
            }
            """,
        )
        source(
            "presentation",
            "navigation",
            "${className}Destination.kt",
            """
            package $featurePackage.presentation.navigation

            import kotlinx.serialization.Serializable

            @Serializable
            data object ${className}Destination
            """,
        )
        source(
            "presentation",
            "navigation",
            "${className}NavGraph.kt",
            """
            package $featurePackage.presentation.navigation

            import androidx.navigation.NavGraphBuilder
            import androidx.navigation.compose.composable
            import $featurePackage.presentation.${className}Route

            fun NavGraphBuilder.${functionName}NavGraph() {
                composable<${className}Destination> {
                    ${className}Route()
                }
            }
            """,
        )

        write(
            "README.md",
            """
            # feature:$rawName

            Generated by `./gradlew newFeature -PfeatureName=$rawName`: three modules wired like
            `feature/characters`, with a placeholder repository. Finish the feature by hand:

            1. Replace the placeholder in `${className}Repository` with the real model and operations;
               add a use case only where it carries logic.
            2. Implement `${className}RepositoryImpl`: Entity/DAO and the network interface with DTOs live
               in `data`; add the entities to `AppDatabase` with a migration and its test.
            3. Derive the screen state like `CharactersListViewModel` and render real content.
            4. Add `implementation(project("$featurePath:data"))` and
               `implementation(project("$featurePath:presentation"))` to `app/build.gradle.kts`.
            5. Call `${functionName}NavGraph()` from `MainActivity`'s `NavHost`.
            6. Write tests like `feature/characters`: repository on an in-memory database, ViewModel with fakes.
            """,
        )

        val settingsFile = root.resolve("settings.gradle.kts")
        val settings = settingsFile.readText()
        val includeLines =
            listOf("domain", "data", "presentation")
                .map { "include(\"$featurePath:$it\")" }
                .filter { it !in settings }
        if (includeLines.isNotEmpty()) {
            settingsFile.appendText(includeLines.joinToString(separator = "\n", postfix = "\n"))
        }

        logger.lifecycle("Created feature/$rawName/{domain,data,presentation} and added them to settings.gradle.kts.")
        logger.lifecycle("Next steps are listed in feature/$rawName/README.md.")
    }
}
