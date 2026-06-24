package com.lilbro.picobotella.data.network

import com.lilbro.picobotella.data.model.PokedexResponse
import retrofit2.http.GET

interface PokedexApi {
    @GET("Biuni/PokemonGO-Pokedex/master/pokedex.json")
    suspend fun getPokedex(): PokedexResponse
}

object RetrofitClient {
    val api: PokedexApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(PokedexApi::class.java)
    }
}