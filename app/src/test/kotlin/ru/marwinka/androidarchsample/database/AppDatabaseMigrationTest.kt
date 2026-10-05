package ru.marwinka.androidarchsample.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Test
    fun `1 to 2 keeps cached characters and adds an empty favorites table`() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                "INSERT INTO characters (id, name, species, imageUrl) VALUES (1, 'Rick Sanchez', 'Human', 'rick.png')",
            )
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true).use { db ->
            db.query("SELECT name FROM characters").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Rick Sanchez", cursor.getString(0))
            }
            db.query("SELECT COUNT(*) FROM favorites").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
