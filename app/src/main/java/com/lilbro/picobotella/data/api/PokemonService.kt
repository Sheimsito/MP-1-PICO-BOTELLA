package com.lilbro.picobotella.data.api

import com.lilbro.picobotella.data.model.PokemonResponse
import retrofit2.http.GET

interface PokemonService {
    @GET("Biuni/PokemonGO-Pokedex/master/pokedex.json")
    suspend fun getPokemon(): PokemonResponse
}