package ru.marwinka.androidarchsample.feature.characters.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterEntity
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterWithFavorite
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterDto
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character

class CharacterMappersTest {
    @Test
    fun `dto maps to entity, image becomes imageUrl`() {
        assertEquals(
            CharacterEntity(1, "Rick Sanchez", "Human", "rick.png"),
            CharacterDto(1, "Rick Sanchez", "Human", "rick.png").toEntity(),
        )
    }

    @Test
    fun `row maps to domain with its favorite flag`() {
        val row = CharacterWithFavorite(CharacterEntity(1, "Rick Sanchez", "Human", "rick.png"), isFavorite = true)

        assertEquals(Character(1, "Rick Sanchez", "Human", "rick.png", isFavorite = true), row.toDomain())
    }
}
