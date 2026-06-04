package com.lilbro.picobotella.ui.retos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.lilbro.picobotella.R
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.model.Reto
import com.lilbro.picobotella.data.repository.API
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.lilbro.picobotella.ui.retos.RetosAdapter


class RetosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RetosAdapter

    private val viewModel: RetosViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = PicoBotellaRepository(database.retoDao(), API.pokemonService)
        RetosViewModel.Factory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_retos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupRecyclerView(view)
        setupFab(view)
        observeRetos()
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarRetos)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView(view: View) {
        adapter = RetosAdapter(
            onEditClick = { reto -> showEditDialog(reto) },
            onDeleteClick = { reto -> showDeleteConfirmation(reto) }
        )
        recyclerView = view.findViewById(R.id.recyclerViewRetos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.fabAgregarReto).setOnClickListener {
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
        }.show(parentFragmentManager, AgregarRetoDialog.TAG)
    }

    private fun showEditDialog(reto: Reto) {
        val input = TextInputEditText(requireContext())
        input.setText(reto.descripcion)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_editar_reto)
            .setView(input)
            .setPositiveButton(R.string.btn_guardar) { _, _ ->
                val texto = input.text?.toString()?.trim()
                if (!texto.isNullOrEmpty()) {
                    viewModel.update(reto.copy(descripcion = texto))
                } else {
                    Toast.makeText(requireContext(), R.string.error_descripcion_vacia, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun showDeleteConfirmation(reto: Reto) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_eliminar_reto)
            .setMessage(R.string.msg_confirmar_eliminar)
            .setPositiveButton(R.string.btn_eliminar) { _, _ ->
                viewModel.delete(reto)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}