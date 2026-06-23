package com.lilbro.picobotella.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PicoBotellaRepository
) : ViewModel() {

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
}
