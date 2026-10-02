package ru.marwinka.androidarchsample.domain.model

data class Character(
    val id: Int,
    val name: String,
    val species: String,
    val imageUrl: String,
    val isFavorite: Boolean,
)
