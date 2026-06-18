package com.lilbro.picobotella.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.model.Pokemon
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PicoBotellaRepository) : ViewModel() {

    private val _challengeEvent = SingleLiveEvent<Pair<String, String>>()
    val challengeEvent: LiveData<Pair<String, String>> = _challengeEvent

    private val _errorEvent = SingleLiveEvent<Unit>()
    val errorEvent: LiveData<Unit> = _errorEvent

    fun getRandomChallenge() {
        viewModelScope.launch {
            try {
                // Fetch random Pokemon from API
                val pokemonResponse = repository.getRandomPokemon()
                val randomPokemon = pokemonResponse.pokemon.random()

                // Fetch random Challenge from Room
                val allRetos = repository.getAllRetos().first()
                val challengeText = if (allRetos.isNotEmpty()) {
                    allRetos.random().descripcion
                } else {
                    "Todavía no hay retos creados, dirígete a la opción de añadir retos en la parte superior"
                }

                _challengeEvent.value = Pair(
                    randomPokemon.img.replace("http://", "https://"),
                    challengeText
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _errorEvent.value = Unit
            }
        }
    }

    class Factory(private val repository: PicoBotellaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}