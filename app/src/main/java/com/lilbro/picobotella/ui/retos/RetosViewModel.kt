package com.lilbro.picobotella.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.model.Reto
<<<<<<< HEAD
import com.lilbro.picobotella.data.repository.RetoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
=======
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.data.repository.RetoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
>>>>>>> origin/development
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
<<<<<<< HEAD
 * ViewModel for the Retos screen.
 *
 * Manages the list of challenges and delegates all persistence operations
 * to [RetoRepository]. State is exposed via [StateFlow] so the UI can
 * observe changes without leaking lifecycle references.
 *
 * Satisfies HU 7.0 acceptance criteria:
 * - Exposes [uiState] with the current list, loading flag, and error.
 * - [agregarReto] invokes the repository and prepends the new reto at
 *   position 0 of the local list on success (no full reload).
 * - On Firestore error the [RetosUiState.error] field is populated.
 */
@HiltViewModel
class RetosViewModel @Inject constructor(
    private val repository: RetoRepository
) : ViewModel() {

    // ── Internal mutable state ────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(RetosUiState())

    /** Read-only view of the UI state exposed to the Activity / Fragment. */
    val uiState: StateFlow<RetosUiState> = _uiState.asStateFlow()

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        cargarRetos()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Fetches the full challenge list from Firestore and updates [uiState].
     *
     * Called automatically on construction. The Activity / Fragment should
     * not call this unless a manual refresh is required.
     */
    fun cargarRetos() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getRetos()
                .onSuccess { retos ->
                    _uiState.update { it.copy(isLoading = false, retos = retos) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Error desconocido al cargar retos"
                        )
                    }
                }
=======
 * ViewModel encargado de gestionar el estado y las operaciones relacionadas
 * con la lista de retos mostrada en la interfaz.
 *
 * Esta clase coordina:
 * - Las operaciones locales tradicionales de Room a través de [PicoBotellaRepository].
 * - Las operaciones remotas de edición y eliminación en Firestore a través de [RetoRepository].
 *
 * También expone flujos de estado para que la vista pueda:
 * - Observar la lista actual de retos.
 * - Detectar errores al editar un reto.
 * - Detectar errores al eliminar un reto.
 *
 * @property repository Repositorio principal usado para operaciones locales
 * como insertar, actualizar y borrar retos en Room.
 * @property retoRepository Repositorio especializado en operaciones remotas
 * sobre retos en Firestore, con sincronización local posterior.
 */
@HiltViewModel
class RetosViewModel @Inject constructor(
    private val repository: PicoBotellaRepository,
    private val retoRepository: RetoRepository
) : ViewModel() {

    /**
     * Flujo de solo lectura que expone la lista actual de retos.
     *
     * Su origen es el [Flow] devuelto por Room desde [PicoBotellaRepository].
     * Se transforma a [StateFlow] usando [stateIn] para que la interfaz
     * siempre tenga acceso al último valor emitido.
     *
     * La lista se actualiza automáticamente cuando Room detecta cambios
     * locales, por ejemplo después de insertar, editar o eliminar un reto.
     */
    val allRetos: StateFlow<List<Reto>> = repository.getAllRetos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Estado interno mutable para reportar errores durante la edición de retos.
     */
    private val _editError = MutableStateFlow<String?>(null)

    /**
     * Flujo de solo lectura que expone el último error ocurrido
     * durante la operación de edición de un reto.
     *
     * Su valor será `null` cuando no exista error.
     */
    val editError: StateFlow<String?> = _editError.asStateFlow()

    /**
     * Estado interno mutable para reportar errores durante la eliminación de retos.
     */
    private val _deleteError = MutableStateFlow<String?>(null)

    /**
     * Flujo de solo lectura que expone el último error ocurrido
     * durante la operación de eliminación de un reto.
     *
     * Su valor será `null` cuando no exista error.
     */
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    /**
     * Inserta un nuevo reto en la base de datos local.
     *
     * La actualización visual ocurre automáticamente porque [allRetos]
     * observa los cambios emitidos por Room.
     *
     * @param reto Reto que se desea insertar.
     */
    fun insert(reto: Reto) = viewModelScope.launch {
        repository.insertReto(reto)
    }

    /**
     * Actualiza un reto existente en la base de datos local.
     *
     * Esta operación usa el repositorio principal orientado a Room.
     *
     * @param reto Reto con los nuevos valores a persistir.
     */
    fun update(reto: Reto) = viewModelScope.launch {
        repository.updateReto(reto)
    }

    /**
     * Elimina un reto de la base de datos local.
     *
     * Esta función corresponde al flujo local tradicional.
     * Para eliminación remota en Firestore debe usarse [eliminarReto].
     *
     * @param reto Reto que se desea eliminar localmente.
     */
    fun delete(reto: Reto) = viewModelScope.launch {
        repository.deleteReto(reto)
    }

    /**
     * Edita la descripción de un reto existente en Firestore
     * y luego refleja el cambio en la base local.
     *
     * Antes de iniciar la operación, limpia cualquier error previo de edición.
     * Si la actualización falla, publica el mensaje de error en [editError].
     *
     * Tras una operación exitosa, la lista [allRetos] se actualiza automáticamente
     * porque Room emite la nueva lista al modificarse el reto local.
     *
     * @param id Identificador del reto a editar.
     * @param nuevaDescripcion Nuevo texto que reemplazará la descripción actual.
     */
    fun editarReto(id: String, nuevaDescripcion: String) = viewModelScope.launch {
        _editError.value = null
        try {
            retoRepository.editarReto(id, nuevaDescripcion)
        } catch (e: Exception) {
            _editError.value = e.message ?: "Error al editar el reto"
>>>>>>> origin/development
        }
    }

    /**
<<<<<<< HEAD
     * Persists a new challenge to Firestore and, on success, prepends it to
     * the local list so the UI reflects the change immediately (HU 7.0, criterion 6).
     *
     * On failure the [RetosUiState.error] is populated with a human-readable
     * message (covers: no connection, write denied, etc.).
     *
     * @param descripcion Non-blank text of the challenge.
     */
    fun agregarReto(descripcion: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.agregarReto(descripcion)
                .onSuccess { nuevoReto ->
                    // Prepend the new reto without reloading the full list.
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            retos = listOf(nuevoReto) + state.retos
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Error al guardar el reto"
                        )
                    }
                }
        }
    }

    /**
     * Clears the current error from the UI state after it has been shown.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Immutable snapshot of the Retos screen state.
 *
 * @property retos Current list of challenges; ordered newest-first.
 * @property isLoading Whether a network operation is in progress.
 * @property error Non-null when the last operation failed; null otherwise.
 */
data class RetosUiState(
    val retos: List<Reto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
=======
     * Elimina un reto en Firestore y luego lo remueve de la base local.
     *
     * Antes de iniciar la operación, limpia cualquier error previo de eliminación.
     * Si la eliminación falla, publica el mensaje de error en [deleteError].
     *
     * Tras una operación exitosa, el reto desaparece de [allRetos]
     * tan pronto Room emite la nueva lista sin ese elemento.
     *
     * @param id Identificador del reto a eliminar.
     */
    fun eliminarReto(id: String) = viewModelScope.launch {
        _deleteError.value = null
        try {
            retoRepository.eliminarReto(id)
        } catch (e: Exception) {
            _deleteError.value = e.message ?: "Error al eliminar el reto"
        }
    }
}
>>>>>>> origin/development
