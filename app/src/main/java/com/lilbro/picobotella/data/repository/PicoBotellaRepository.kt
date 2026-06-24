package com.lilbro.picobotella.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lilbro.picobotella.data.api.PokemonService
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.model.PokemonResponse
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.worker.FirestoreSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that abstracts access to local (Room) and remote (API) data sources.
 */
@Singleton
class PicoBotellaRepository @Inject constructor(
    private val retoDao: RetoDao,
    private val pokemonService: PokemonService,
    @ApplicationContext private val context: Context
) {
    // Database operations
    fun getAllRetos(): Flow<List<Reto>> = retoDao.getAllRetos()

    suspend fun insertReto(reto: Reto) {
        retoDao.insert(reto)
        scheduleSync()
    }

    suspend fun updateReto(reto: Reto) {
        retoDao.update(reto)
        scheduleSync()
    }

    suspend fun deleteReto(reto: Reto) {
        retoDao.delete(reto)
        scheduleSync()
    }

    // API operations
    suspend fun getRandomPokemon(): PokemonResponse = pokemonService.getPokemon()

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
