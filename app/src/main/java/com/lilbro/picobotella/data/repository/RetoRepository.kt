package com.lilbro.picobotella.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that encapsulates all Firestore operations for the "retos" collection.
 *
 * All operations are suspend functions so they can be called safely from a coroutine
 * context without blocking the main thread.
 *
 * Satisfies HU 7.0 acceptance criteria:
 * - Encapsulates the Firestore collection "retos".
 * - Exposes [agregarReto] to persist a new challenge.
 * - Exposes [getRetos] to retrieve the ordered list of challenges.
 */
@Singleton
class RetoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        /** Name of the Firestore collection that stores challenges. */
        const val COLLECTION = "retos"
    }

    /**
     * Saves a new challenge to Firestore.
     *
     * The Firestore document is auto-generated (using [FirebaseFirestore.collection]
     * + [com.google.firebase.firestore.CollectionReference.document] with no ID).
     * The returned [Result] wraps the stored [Reto] with the server-assigned ID
     * on success, or a [Throwable] on failure (e.g. no network, write denied).
     *
     * @param descripcion Non-blank text of the challenge to save.
     * @return [Result.success] with the stored [Reto], or [Result.failure] with the error.
     */
    suspend fun agregarReto(descripcion: String): Result<Reto> = runCatching {
        val docRef = firestore.collection(COLLECTION).document()
        val reto = Reto(
            id = docRef.id,
            descripcion = descripcion,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(reto.toFirestoreMap()).await()
        reto
    }

    /**
     * Retrieves all challenges ordered by [Reto.createdAt] descending (newest first).
     *
     * @return [Result.success] with the list, or [Result.failure] with the error.
     */
    suspend fun getRetos(): Result<List<Reto>> = runCatching {
        val snapshot = firestore
            .collection(COLLECTION)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.documents.mapNotNull { doc ->
            val descripcion = doc.getString("descripcion") ?: return@mapNotNull null
            val createdAt = doc.getLong("createdAt") ?: 0L
            Reto(id = doc.id, descripcion = descripcion, createdAt = createdAt)
        }
    }
}
