package ru.marwinka.androidarchsample.feature.characters.data.mapper

import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterEntity
import ru.marwinka.androidarchsample.feature.characters.data.local.CharacterWithFavorite
import ru.marwinka.androidarchsample.feature.characters.data.remote.CharacterDto
import ru.marwinka.androidarchsample.feature.characters.domain.model.Character

internal fun CharacterDto.toEntity(): CharacterEntity =
    CharacterEntity(
        id = id,
        name = name,
        species = species,
        imageUrl = image,
    )

internal fun CharacterWithFavorite.toDomain(): Character =
    Character(
        id = character.id,
        name = character.name,
        species = character.species,
        imageUrl = character.imageUrl,
        isFavorite = isFavorite,
    )
