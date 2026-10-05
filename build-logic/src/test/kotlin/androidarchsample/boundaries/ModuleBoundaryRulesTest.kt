package androidarchsample.boundaries

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleBoundaryRulesTest {
    private fun allowed(
        from: String,
        to: String,
    ) = assertNull("$from -> $to", ModuleBoundaryRules.violation(from, to))

    private fun forbidden(
        from: String,
        to: String,
        expected: String,
    ) {
        val reason = ModuleBoundaryRules.violation(from, to)
        assertNotNull("$from -> $to must be forbidden", reason)
        assertTrue("expected '$expected' in '$reason'", reason!!.contains(expected))
    }

    @Test
    fun `presentation and data of one feature do not see each other`() {
        allowed(":feature:a:presentation", ":feature:a:domain")
        allowed(":feature:a:data", ":feature:a:domain")
        forbidden(":feature:a:presentation", ":feature:a:data", "presentation must not depend on data")
        forbidden(":feature:a:data", ":feature:a:presentation", "data must not depend on presentation")
    }

    @Test
    fun `domain sees only core common and api modules`() {
        allowed(":feature:a:domain", ":core:common")
        allowed(":feature:a:domain", ":feature:a:api")
        allowed(":feature:a:domain", ":feature:b:api")
        forbidden(":feature:a:domain", ":core:network", "domain may depend only on")
        forbidden(":feature:a:domain", ":core:settings", "domain may depend only on")
        forbidden(":feature:a:domain", ":design-system", "domain may depend only on")
    }

    @Test
    fun `features see each other only through api`() {
        allowed(":feature:a:data", ":feature:b:api")
        forbidden(":feature:a:data", ":feature:b:domain", "only through its api module")
        forbidden(":feature:a:domain", ":feature:b:domain", "only through its api module")
        forbidden(":feature:a:presentation", ":feature:b:presentation", "only through its api module")
        forbidden(":feature:a:presentation", ":feature:b:api", "only through its api module")
    }

    @Test
    fun `api depends only on core common`() {
        allowed(":feature:a:api", ":core:common")
        forbidden(":feature:a:api", ":feature:a:domain", "api may depend only on core:common")
        forbidden(":feature:a:api", ":core:network", "api may depend only on core:common")
    }

    @Test
    fun `core and design-system do not know features`() {
        allowed(":core:network", ":core:common")
        allowed(":design-system", ":core:common")
        forbidden(":core:network", ":feature:a:data", "core modules must not depend")
        forbidden(":core:network", ":app", "core modules must not depend")
        forbidden(":core:common", ":core:network", "core:common must not depend")
        forbidden(":design-system", ":feature:a:domain", "design-system must not depend")
    }

    @Test
    fun `presentation sees design-system and core common only`() {
        allowed(":feature:a:presentation", ":design-system")
        allowed(":feature:a:presentation", ":core:common")
        forbidden(":feature:a:presentation", ":core:network", "presentation must not depend on this module")
    }

    @Test
    fun `di module is wired only by the app`() {
        allowed(":app", ":feature:a:di")
        allowed(":feature:a:di", ":feature:a:data")
        forbidden(":feature:a:presentation", ":feature:a:di", "di module is wired only by the app")
    }

    @Test
    fun `modules outside the layout are reported`() {
        assertNotNull(ModuleBoundaryRules.violation(":shared", ":core:common"))
        assertNotNull(ModuleBoundaryRules.violation(":app", ":feature:a:utils"))
    }

    @Test
    fun `cycles are reported`() {
        val graph =
            mapOf(
                ":core:network" to setOf(":core:settings"),
                ":core:settings" to setOf(":core:network"),
            )

        assertEquals(
            listOf(listOf(":core:network", ":core:settings", ":core:network")),
            ModuleBoundaryRules.findCycles(graph),
        )
    }

    @Test
    fun `the reference layout passes`() {
        val graph =
            mapOf(
                ":app" to setOf(":feature:a:data", ":feature:a:presentation", ":core:network"),
                ":feature:a:data" to setOf(":feature:a:domain", ":core:network", ":core:common"),
                ":feature:a:presentation" to setOf(":feature:a:domain", ":design-system", ":core:common"),
                ":feature:a:domain" to setOf(":core:common"),
                ":core:network" to setOf(":core:common"),
                ":core:testing" to setOf(":core:common"),
            )

        assertEquals(emptyList<String>(), ModuleBoundaryRules.check(graph))
    }
}
