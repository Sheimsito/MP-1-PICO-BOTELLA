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
import com.lilbro.picobotella.data.repository.PicoBotellaRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RetoAleatorioDialog : DialogFragment() {

    @Inject
    lateinit var repository: PicoBotellaRepository

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
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val tvReto = view.findViewById<TextView>(R.id.tvRetoDescripcion)
        val ivPokemon = view.findViewById<ImageView>(R.id.ivPokemon)
        val btnCerrar = view.findViewById<MaterialButton>(R.id.btnCerrarReto)

        isCancelable = false
        btnCerrar.setOnClickListener { dismiss() }

        cargarRetoYPokemon(tvReto, ivPokemon)
    }

    private fun cargarRetoYPokemon(tvReto: TextView, ivPokemon: ImageView) {
        lifecycleScope.launch {
            try {
                // 1. Obtener reto aleatorio desde el repositorio
                val retos = repository.getAllRetos().first()
                val reto = if (retos.isNotEmpty()) retos.random() else null
                tvReto.text = reto?.descripcion ?: "¡Sin retos disponibles! Agrega uno primero."

                // 2. Obtener Pokémon aleatorio desde el repositorio (API unificada)
                val pokedex = repository.getRandomPokemon()
                val randomPokemon = pokedex.pokemon.random()
                
                Glide.with(this@RetoAleatorioDialog)
                    .load(randomPokemon.img.replace("http://", "https://"))
                    .into(ivPokemon)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
