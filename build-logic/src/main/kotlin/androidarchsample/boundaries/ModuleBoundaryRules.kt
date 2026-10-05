package androidarchsample.boundaries

/** Kind of a Gradle module in the project layout. */
sealed interface ModuleKind {
    data object App : ModuleKind

    data object DesignSystem : ModuleKind

    data class Core(
        val name: String,
    ) : ModuleKind

    data class Feature(
        val feature: String,
        val layer: Layer,
    ) : ModuleKind

    data class Unknown(
        val path: String,
    ) : ModuleKind
}

enum class Layer { API, DOMAIN, DATA, PRESENTATION, DI }

/**
 * Allowed dependencies between modules.
 *
 * Pure Kotlin so the rules are unit-tested without Gradle; the `androidarchsample.module-boundaries`
 * plugin feeds it the project graph.
 */
object ModuleBoundaryRules {
    private val featurePath = Regex("^:feature:([^:]+):(api|domain|data|presentation|di)$")
    private val corePath = Regex("^:core:([^:]+)$")

    fun classify(path: String): ModuleKind {
        featurePath.matchEntire(path)?.let { match ->
            return ModuleKind.Feature(match.groupValues[1], Layer.valueOf(match.groupValues[2].uppercase()))
        }
        corePath.matchEntire(path)?.let { match -> return ModuleKind.Core(match.groupValues[1]) }
        return when (path) {
            ":app" -> ModuleKind.App
            ":design-system" -> ModuleKind.DesignSystem
            else -> ModuleKind.Unknown(path)
        }
    }

    /** Returns why `from -> to` is forbidden, or `null` if it is allowed. */
    @Suppress("CyclomaticComplexMethod")
    fun violation(
        from: String,
        to: String,
    ): String? {
        val source = classify(from)
        val target = classify(to)
        if (source is ModuleKind.Unknown) return "$from is not part of the module layout"
        if (target is ModuleKind.Unknown) return "$to is not part of the module layout"
        if (source == ModuleKind.App) return null
        if (target is ModuleKind.Feature && target.layer == Layer.DI) {
            return "a feature's di module is wired only by the app"
        }
        return when (source) {
            is ModuleKind.Core -> coreViolation(source, target)
            ModuleKind.DesignSystem ->
                if (target is ModuleKind.Core) null else "design-system must not depend on features or the app"
            is ModuleKind.Feature -> featureViolation(source, target)
            else -> null
        }
    }

    private fun coreViolation(
        source: ModuleKind.Core,
        target: ModuleKind,
    ): String? =
        when {
            target !is ModuleKind.Core -> "core modules must not depend on features, design-system or the app"
            source.name == "common" -> "core:common must not depend on other core modules"
            else -> null
        }

    private fun featureViolation(
        source: ModuleKind.Feature,
        target: ModuleKind,
    ): String? {
        val sameFeature = target is ModuleKind.Feature && target.feature == source.feature
        val targetLayer = (target as? ModuleKind.Feature)?.layer
        val isCommon = target == ModuleKind.Core("common")
        val isForeignApi = target is ModuleKind.Feature && !sameFeature && targetLayer == Layer.API
        val allowed =
            when (source.layer) {
                Layer.API -> isCommon
                Layer.DOMAIN -> isCommon || targetLayer == Layer.API
                Layer.DATA ->
                    target is ModuleKind.Core ||
                        (sameFeature && targetLayer in setOf(Layer.DOMAIN, Layer.API)) ||
                        isForeignApi
                Layer.PRESENTATION ->
                    isCommon ||
                        target == ModuleKind.DesignSystem ||
                        (sameFeature && targetLayer in setOf(Layer.DOMAIN, Layer.API))
                Layer.DI -> target is ModuleKind.Core || sameFeature
            }
        if (allowed) return null
        return when {
            source.layer == Layer.PRESENTATION && sameFeature && targetLayer == Layer.DATA ->
                "presentation must not depend on data"
            source.layer == Layer.DATA && sameFeature && targetLayer == Layer.PRESENTATION ->
                "data must not depend on presentation"
            target is ModuleKind.Feature && !sameFeature ->
                "a feature may depend on another feature only through its api module"
            source.layer == Layer.DOMAIN -> "domain may depend only on core:common and api modules"
            source.layer == Layer.API -> "api may depend only on core:common"
            else -> "${source.layer.name.lowercase()} must not depend on this module"
        }
    }

    /** Checks every edge and reports dependency cycles. */
    fun check(graph: Map<String, Set<String>>): List<String> {
        val edgeViolations =
            graph.flatMap { (from, targets) ->
                targets.mapNotNull { to -> violation(from, to)?.let { "$from -> $to: $it" } }
            }
        return edgeViolations + findCycles(graph).map { "dependency cycle: ${it.joinToString(" -> ")}" }
    }

    fun findCycles(graph: Map<String, Set<String>>): List<List<String>> {
        val cycles = mutableListOf<List<String>>()
        val done = mutableSetOf<String>()
        val stack = ArrayDeque<String>()

        fun visit(node: String) {
            if (node in done) return
            val index = stack.indexOf(node)
            if (index >= 0) {
                cycles += stack.drop(index) + node
                return
            }
            stack.addLast(node)
            graph[node].orEmpty().sorted().forEach(::visit)
            stack.removeLast()
            done += node
        }
        graph.keys.sorted().forEach(::visit)
        return cycles
    }
}
