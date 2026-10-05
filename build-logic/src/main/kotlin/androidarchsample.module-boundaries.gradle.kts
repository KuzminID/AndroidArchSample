// Fails the build on forbidden module dependencies. Applied to the root project.
// Test configurations are excluded.
import androidarchsample.boundaries.Layer
import androidarchsample.boundaries.ModuleBoundaryRules
import androidarchsample.boundaries.ModuleKind

gradle.projectsEvaluated {
    val modules = rootProject.subprojects.filter { it.buildFile.exists() }
    val graph =
        modules.associate { module ->
            module.path to
                module.configurations
                    .filter { !it.name.contains("test", ignoreCase = true) }
                    .flatMap { it.dependencies.withType<ProjectDependency>() }
                    .map { it.path }
                    .filter { it != module.path }
                    .toSet()
        }

    // Domain and api are plain Kotlin/JVM modules, so Android API is not on their classpath.
    val platformViolations =
        modules
            .filter { module ->
                val kind = ModuleBoundaryRules.classify(module.path)
                kind is ModuleKind.Feature && kind.layer in setOf(Layer.DOMAIN, Layer.API) &&
                    (module.plugins.hasPlugin("com.android.library") || !module.plugins.hasPlugin("org.jetbrains.kotlin.jvm"))
            }.map { "${it.path}: domain and api must be Kotlin/JVM modules (androidarchsample.jvm.library)" }

    val violations = ModuleBoundaryRules.check(graph) + platformViolations
    if (violations.isNotEmpty()) {
        throw GradleException("Module boundary violations:\n" + violations.joinToString("\n") { "  - $it" })
    }
}
