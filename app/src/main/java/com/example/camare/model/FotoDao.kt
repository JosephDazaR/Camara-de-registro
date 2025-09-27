package com.example.camare.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FotoDao {   // 👈 aquí debe ser interface, no class

    @Insert
    suspend fun insertarFoto(foto: FotoEntity)

    @Query("SELECT * FROM fotos ORDER BY id DESC")
    fun obtenerFotos(): LiveData<List<FotoEntity>>
}
