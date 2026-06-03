package com.lilbro.picobotella.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RetosViewModel(private val repository: PicoBotellaRepository) : ViewModel() {

    val allRetos: StateFlow<List<Reto>> = repository.getAllRetos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(reto: Reto) = viewModelScope.launch {
        repository.insertReto(reto)
    }

    fun update(reto: Reto) = viewModelScope.launch {
        repository.updateReto(reto)
    }

    fun delete(reto: Reto) = viewModelScope.launch {
        repository.deleteReto(reto)
    }

    class Factory(private val repository: PicoBotellaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RetosViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RetosViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}