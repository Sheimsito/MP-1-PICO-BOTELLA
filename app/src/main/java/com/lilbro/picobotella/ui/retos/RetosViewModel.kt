package com.lilbro.picobotella.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.data.repository.RetoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetosViewModel @Inject constructor(
    private val repository: PicoBotellaRepository,
    private val retoRepository: RetoRepository       // ← nuevo
) : ViewModel() {

    val allRetos: StateFlow<List<Reto>> = repository.getAllRetos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expone errores de la operación de edición
    private val _editError = MutableStateFlow<String?>(null)
    val editError: StateFlow<String?> = _editError.asStateFlow()

    fun insert(reto: Reto) = viewModelScope.launch {
        repository.insertReto(reto)
    }

    fun update(reto: Reto) = viewModelScope.launch {
        repository.updateReto(reto)
    }

    fun delete(reto: Reto) = viewModelScope.launch {
        repository.deleteReto(reto)
    }

    /**
     * Edita la descripción de un reto existente vía Firestore.
     * El criterio de aceptación exige que la lista local se actualice
     * sin alterar la posición del ítem — esto lo garantiza el Flow de Room
     * ya que Room emite la lista actualizada automáticamente tras el update().
     */
    fun editarReto(id: String, nuevaDescripcion: String) = viewModelScope.launch {
        _editError.value = null
        try {
            retoRepository.editarReto(id, nuevaDescripcion)
        } catch (e: Exception) {
            _editError.value = e.message ?: "Error al editar el reto"
        }
    }
}