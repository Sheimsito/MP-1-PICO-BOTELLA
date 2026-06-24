package com.lilbro.picobotella.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.RetoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
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
        }
    }

    /**
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
