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
import kotlin.random.Random

@HiltViewModel
class RetosViewModel @Inject constructor(
    private val repository: PicoBotellaRepository,
    private val retoRepository: RetoRepository,
    private val random: Random = Random.Default
) : ViewModel() {

    val allRetos: StateFlow<List<Reto>> = repository.getAllRetos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _editError = MutableStateFlow<String?>(null)
    val editError: StateFlow<String?> = _editError.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    private val _retoAleatorio = MutableStateFlow<Reto?>(null)
    val retoAleatorio: StateFlow<Reto?> = _retoAleatorio.asStateFlow()

    private val _retoAleatorioMensaje = MutableStateFlow<String?>(null)
    val retoAleatorioMensaje: StateFlow<String?> = _retoAleatorioMensaje.asStateFlow()

    fun insert(reto: Reto) = viewModelScope.launch {
        repository.insertReto(reto)
    }

    fun update(reto: Reto) = viewModelScope.launch {
        repository.updateReto(reto)
    }

    fun delete(reto: Reto) = viewModelScope.launch {
        repository.deleteReto(reto)
    }

    fun editarReto(id: String, nuevaDescripcion: String) = viewModelScope.launch {
        _editError.value = null
        try {
            retoRepository.editarReto(id, nuevaDescripcion)
        } catch (e: Exception) {
            _editError.value = e.message ?: "Error al editar el reto"
        }
    }

    fun eliminarReto(id: String) = viewModelScope.launch {
        _deleteError.value = null
        try {
            retoRepository.eliminarReto(id)
        } catch (e: Exception) {
            _deleteError.value = e.message ?: "Error al eliminar el reto"
        }
    }

    fun obtenerRetoAleatorio() = viewModelScope.launch {
        try {
            val retos = retoRepository.obtenerRetosDesdeFirestore()

            if (retos.isEmpty()) {
                _retoAleatorio.value = null
                _retoAleatorioMensaje.value = "No hay retos disponibles"
                return@launch
            }

            val indice = random.nextInt(retos.size)
            _retoAleatorio.value = retos[indice]
            _retoAleatorioMensaje.value = null
        } catch (e: Exception) {
            _retoAleatorio.value = null
            _retoAleatorioMensaje.value = e.message ?: "Error al obtener un reto aleatorio"
        }
    }
}