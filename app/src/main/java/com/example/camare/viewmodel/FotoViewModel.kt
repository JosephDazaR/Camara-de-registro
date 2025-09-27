package com.example.camare.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.camare.model.AppDatabase
import com.example.camare.model.FotoEntity
import com.example.camare.repository.FotoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FotoRepository
    val todasLasFotos: LiveData<List<FotoEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).fotoDao()
        repository = FotoRepository(dao)
        todasLasFotos = repository.obtenerFotos()
    }

    fun insertarFoto(foto: FotoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarFoto(foto)
        }
    }
}
