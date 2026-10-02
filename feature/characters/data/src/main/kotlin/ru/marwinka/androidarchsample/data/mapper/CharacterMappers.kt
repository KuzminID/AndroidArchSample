package ru.marwinka.androidarchsample.data.mapper

import ru.marwinka.androidarchsample.core.network.model.CharacterDto
import ru.marwinka.androidarchsample.data.local.CharacterEntity
import ru.marwinka.androidarchsample.domain.model.Character

fun CharacterDto.toEntity(): CharacterEntity =
    CharacterEntity(
        id = id,
        name = name,
        species = species,
        imageUrl = image,
    )

fun CharacterEntity.toDomain(isFavorite: Boolean): Character =
    Character(
        id = id,
        name = name,
        species = species,
        imageUrl = imageUrl,
        isFavorite = isFavorite,
    )
