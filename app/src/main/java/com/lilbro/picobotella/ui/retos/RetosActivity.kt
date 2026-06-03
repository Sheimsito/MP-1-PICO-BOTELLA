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
import com.lilbro.picobotella.data.api.PokemonService
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.API
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity that displays and manages the list of challenges (retos).
 * Refactored to use MVVM with Repository.
 */
class RetosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var adapter: RetosAdapter

    private val viewModel: RetosViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = PicoBotellaRepository(database.retoDao(), API.pokemonService)
        RetosViewModel.Factory(repository)
    }

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
        observeRetos()
    }

    private fun setupRecyclerView() {
        adapter = RetosAdapter(
            onEditClick = { reto -> showEditDialog(reto) },
            onDeleteClick = { reto -> showDeleteConfirmation(reto) }
        )
        recyclerView = findViewById(R.id.recyclerViewRetos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        fabAgregar = findViewById(R.id.fabAgregarReto)
        fabAgregar.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeRetos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allRetos.collectLatest { retos ->
                    adapter.submitList(retos)
                }
            }
        }
    }

    private fun showAddDialog() {
        AgregarRetoDialog { descripcion ->
            viewModel.insert(Reto(descripcion = descripcion))
        }.show(supportFragmentManager, AgregarRetoDialog.TAG)
    }

    private fun showEditDialog(reto: Reto) {
        val input = TextInputEditText(this)
        input.setText(reto.descripcion)

        AlertDialog.Builder(this)
            .setTitle(R.string.title_editar_reto)
            .setView(input)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val texto = input.text?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    viewModel.update(reto.copy(descripcion = texto))
                } else {
                    Toast.makeText(this, R.string.error_descripcion_vacia, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun showDeleteConfirmation(reto: Reto) {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_eliminar_reto)
            .setMessage(R.string.msg_confirmar_eliminar)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                viewModel.delete(reto)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}