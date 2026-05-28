package com.lilbro.picobotella.ui.retos

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.lilbro.picobotella.R
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.model.Reto
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.fragment.app.DialogFragment

/**
 * Activity that displays and manages the list of challenges (retos).
 *
 * Handles creating, editing, and deleting challenges using Room via coroutines.
 * Satisfies US 6.0 acceptance criteria:
 * - Scrollable list via RecyclerView
 * - Edit and delete icons per item
 * - New challenges inserted at the top (ordered by createdAt DESC)
 * - Floating action button to add a new challenge
 * - Navigation stubs for HU 7, 8, 9 dialogs
 */
class RetosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var adapter: RetosAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.retosRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getInstance(this)

        setupRecyclerView()
        setupFab()
        observeRetos()
    }

    /**
     * Initializes the RecyclerView with its adapter and layout manager.
     */
    private fun setupRecyclerView() {
        adapter = RetosAdapter(
            onEditClick = { reto -> showEditDialog(reto) },
            onDeleteClick = { reto -> showDeleteConfirmation(reto) }
        )
        recyclerView = findViewById(R.id.recyclerViewRetos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Sets up the FloatingActionButton to open the add-challenge dialog (HU 7).
     */
    private fun setupFab() {
        fabAgregar = findViewById(R.id.fabAgregarReto)
        fabAgregar.setOnClickListener {
            showAddDialog()
        }
    }

    /**
     * Observes the challenges flow from Room and submits updates to the adapter.
     */
    private fun observeRetos() {
        lifecycleScope.launch {
            database.retoDao().getAllRetos().collectLatest { retos ->
                adapter.submitList(retos)
            }
        }
    }

    /**
     * Opens [AgregarRetoDialog] to create a new challenge (US 7.0).
     *
     * The dialog handles its own input validation and save-button state.
     * Persists the new [Reto] to Room on confirmation.
     */
    private fun showAddDialog() {
        AgregarRetoDialog { descripcion ->
            lifecycleScope.launch {
                database.retoDao().insert(Reto(descripcion = descripcion))
            }
        }.show(supportFragmentManager, AgregarRetoDialog.TAG)
    }

    /**
     * Displays a dialog to edit an existing challenge (stub for HU 8).
     *
     * @param reto The challenge to edit.
     */
    private fun showEditDialog(reto: Reto) {
        val input = TextInputEditText(this)
        input.setText(reto.descripcion)

        AlertDialog.Builder(this)
            .setTitle(R.string.title_editar_reto)
            .setView(input)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val texto = input.text?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        database.retoDao().update(reto.copy(descripcion = texto))
                    }
                } else {
                    Toast.makeText(this, R.string.error_descripcion_vacia, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    /**
     * Displays a confirmation dialog before deleting a challenge (stub for HU 9).
     *
     * @param reto The challenge to delete.
     */
    private fun showDeleteConfirmation(reto: Reto) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_eliminar_reto)
            .setMessage(R.string.msg_confirmar_eliminar)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                lifecycleScope.launch {
                    database.retoDao().delete(reto)
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}