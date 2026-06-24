package com.lilbro.picobotella.ui.retos

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
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class RetosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: PicoBotellaRepository

    private lateinit var viewModel: RetosViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock the initial flow for allRetos
        `when`(repository.getAllRetos()).thenReturn(flowOf(emptyList()))

        viewModel = RetosViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert reto calls repository insertReto`() = runTest {
        val reto = Reto(id = 1, descripcion = "Nuevo Reto")
        viewModel.insert(reto)
        advanceUntilIdle()
        verify(repository).insertReto(reto)
    }

    @Test
    fun `update reto calls repository updateReto`() = runTest {
        val reto = Reto(id = 1, descripcion = "Reto Actualizado")
        viewModel.update(reto)
        advanceUntilIdle()
        verify(repository).updateReto(reto)
    }

    @Test
    fun `delete reto calls repository deleteReto`() = runTest {
        val reto = Reto(id = 1, descripcion = "Reto a borrar")
        viewModel.delete(reto)
        advanceUntilIdle()
        verify(repository).deleteReto(reto)
    }
}
