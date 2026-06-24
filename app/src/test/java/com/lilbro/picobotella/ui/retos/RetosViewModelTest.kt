package com.lilbro.picobotella.ui.retos

<<<<<<< HEAD
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.RetoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
=======
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
>>>>>>> origin/development
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
<<<<<<< HEAD
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [RetosViewModel].
 *
 * Uses [StandardTestDispatcher] to control coroutine execution deterministically,
 * and Mockito-Kotlin to mock [RetoRepository].
 *
 * Covers all acceptance criteria for HU 7.0:
 * 1. [agregarReto] delegates to the repository with the correct argument.
 * 2. On success, the new reto appears at position 0 of the exposed list.
 * 3. On failure, the error message is exposed and the list is unchanged.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RetosViewModelTest {

    // ── Rules ─────────────────────────────────────────────────────────────────

    /**
     * Ensures LiveData / Architecture Components execute synchronously
     * on the JVM (needed for [InstantTaskExecutorRule]).
     */
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    // ── Test infrastructure ───────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RetoRepository
    private lateinit var viewModel: RetosViewModel
=======
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
>>>>>>> origin/development

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
<<<<<<< HEAD
        repository = mock()
=======
        picoRepo = mockk()
        retoRepo = mockk()

        val retoInicial = Reto(id = 1, descripcion = "Descripción original")
        retosFlow = MutableStateFlow(listOf(retoInicial))

        coEvery { picoRepo.getAllRetos() } returns retosFlow

        viewModel = RetosViewModel(picoRepo, retoRepo)
>>>>>>> origin/development
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

<<<<<<< HEAD
    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Creates the ViewModel after stubbing [RetoRepository.getRetos] so the
     * `init { cargarRetos() }` call does not fail.
     */
    private suspend fun createViewModel(
        initialRetos: List<Reto> = emptyList()
    ): RetosViewModel {
        whenever(repository.getRetos()).thenReturn(Result.success(initialRetos))
        val vm = RetosViewModel(repository)
        advanceUntilIdle()          // let init{} complete
        return vm
    }

    // ── Tests: agregarReto delegates to repository ────────────────────────────

    /**
     * CRITERION 1: Verifies that [RetosViewModel.agregarReto] calls
     * [RetoRepository.agregarReto] with exactly the description provided.
     */
    @Test
    fun `agregarReto invoca al repository con la descripcion correcta`() = runTest {
        val descripcion = "Beber un vaso de agua"
        val retoGuardado = Reto(id = "abc123", descripcion = descripcion)

        whenever(repository.agregarReto(descripcion)).thenReturn(Result.success(retoGuardado))
        viewModel = createViewModel()

        viewModel.agregarReto(descripcion)
        advanceUntilIdle()

        verify(repository).agregarReto(descripcion)
    }

    // ── Tests: success path ───────────────────────────────────────────────────

    /**
     * CRITERION 2: Mocks a successful repository response and asserts that
     * the new reto is placed at index 0 of the exposed list.
     */
    @Test
    fun `agregarReto exitoso pone el nuevo reto en la primera posicion`() = runTest {
        val existingReto = Reto(id = "old", descripcion = "Reto existente")
        val newReto      = Reto(id = "new", descripcion = "Beber un vaso de agua")

        whenever(repository.agregarReto(any())).thenReturn(Result.success(newReto))
        viewModel = createViewModel(initialRetos = listOf(existingReto))

        viewModel.agregarReto(newReto.descripcion)
        advanceUntilIdle()

        val retos = viewModel.uiState.value.retos
        assertTrue(
            "La lista debe tener 2 elementos después de agregar uno",
            retos.size == 2
        )
        assertEquals(
            "El nuevo reto debe estar en la primera posición",
            newReto.id,
            retos[0].id
        )
        assertEquals(
            "El reto existente debe seguir en la segunda posición",
            existingReto.id,
            retos[1].id
        )
        assertNull(
            "No debe haber error tras un guardado exitoso",
            viewModel.uiState.value.error
        )
    }

    /**
     * CRITERION 2 (loading): Verifies that [isLoading] is false after
     * a successful save.
     */
    @Test
    fun `agregarReto exitoso limpia el estado de carga`() = runTest {
        val newReto = Reto(id = "n1", descripcion = "Saltar 10 veces")
        whenever(repository.agregarReto(any())).thenReturn(Result.success(newReto))
        viewModel = createViewModel()

        viewModel.agregarReto(newReto.descripcion)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    // ── Tests: error path ─────────────────────────────────────────────────────

    /**
     * CRITERION 3: Mocks a Firestore error and verifies that the ViewModel
     * exposes a non-null [RetosUiState.error] and that the list is unchanged.
     */
    @Test
    fun `agregarReto con error expone el mensaje de error`() = runTest {
        val existingReto  = Reto(id = "old", descripcion = "Reto existente")
        val firestoreError = RuntimeException("UNAVAILABLE: no internet connection")

        whenever(repository.agregarReto(any())).thenReturn(Result.failure(firestoreError))
        viewModel = createViewModel(initialRetos = listOf(existingReto))

        viewModel.agregarReto("Reto nuevo")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(
            "El error no debe ser null cuando Firestore falla",
            state.error
        )
        assertTrue(
            "El mensaje de error debe reflejar la causa",
            state.error!!.contains("UNAVAILABLE", ignoreCase = true) ||
            state.error.isNotBlank()
        )
        assertEquals(
            "La lista no debe modificarse tras un error",
            listOf(existingReto),
            state.retos
        )
    }

    /**
     * CRITERION 3 (loading): Verifies that [isLoading] is false after
     * a failed save.
     */
    @Test
    fun `agregarReto con error limpia el estado de carga`() = runTest {
        whenever(repository.agregarReto(any()))
            .thenReturn(Result.failure(RuntimeException("write error")))
        viewModel = createViewModel()

        viewModel.agregarReto("Reto fallido")
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    // ── Tests: clearError ─────────────────────────────────────────────────────

    /**
     * Verifies that [RetosViewModel.clearError] resets the error to null.
     */
    @Test
    fun `clearError limpia el campo error del estado`() = runTest {
        whenever(repository.agregarReto(any()))
            .thenReturn(Result.failure(RuntimeException("err")))
        viewModel = createViewModel()

        viewModel.agregarReto("Reto")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(
            "clearError() debe poner error = null",
            viewModel.uiState.value.error
        )
=======
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
>>>>>>> origin/development
    }
}
