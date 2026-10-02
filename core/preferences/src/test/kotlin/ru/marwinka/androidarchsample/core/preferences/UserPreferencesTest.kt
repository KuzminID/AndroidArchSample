package ru.marwinka.androidarchsample.core.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class UserPreferencesTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.preferences(): UserPreferences =
        UserPreferences(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(folder.root, "test.preferences_pb") },
            ),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sort order defaults to NAME_ASC`() =
        runTest(UnconfinedTestDispatcher()) {
            assertEquals(DEFAULT_SORT_ORDER, preferences().observeSortOrder().first())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setSortOrder is observed`() =
        runTest(UnconfinedTestDispatcher()) {
            val preferences = preferences()

            preferences.setSortOrder("NAME_DESC")

            assertEquals("NAME_DESC", preferences.observeSortOrder().first())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavorite adds then removes the id`() =
        runTest(UnconfinedTestDispatcher()) {
            val preferences = preferences()

            preferences.toggleFavorite("42")
            assertEquals(setOf("42"), preferences.observeFavoriteIds().first())

            preferences.toggleFavorite("7")
            assertEquals(setOf("42", "7"), preferences.observeFavoriteIds().first())

            preferences.toggleFavorite("42")
            assertEquals(setOf("7"), preferences.observeFavoriteIds().first())
        }
}
