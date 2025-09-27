package com.example.camare.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// Aquí le decimos a Room qué entidades (tablas) tiene esta BD
@Database(entities = [FotoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Aquí exponemos el DAO (interfaz con funciones para trabajar con la tabla)
    abstract fun fotoDao(): FotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Método para obtener la BD (singleton → solo una instancia para toda la app)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fotos_db"   // nombre del archivo de la BD
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
