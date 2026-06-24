package com.lilbro.picobotella.ui.retos

import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import com.lilbro.picobotella.data.repository.RetoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class RetosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var picoRepo: PicoBotellaRepository
    private lateinit var retoRepo: RetoRepository
    private lateinit var viewModel: RetosViewModel
    private lateinit var retosFlow: MutableStateFlow<List<Reto>>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        picoRepo = mockk()
        retoRepo = mockk()

        val retoInicial = Reto(id = 1, descripcion = "Descripción original")
        retosFlow = MutableStateFlow(listOf(retoInicial))

        coEvery { picoRepo.getAllRetos() } returns retosFlow

        viewModel = RetosViewModel(picoRepo, retoRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifica que el ViewModel invoque al repository de edición
     * con el id y la nueva descripción correctos.
     */
    @Test
    fun `editarReto invoca retoRepository con id y descripcion correctos`() = runTest {
        coEvery { retoRepo.editarReto(any(), any()) } returns Unit

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        coVerify(exactly = 1) { retoRepo.editarReto("1", "Nueva descripción") }
    }

    /**
     * Verifica que, tras una edición exitosa, la lista expuesta
     * por el ViewModel refleje la nueva descripción.
     */
    @Test
    fun `tras respuesta exitosa la lista en el ViewModel se actualiza con la nueva descripcion`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.allRetos.collect {}
        }

        coEvery { retoRepo.editarReto(any(), any()) } answers {
            retosFlow.value = listOf(
                Reto(id = 1, descripcion = "Nueva descripción")
            )
            Unit
        }

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        assertEquals(1, viewModel.allRetos.value.size)
        assertEquals("Nueva descripción", viewModel.allRetos.value[0].descripcion)
        assertEquals(1, viewModel.allRetos.value[0].id)
        assertNull(viewModel.editError.value)

        collectJob.cancel()
    }

    /**
     * Verifica que, si el repository falla al editar,
     * el ViewModel exponga el mensaje de error correspondiente.
     */
    @Test
    fun `cuando retoRepository falla al editar editError expone el mensaje`() = runTest {
        val mensajeError = "Error de red"
        coEvery { retoRepo.editarReto(any(), any()) } throws Exception(mensajeError)

        viewModel.editarReto("1", "Nueva descripción")
        advanceUntilIdle()

        assertEquals(mensajeError, viewModel.editError.value)
    }

    /**
     * Verifica que el ViewModel invoque al repository de eliminación
     * con el id correcto.
     */
    @Test
    fun `eliminarReto invoca retoRepository con id correcto`() = runTest {
        coEvery { retoRepo.eliminarReto(any()) } returns Unit

        viewModel.eliminarReto("1")
        advanceUntilIdle()

        coVerify(exactly = 1) { retoRepo.eliminarReto("1") }
    }

    /**
     * Verifica que, tras una eliminación exitosa,
     * el reto desaparezca de la lista expuesta por el ViewModel.
     */
    @Test
    fun `tras eliminacion exitosa el reto desaparece de la lista expuesta por el ViewModel`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.allRetos.collect {}
        }

        val reto1 = Reto(id = 1, descripcion = "Reto 1")
        val reto2 = Reto(id = 2, descripcion = "Reto 2")
        retosFlow.value = listOf(reto1, reto2)
        advanceUntilIdle()

        coEvery { retoRepo.eliminarReto("1") } answers {
            retosFlow.value = listOf(reto2)
            Unit
        }

        viewModel.eliminarReto("1")
        advanceUntilIdle()

        assertEquals(1, viewModel.allRetos.value.size)
        assertEquals(2, viewModel.allRetos.value[0].id)
        assertEquals("Reto 2", viewModel.allRetos.value[0].descripcion)
        assertNull(viewModel.deleteError.value)

        collectJob.cancel()
    }

    /**
     * Verifica que, si el repository falla al eliminar,
     * el ViewModel exponga el mensaje de error correspondiente.
     */
    @Test
    fun `cuando eliminarReto falla deleteError expone el mensaje`() = runTest {
        val mensajeError = "Error al eliminar"
        coEvery { retoRepo.eliminarReto(any()) } throws Exception(mensajeError)

        viewModel.eliminarReto("1")
        advanceUntilIdle()

        assertEquals(mensajeError, viewModel.deleteError.value)
    }
}
