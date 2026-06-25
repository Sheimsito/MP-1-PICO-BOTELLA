package com.lilbro.picobotella.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "FirestoreSyncWorker"
        const val KEY_RETO_ID = "reto_id"
        const val KEY_RETO_DESC = "reto_desc"
        const val KEY_ACTION = "action"
        const val ACTION_SAVE = "save"
        const val ACTION_DELETE = "delete"
    }

    override suspend fun doWork(): Result {
        val retoId = inputData.getInt(KEY_RETO_ID, -1)
        val retoDesc = inputData.getString(KEY_RETO_DESC)
        val action = inputData.getString(KEY_ACTION)

        Log.d(TAG, "Iniciando tarea: ID=$retoId, Accion=$action")

        if (retoId == -1 || action == null) {
            Log.e(TAG, "Datos de entrada invalidos")
            return Result.failure()
        }

        return try {
            val docRef = firestore.collection("retos").document(retoId.toString())

            when (action) {
                ACTION_SAVE -> {
                    val retoMap = hashMapOf(
                        "id" to retoId,
                        "descripcion" to retoDesc
                    )
                    docRef.set(retoMap).await()
                    Log.d(TAG, "Reto guardado exitosamente en Firestore")
                }
                ACTION_DELETE -> {
                    docRef.delete().await()
                    Log.d(TAG, "Reto eliminado exitosamente de Firestore")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando con Firestore: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
