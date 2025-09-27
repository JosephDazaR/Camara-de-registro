package com.example.camare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.camare.model.FotoEntity
import com.bumptech.glide.Glide


class FotoAdapter :
    ListAdapter<FotoEntity, FotoAdapter.FotoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val foto = getItem(position)
        holder.bind(foto)
    }

    class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageFoto)
        private val textInfo: TextView = itemView.findViewById(R.id.textFecha)

        fun bind(foto: FotoEntity) {
            // Mostrar la foto usando Glide (carga desde ruta local)
            Glide.with(itemView.context)
                .load(foto.uri)
                .into(imageView)

            textInfo.text = """
                Foto ${foto.numero}
                Fecha: ${foto.fechaHora}
                Lat: ${foto.latitud}, Lon: ${foto.longitud}
            """.trimIndent()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FotoEntity>() {
        override fun areItemsTheSame(oldItem: FotoEntity, newItem: FotoEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FotoEntity, newItem: FotoEntity) =
            oldItem == newItem
    }
}
