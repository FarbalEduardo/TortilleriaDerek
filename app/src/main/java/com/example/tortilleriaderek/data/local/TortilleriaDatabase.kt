package com.example.tortilleriaderek.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.SeguridadConfigDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.MermaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.SeguridadConfigEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity

@Database(
    entities = [
        TurnoEntity::class,
        VentaEntity::class,
        ConfiguracionProduccionEntity::class,
        UsuarioEntity::class,
        TandaProduccionEntity::class,
        MermaProduccionEntity::class,
        RepartidorEntity::class,
        RutaRepartidorEntity::class,
        SeguridadConfigEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class TortilleriaDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun turnoDao(): TurnoDao
    abstract fun ventaDao(): VentaDao
    abstract fun rutaRepartidorDao(): RutaRepartidorDao
    abstract fun produccionDao(): ProduccionDao
    abstract fun configuracionProduccionDao(): ConfiguracionProduccionDao
    abstract fun repartidorDao(): RepartidorDao
    abstract fun seguridadConfigDao(): SeguridadConfigDao
}
