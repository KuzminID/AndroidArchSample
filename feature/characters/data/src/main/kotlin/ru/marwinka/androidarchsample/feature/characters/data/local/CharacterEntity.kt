package ru.marwinka.androidarchsample.feature.characters.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

// Entities and the DAO are public: AppDatabase in :app lists them.

/** Cached character; the whole table is replaced on refresh. */
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val imageUrl: String,
)

/**
 * User's favorite. Kept apart from the cache, without a foreign key, so that replacing
 * the cache does not lose favorites.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val characterId: Int,
)

data class CharacterWithFavorite(
    @Embedded val character: CharacterEntity,
    val isFavorite: Boolean,
)
