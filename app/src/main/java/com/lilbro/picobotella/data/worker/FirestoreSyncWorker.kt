package com.lilbro.picobotella.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.lilbro.picobotella.data.dao.RetoDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

// Implementation of FirestoreSyncWorker class to handle Firestore synchronization.
// Uses HiltWorker to inject dependencies.
@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val retoDao: RetoDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val retos = retoDao.getAllRetos().first()
            
            for (reto in retos) {
                val retoMap = hashMapOf(
                    "id" to reto.id,
                    "descripcion" to reto.descripcion
                )

                firestore.collection("retos")
                    .document(reto.id.toString())
                    .set(retoMap)
                    .await()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
