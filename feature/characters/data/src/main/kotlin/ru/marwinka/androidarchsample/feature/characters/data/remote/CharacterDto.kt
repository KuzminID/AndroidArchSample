package ru.marwinka.androidarchsample.feature.characters.data.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class CharacterDto(
    val id: Int,
    val name: String,
    val species: String,
    val image: String,
)

@Serializable
internal data class CharacterListInfoDto(
    val next: String? = null,
)

@Serializable
internal data class CharacterListResponseDto(
    val info: CharacterListInfoDto,
    val results: List<CharacterDto>,
)
