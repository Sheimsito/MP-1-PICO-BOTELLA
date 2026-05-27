package com.lilbro.picobotella.ui.retos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lilbro.picobotella.R
import com.lilbro.picobotella.data.model.Reto

/**
 * RecyclerView adapter for displaying a list of [Reto] items.
 *
 * @property onEditClick Callback invoked when the edit button is tapped, receiving the selected [Reto].
 * @property onDeleteClick Callback invoked when the delete button is tapped, receiving the selected [Reto].
 */
class RetosAdapter(
    private val onEditClick: (Reto) -> Unit,
    private val onDeleteClick: (Reto) -> Unit
) : ListAdapter<Reto, RetosAdapter.RetoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reto, parent, false)
        return RetoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RetoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RetoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionReto)
        private val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarReto)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarReto)

        /**
         * Binds a [Reto] entity to the view holder.
         *
         * @param reto The challenge to display.
         */
        fun bind(reto: Reto) {
            tvDescripcion.text = reto.descripcion
            btnEditar.setOnClickListener { onEditClick(reto) }
            btnEliminar.setOnClickListener { onDeleteClick(reto) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reto>() {
            override fun areItemsTheSame(oldItem: Reto, newItem: Reto) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Reto, newItem: Reto) = oldItem == newItem
        }
    }
}