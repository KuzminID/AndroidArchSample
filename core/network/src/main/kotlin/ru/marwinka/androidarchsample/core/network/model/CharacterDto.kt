package ru.marwinka.androidarchsample.core.network.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi
@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val species: String,
    val image: String,
)

@InternalSerializationApi
@Serializable
data class CharacterListInfoDto(
    val next: String? = null,
)

@InternalSerializationApi
@Serializable
data class CharacterListResponseDto(
    val info: CharacterListInfoDto,
    val results: List<CharacterDto>,
)
