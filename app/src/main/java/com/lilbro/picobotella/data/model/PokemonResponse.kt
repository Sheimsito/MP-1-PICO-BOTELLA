package com.lilbro.picobotella.data.model

data class PokemonResponse(
    val pokemon: List<Pokemon>
)

data class Pokemon(
    val id: Int,
    val num: String,
    val name: String,
    val img: String,
    val type: List<String>,
    val height: String,
    val weight: String,
    val candy: String?,
    val candy_count: Int?,
    val egg: String?,
    val spawn_chance: Double?,
    val avg_spawns: Double?,
    val spawn_time: String?,
    val multipliers: List<Double>?,
    val weaknesses: List<String>?,
    val next_evolution: List<Evolution>?,
    val prev_evolution: List<Evolution>?
)

data class Evolution(
    val num: String,
    val name: String
)