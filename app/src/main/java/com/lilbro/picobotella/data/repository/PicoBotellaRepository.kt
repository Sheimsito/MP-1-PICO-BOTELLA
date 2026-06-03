package com.lilbro.picobotella.data.repository

import com.lilbro.picobotella.data.api.PokemonService
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.model.PokemonResponse
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts access to local (Room) and remote (API) data sources.
 */
class PicoBotellaRepository(
    private val retoDao: RetoDao,
    private val pokemonService: PokemonService
) {
    // Database operations
    fun getAllRetos(): Flow<List<Reto>> = retoDao.getAllRetos()

    suspend fun insertReto(reto: Reto) = retoDao.insert(reto)

    suspend fun updateReto(reto: Reto) = retoDao.update(reto)

    suspend fun deleteReto(reto: Reto) = retoDao.delete(reto)

    // API operations
    suspend fun getRandomPokemon(): PokemonResponse = pokemonService.getPokemon()
}