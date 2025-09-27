package com.example.camare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.camare.model.FotoEntity
import com.example.camare.viewmodel.FotoViewModel

class RegistrosActivity : ComponentActivity() {

    private val fotoViewModel: FotoViewModel by viewModels()
    private lateinit var adapter: FotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registros)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRegistros)
        adapter = FotoAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observamos los datos desde el ViewModel
        fotoViewModel.todasLasFotos.observe(this) { fotos ->
            adapter.submitList(fotos)
        }
    }
}
