package com.lilbro.picobotella.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lilbro.picobotella.data.dao.RetoDao
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio encargado de coordinar las operaciones de retos entre
 * la base de datos local (Room) y la base de datos remota (Firestore).
 *
 * Esta clase concentra la lógica de edición y eliminación de retos para
 * mantener sincronizados ambos orígenes de datos:
 * - Primero ejecuta la operación remota en Firestore.
 * - Luego refleja el cambio en Room para actualizar inmediatamente la
 *   lista expuesta a la interfaz.
 *
 * @property retoDao DAO de acceso a los retos almacenados localmente en Room.
 * @property firestore Instancia de Firestore usada para operar sobre la colección `retos`.
 */
@Singleton
class RetoRepository @Inject constructor(
    private val retoDao: RetoDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Edita la descripción de un reto existente.
     *
     * El proceso se realiza en dos pasos:
     * 1. Actualiza en Firestore únicamente el campo `descripcion`
     *    del documento correspondiente al [id].
     * 2. Si el reto existe localmente en Room, actualiza su descripción
     *    para que la lista observada por la vista refleje el cambio
     *    sin necesidad de recargar manualmente.
     *
     * El parámetro [id] llega como `String` porque Firestore identifica
     * los documentos con cadenas, pero también se convierte a `Int`
     * para buscar el mismo reto en la base de datos local.
     *
     * @param id Identificador del reto a editar. Debe coincidir con el id
     * del documento en Firestore y con el id local del reto en Room.
     * @param nuevaDescripcion Nuevo texto que reemplazará la descripción actual.
     *
     * @throws NumberFormatException si el [id] no puede convertirse a `Int`
     * para la búsqueda local.
     * @throws Exception si Firestore falla al actualizar el documento.
     */
    suspend fun editarReto(id: String, nuevaDescripcion: String) {
        firestore.collection("retos")
            .document(id)
            .update("descripcion", nuevaDescripcion)
            .await()

        val idInt = id.toInt()
        val retoActual = retoDao.getRetoById(idInt) ?: return
        retoDao.update(retoActual.copy(descripcion = nuevaDescripcion))
    }

    /**
     * Elimina un reto existente.
     *
     * El proceso se realiza en dos pasos:
     * 1. Elimina el documento correspondiente en Firestore usando [delete].
     * 2. Si el reto existe localmente en Room, lo elimina también para que
     *    desaparezca de inmediato de la lista mostrada en la interfaz.
     *
     * El parámetro [id] llega como `String` porque Firestore trabaja con ids
     * de documento en formato cadena, pero se convierte a `Int` para ubicar
     * el mismo registro en la base local.
     *
     * @param id Identificador del reto a eliminar.
     *
     * @throws NumberFormatException si el [id] no puede convertirse a `Int`.
     * @throws Exception si Firestore falla al eliminar el documento.
     */
    suspend fun eliminarReto(id: String) {
        firestore.collection("retos")
            .document(id)
            .delete()
            .await()

        val idInt = id.toInt()
        val retoActual = retoDao.getRetoById(idInt) ?: return
        retoDao.delete(retoActual)
    }
}