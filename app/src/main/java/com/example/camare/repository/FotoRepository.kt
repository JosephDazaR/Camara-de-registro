package com.example.camare.repository

import androidx.lifecycle.LiveData
import com.example.camare.model.FotoDao
import com.example.camare.model.FotoEntity

class FotoRepository(private val fotoDao: FotoDao) {

    suspend fun insertarFoto(foto: FotoEntity) {
        fotoDao.insertarFoto(foto)
    }

    fun obtenerFotos(): LiveData<List<FotoEntity>> {
        return fotoDao.obtenerFotos()
    }
}
