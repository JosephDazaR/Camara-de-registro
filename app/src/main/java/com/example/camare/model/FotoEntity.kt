package com.example.camare.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fotos")
data class FotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // id autogenerado por la BD
    val numero: Int,           // número de foto (contador interno)
    val uri: String,           // ruta del archivo de la foto
    val latitud: Double,       // latitud donde se tomó
    val longitud: Double,      // longitud donde se tomó
    val fechaHora: String      // fecha y hora de captura
)
