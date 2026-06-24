package com.lilbro.picobotella.data.model

import com.google.gson.annotations.SerializedName

data class PokedexResponse(
    @SerializedName("pokemon") val pokemon: List<PokemonBasic>
)

data class PokemonBasic(
    @SerializedName("num") val num: String,
    @SerializedName("name") val name: String,
    @SerializedName("img") val img: String
)