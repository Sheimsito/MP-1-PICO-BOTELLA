package com.lilbro.picobotella.data.repository

import android.content.Context
import androidx.work.*
import com.lilbro.picobotella.data.api.PokemonService
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.model.PokemonResponse
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.worker.FirestoreSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicoBotellaRepository @Inject constructor(
    private val retoDao: RetoDao,
    private val pokemonService: PokemonService,
    @ApplicationContext private val context: Context
) {
    fun getAllRetos(): Flow<List<Reto>> = retoDao.getAllRetos()

    suspend fun insertReto(reto: Reto) {
        val generatedId = retoDao.insert(reto)
        val retoWithId = reto.copy(id = generatedId.toInt())
        scheduleSync(retoWithId, FirestoreSyncWorker.ACTION_SAVE)
    }

    suspend fun updateReto(reto: Reto) {
        retoDao.update(reto)
        scheduleSync(reto, FirestoreSyncWorker.ACTION_SAVE)
    }

    suspend fun deleteReto(reto: Reto) {
        retoDao.delete(reto)
        scheduleSync(reto, FirestoreSyncWorker.ACTION_DELETE)
    }

    suspend fun getRandomPokemon(): PokemonResponse = pokemonService.getPokemon()

    private fun scheduleSync(reto: Reto, action: String) {
        val inputData = workDataOf(
            FirestoreSyncWorker.KEY_RETO_ID to reto.id,
            FirestoreSyncWorker.KEY_RETO_DESC to reto.descripcion,
            FirestoreSyncWorker.KEY_ACTION to action
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_${reto.id}",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
