package ru.marwinka.androidarchsample.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterDao
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterEntity
import ru.marwinka.androidarchsample.feature.characters.data.local.FavoriteEntity

@Database(
    entities = [CharacterEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}
