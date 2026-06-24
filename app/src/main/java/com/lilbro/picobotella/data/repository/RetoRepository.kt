package com.lilbro.picobotella.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lilbro.picobotella.data.dao.RetoDao
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetoRepository @Inject constructor(
    private val retoDao: RetoDao,
    private val firestore: FirebaseFirestore
) {
    /**
     * Actualiza la descripción de un reto existente:
     * 1. Hace update() en Firestore sobre el campo "descripcion"
     * 2. Actualiza el registro local en Room
     *
     * @param id ID del reto a editar (como String para Firestore, Int para Room)
     * @param nuevaDescripcion Nuevo texto del reto
     */
    suspend fun editarReto(id: String, nuevaDescripcion: String) {
        // 1. Actualiza solo el campo en Firestore (no reemplaza el documento)
        firestore.collection("retos")
            .document(id)
            .update("descripcion", nuevaDescripcion)
            .await()

        // 2. Actualiza Room para mantener consistencia local
        val idInt = id.toInt()
        val retoActual = retoDao.getRetoById(idInt) ?: return
        retoDao.update(retoActual.copy(descripcion = nuevaDescripcion))
    }
}