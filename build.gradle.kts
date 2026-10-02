plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        filter {
            exclude { entry -> entry.file.path.contains("${File.separator}build${File.separator}") }
        }
    }
}

// Checks module dependency rules. Test configurations are excluded.
gradle.projectsEvaluated {
    val featureLayer = Regex("^:feature:([^:]+):(domain|data|presentation)$")
    val violations = mutableListOf<String>()
    subprojects.forEach { module ->
        val from = module.path
        val fromFeature = featureLayer.matchEntire(from)
        module.configurations
            .filter { !it.name.contains("test", ignoreCase = true) }
            .flatMap { it.dependencies.withType<ProjectDependency>() }
            .map { it.path }
            .distinct()
            .forEach { to ->
                val toFeature = featureLayer.matchEntire(to)
                val reason =
                    when {
                        from.startsWith(":core:") && (toFeature != null || to == ":app") ->
                            "core modules must not know about features or the app"
                        fromFeature == null || toFeature == null -> null
                        fromFeature.groupValues[2] == "domain" ->
                            "domain must not depend on other feature modules"
                        toFeature.groupValues[2] == "data" && fromFeature.groupValues[2] == "presentation" ->
                            "presentation must not depend on data"
                        fromFeature.groupValues[1] != toFeature.groupValues[1] && toFeature.groupValues[2] != "domain" ->
                            "features may only see another feature's domain contract"
                        else -> null
                    }
                if (reason != null) violations += "$from -> $to: $reason"
            }
    }
    if (violations.isNotEmpty()) {
        throw GradleException("Module boundary violations:\n" + violations.joinToString("\n") { "  - $it" })
    }
}

// Creates a new feature module set. Usage: ./gradlew newFeature -PfeatureName=movies
tasks.register("newFeature") {
    group = "template"
    description = "Scaffolds feature/<name>/{domain,data,presentation} modules wired like feature/characters."

    doLast {
        val rawName =
            (project.findProperty("featureName") as String?)
                ?: throw GradleException("Pass -PfeatureName=<name>, e.g. ./gradlew newFeature -PfeatureName=movies")

        if (!rawName.matches(Regex("^[a-z][a-z0-9]*([-_][a-z0-9]+)*$"))) {
            throw GradleException(
                "featureName must be lowercase words separated by '-' or '_' (e.g. 'movies', 'movie-list'), got '$rawName'",
            )
        }

        val words = rawName.split('-', '_')
        val moduleName = rawName
        val packageSegment = words.joinToString("")
        val className = words.joinToString("") { it.replaceFirstChar(Char::uppercase) }
        val baseFunctionName = className.replaceFirstChar(Char::lowercase)
        val basePackage = "ru.marwinka.androidarchsample"
        val featurePackage = "$basePackage.feature.$packageSegment"
        val featurePath = ":feature:$moduleName"

        val featureDir = file("feature/$moduleName")
        if (featureDir.exists()) {
            throw GradleException("$featureDir already exists")
        }

        fun sourceDir(
            layer: String,
            subPackage: String = "",
        ): File {
            val pkg = listOf("$featurePackage.$layer", subPackage).filter { it.isNotEmpty() }.joinToString(".")
            return featureDir.resolve("$layer/src/main/kotlin/${pkg.replace('.', '/')}").apply { mkdirs() }
        }

        // domain
        featureDir.resolve("domain/build.gradle.kts").apply { parentFile.mkdirs() }.writeText(
            """
            plugins {
                id("androidarchsample.android.library")
            }

            android {
                namespace = "$featurePackage.domain"
            }

            dependencies {
                implementation(project(":core:common"))
                implementation(libs.kotlinx.coroutines.core)

                testImplementation(libs.kotlinx.coroutines.test)
                testImplementation(libs.turbine)
                testImplementation(project(":core:testing"))
            }

            """.trimIndent(),
        )
        sourceDir("domain", "repository").resolve("${className}Repository.kt").writeText(
            """
            package $featurePackage.domain.repository

            import kotlinx.coroutines.flow.Flow

            /** TODO: describe what this feature reads and writes; replace the placeholder item type. */
            interface ${className}Repository {
                fun observeItems(): Flow<List<String>>
            }

            """.trimIndent(),
        )

        // data
        featureDir.resolve("data/build.gradle.kts").apply { parentFile.mkdirs() }.writeText(
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

                implementation(libs.kotlinx.coroutines.android)

                testImplementation(libs.kotlinx.coroutines.test)
                testImplementation(libs.turbine)
                testImplementation(project(":core:testing"))
            }

            """.trimIndent(),
        )
        sourceDir("data", "repository").resolve("${className}RepositoryImpl.kt").writeText(
            """
            package $featurePackage.data.repository

            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOf
            import $featurePackage.domain.repository.${className}Repository
            import javax.inject.Inject

            internal class ${className}RepositoryImpl
                @Inject
                constructor() : ${className}Repository {
                    // TODO: read from a DAO in this module and/or core:network instead of a placeholder
                    override fun observeItems(): Flow<List<String>> = flowOf(emptyList())
                }

            """.trimIndent(),
        )
        sourceDir("data", "di").resolve("${className}DataModule.kt").writeText(
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

            """.trimIndent(),
        )

        // presentation
        featureDir.resolve("presentation/build.gradle.kts").apply { parentFile.mkdirs() }.writeText(
            """
            plugins {
                id("androidarchsample.android.library")
                id("androidarchsample.android.compose")
                id("androidarchsample.hilt")
            }

            android {
                namespace = "$featurePackage.presentation"

                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:ui"))
                implementation(project("$featurePath:domain"))

                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)

                implementation(libs.hilt.navigation.compose)

                testImplementation(libs.turbine)
                testImplementation(libs.kotlinx.coroutines.test)
                testImplementation(project(":core:testing"))
            }

            """.trimIndent(),
        )
        val presentationDir = sourceDir("presentation")
        presentationDir.resolve("${className}UiState.kt").writeText(
            """
            package $featurePackage.presentation

            data class ${className}UiState(
                val items: List<String> = emptyList(),
                val isLoading: Boolean = true,
            )

            """.trimIndent(),
        )
        presentationDir.resolve("${className}ViewModel.kt").writeText(
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

            @HiltViewModel
            class ${className}ViewModel
                @Inject
                constructor(
                    repository: ${className}Repository,
                ) : ViewModel() {
                    val uiState: StateFlow<${className}UiState> =
                        repository
                            .observeItems()
                            .map { items -> ${className}UiState(items = items, isLoading = false) }
                            .stateIn(
                                scope = viewModelScope,
                                started = SharingStarted.WhileSubscribed(5_000),
                                initialValue = ${className}UiState(),
                            )
                }

            """.trimIndent(),
        )
        presentationDir.resolve("${className}Screen.kt").writeText(
            """
            package $featurePackage.presentation

            import androidx.compose.foundation.layout.Box
            import androidx.compose.foundation.layout.fillMaxSize
            import androidx.compose.runtime.Composable
            import androidx.compose.runtime.getValue
            import androidx.compose.ui.Modifier
            import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
            import androidx.lifecycle.compose.collectAsStateWithLifecycle
            import ru.marwinka.androidarchsample.core.ui.components.FullScreenLoading

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
                    if (uiState.isLoading) {
                        FullScreenLoading()
                    }
                    // TODO: render $className content
                }
            }

            """.trimIndent(),
        )
        sourceDir("presentation", "navigation").resolve("${className}NavGraph.kt").writeText(
            """
            package $featurePackage.presentation.navigation

            import androidx.navigation.NavGraphBuilder
            import androidx.navigation.compose.composable
            import $featurePackage.presentation.${className}Route

            object ${className}Destinations {
                const val ROUTE = "$packageSegment"
            }

            fun NavGraphBuilder.${baseFunctionName}NavGraph() {
                composable(${className}Destinations.ROUTE) {
                    ${className}Route()
                }
            }

            """.trimIndent(),
        )

        featureDir.resolve("README.md").writeText(
            """
            # feature:$moduleName

            Generated by `./gradlew newFeature -PfeatureName=$rawName`: three modules wired like
            `feature:characters`, with a placeholder repository. Finish the feature by hand:

            1. Replace the placeholder `${className}Repository` contract in `domain` with the real
               model and operations; add use cases only where they carry logic.
            2. Implement `${className}RepositoryImpl` in `data` against `core:*` (add a new `core:*`
               module first if the feature needs a new data source).
            3. Render real content in `${className}Screen.kt`.
            4. Add `implementation(project("$featurePath:data"))` and
               `implementation(project("$featurePath:presentation"))` to `app/build.gradle.kts`.
            5. Call `${baseFunctionName}NavGraph()` from `MainActivity`'s `NavHost`.
            6. Write unit tests for the repository and the ViewModel, same as `feature/characters`.

            """.trimIndent(),
        )

        val settingsFile = rootDir.resolve("settings.gradle.kts")
        val settings = settingsFile.readText()
        val includeLines =
            listOf("domain", "data", "presentation")
                .map { "include(\"$featurePath:$it\")" }
                .filter { it !in settings }
        if (includeLines.isNotEmpty()) {
            settingsFile.appendText(includeLines.joinToString(separator = "\n", postfix = "\n"))
        }

        logger.lifecycle("Created feature/$moduleName/{domain,data,presentation} and added them to settings.gradle.kts.")
        logger.lifecycle("Next steps are listed in feature/$moduleName/README.md.")
    }
}
