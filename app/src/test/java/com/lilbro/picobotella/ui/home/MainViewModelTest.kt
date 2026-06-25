package com.lilbro.picobotella.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.lilbro.picobotella.data.model.Pokemon
import com.lilbro.picobotella.data.model.PokemonResponse
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: PicoBotellaRepository

    @Mock
    private lateinit var challengeObserver: Observer<Pair<String, String>>

    @Mock
    private lateinit var errorObserver: Observer<Unit>

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(repository)
        viewModel.challengeEvent.observeForever(challengeObserver)
        viewModel.errorEvent.observeForever(errorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getRandomChallenge success updates challengeEvent`() = runTest {
        val mockPokemon = Pokemon(
            id = 1, num = "001", name = "Bulbasaur", img = "http://img.url",
            type = listOf("Grass"), height = "0.71 m", weight = "6.9 kg",
            candy = null, candy_count = null, egg = null, spawn_chance = null,
            avg_spawns = null, spawn_time = null, multipliers = null,
            weaknesses = null, next_evolution = null, prev_evolution = null
        )
        val mockResponse = PokemonResponse(pokemon = listOf(mockPokemon))
        val mockRetos = listOf(Reto(id = 1, descripcion = "Hacer 10 flexiones"))

        `when`(repository.getRandomPokemon()).thenReturn(mockResponse)
        `when`(repository.getAllRetos()).thenReturn(flowOf(mockRetos))

        viewModel.getRandomChallenge()
        advanceUntilIdle()

        verify(challengeObserver).onChanged(Pair("https://img.url", "Hacer 10 flexiones"))
    }

    @Test
    fun `getRandomChallenge empty retos uses default message`() = runTest {
        val mockPokemon = Pokemon(
            id = 1, num = "001", name = "Bulbasaur", img = "http://img.url",
            type = listOf("Grass"), height = "0.71 m", weight = "6.9 kg",
            candy = null, candy_count = null, egg = null, spawn_chance = null,
            avg_spawns = null, spawn_time = null, multipliers = null,
            weaknesses = null, next_evolution = null, prev_evolution = null
        )
        val mockResponse = PokemonResponse(pokemon = listOf(mockPokemon))
        val mockRetos = emptyList<Reto>()

        `when`(repository.getRandomPokemon()).thenReturn(mockResponse)
        `when`(repository.getAllRetos()).thenReturn(flowOf(mockRetos))

        viewModel.getRandomChallenge()
        advanceUntilIdle()

        verify(challengeObserver).onChanged(
            Pair("https://img.url", "Todavía no hay retos creados, dirígete a la opción de añadir retos en la parte superior")
        )
    }

    @Test
    fun `getRandomChallenge error updates errorEvent`() = runTest {
        `when`(repository.getRandomPokemon()).thenThrow(RuntimeException("API Error"))

        viewModel.getRandomChallenge()
        advanceUntilIdle()

        verify(errorObserver).onChanged(Unit)
    }
}
