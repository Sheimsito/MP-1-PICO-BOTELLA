package com.lilbro.picobotella.ui.retos

import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.data.repository.RetoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RetosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var picoRepo: PicoBotellaRepository
    private lateinit var retoRepo: RetoRepository
    private lateinit var viewModel: RetosViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        picoRepo = mockk()
        retoRepo = mockk()

        // El StateFlow de retos parte de una lista con un reto de prueba
        val retoInicial = Reto(id = 1, descripcion = "Descripción original")
        coEvery { picoRepo.getAllRetos() } returns flowOf(listOf(retoInicial))

        viewModel = RetosViewModel(picoRepo, retoRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ✅ Criterio 1: editarReto() invoca el método update del Repository
    //    con el id y la descripción correctos
    @Test
    fun `editarReto invoca retoRepository con id y descripcion correctos`() = runTest {
        coEvery { retoRepo.editarReto(any(), any()) } returns Unit

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        coVerify(exactly = 1) { retoRepo.editarReto("1", "Nueva descripción") }
    }

    // ✅ Criterio 2: tras respuesta exitosa, editError permanece null
    @Test
    fun `tras exito editError es null`() = runTest {
        coEvery { retoRepo.editarReto(any(), any()) } returns Unit

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        assertNull(viewModel.editError.value)
    }

    // ✅ Criterio 3: cuando el Repository lanza excepción, editError expone el mensaje
    @Test
    fun `cuando repoRepository falla editError expone el mensaje`() = runTest {
        val mensajeError = "Error de red"
        coEvery { retoRepo.editarReto(any(), any()) } throws Exception(mensajeError)

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        assertEquals(mensajeError, viewModel.editError.value)
    }
}