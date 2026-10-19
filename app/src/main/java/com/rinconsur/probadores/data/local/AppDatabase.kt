package com.rinconsur.probadores.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rinconsur.probadores.data.local.dao.ProbadorDao
import com.rinconsur.probadores.data.local.dao.TurnoDao
import com.rinconsur.probadores.data.local.entities.ProbadorEntity
import com.rinconsur.probadores.data.local.entities.TurnoEntity

@Database(
    entities = [ProbadorEntity::class, TurnoEntity::class],
    version = 3, // Incrementar versión a 3
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun probadorDao(): ProbadorDao
    abstract fun turnoDao(): TurnoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "probadores_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}