package com.lilbro.picobotella.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Implementation of RetrofitClient class to handle API calls ( Pokémon API )
object RetrofitClient {
    private const val BASE_URL = "https://raw.githubusercontent.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val pokemonService: PokemonService by lazy {
        retrofit.create(PokemonService::class.java)
    }
}
