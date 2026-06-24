package com.lilbro.picobotella.ui.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.lilbro.picobotella.R
import com.lilbro.picobotella.data.db.AppDatabase
import com.lilbro.picobotella.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RetoAleatorioDialog : DialogFragment() {

    companion object {
        const val TAG = "RetoAleatorioDialog"
        fun newInstance() = RetoAleatorioDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_reto_aleatorio_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fondo transparente para que se vea el degradado personalizado
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Ocupa toda la pantalla para poder posicionar el círculo fuera del card
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val tvReto = view.findViewById<TextView>(R.id.tvRetoDescripcion)
        val ivPokemon = view.findViewById<ImageView>(R.id.ivPokemon)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarReto)

        // Criterio 6: solo cierra con el botón, NO con click fuera
        isCancelable = false

        btnCerrar.setOnClickListener {
            dismiss()
        }

        cargarRetoYPokemon(tvReto, ivPokemon)
    }

    private fun cargarRetoYPokemon(tvReto: TextView, ivPokemon: ImageView) {
        lifecycleScope.launch {
            // 1. Obtener reto aleatorio desde Room (BD local)
            val reto = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .retoDao()
                    .getRetoAleatorio()
            }
            tvReto.text = reto?.descripcion ?: "¡Sin retos disponibles! Agrega uno primero."

            // 2. Obtener Pokémon aleatorio desde la API
            try {
                val pokedex = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getPokedex()
                }
                val randomPokemon = pokedex.pokemon.random()
                Glide.with(this@RetoAleatorioDialog)
                    .load(randomPokemon.img)
                    .into(ivPokemon)
            } catch (e: Exception) {
                // Si falla la red, el círculo queda vacío sin crashear
            }
        }
    }
}