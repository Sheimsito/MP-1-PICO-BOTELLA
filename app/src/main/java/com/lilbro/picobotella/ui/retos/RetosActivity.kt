package com.lilbro.picobotella.ui.retos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.lilbro.picobotella.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity that displays and manages the list of challenges (retos).
 *
 * Wired to [RetosViewModel] via Hilt. Observes [RetosUiState] to update
 * the RecyclerView and show error messages.
 *
 * Satisfies HU 7.0:
 * - Scrollable list via RecyclerView.
 * - New challenges are prepended at position 0 without a full reload.
 * - FAB opens [AgregarRetoDialog]; on confirm calls [RetosViewModel.agregarReto].
 * - Firestore errors are surfaced as Toasts.
 */
@AndroidEntryPoint
class RetosActivity : AppCompatActivity() {

    private val viewModel: RetosViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var adapter: RetosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.retosRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFab()
        observeState()
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = RetosAdapter(
            onEditClick  = { reto -> showEditDialog(reto) },
            onDeleteClick = { reto -> showDeleteConfirmation(reto) }
        )
        recyclerView = findViewById(R.id.recyclerViewRetos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAgregar = findViewById(R.id.fabAgregarReto)
        fabAgregar.setOnClickListener { showAddDialog() }
    }

    // ── State observation ─────────────────────────────────────────────────────

    /**
     * Collects [RetosUiState] safely, respecting the Activity lifecycle
     * (stops collection in STOPPED, resumes in STARTED).
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.retos)

                    state.error?.let { errorMsg ->
                        Toast.makeText(this@RetosActivity, errorMsg, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private fun showAddDialog() {
        AgregarRetoDialog { descripcion ->
            viewModel.agregarReto(descripcion)
        }.show(supportFragmentManager, AgregarRetoDialog.TAG)
    }

    private fun showEditDialog(reto: com.lilbro.picobotella.data.model.Reto) {
        val input = TextInputEditText(this)
        input.setText(reto.descripcion)

        AlertDialog.Builder(this)
            .setTitle(R.string.title_editar_reto)
            .setView(input)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val texto = input.text?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    // TODO: Implement edit in ViewModel / Repository (HU 8)
                } else {
                    Toast.makeText(this, R.string.error_descripcion_vacia, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun showDeleteConfirmation(reto: com.lilbro.picobotella.data.model.Reto) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_eliminar_reto)
            .setMessage(R.string.msg_confirmar_eliminar)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                // TODO: Implement delete in ViewModel / Repository (HU 9)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}
