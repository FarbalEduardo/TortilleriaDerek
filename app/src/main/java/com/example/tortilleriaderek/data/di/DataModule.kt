package com.example.tortilleriaderek.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tortilleriaderek.data.local.TortilleriaDatabase
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.repository.AuthRepositoryImpl
import com.example.tortilleriaderek.data.security.CryptoManager
import com.example.tortilleriaderek.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        usuarioDaoProvider: Provider<UsuarioDao>
    ): TortilleriaDatabase {
        return Room.databaseBuilder(
            context,
            TortilleriaDatabase::class.java,
            "tortilleria_db"
        ).addMigrations(*com.example.tortilleriaderek.data.local.migration.Migrations.ALL_MIGRATIONS)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Creado por 🛡️ security-expert y 🏗️ mobile-developer
                // Insertamos los usuarios predeterminados (US1) de forma segura en la creación
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = usuarioDaoProvider.get()
                    
                    val salt1 = CryptoManager.generateSalt()
                    dao.insertUsuario(UsuarioEntity("1", "admin1", CryptoManager.hashPassword("admin123", salt1), salt1, "ADMIN"))
                }
            }
        }).build()
    }

    @Provides
    fun provideUsuarioDao(database: TortilleriaDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    fun provideTurnoDao(database: TortilleriaDatabase): TurnoDao = database.turnoDao()

    @Provides
    fun provideVentaDao(database: TortilleriaDatabase): com.example.tortilleriaderek.data.local.dao.VentaDao = database.ventaDao()

    @Provides
    fun provideRutaRepartidorDao(database: TortilleriaDatabase): com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao = database.rutaRepartidorDao()

    @Provides
    fun provideProduccionDao(database: TortilleriaDatabase): com.example.tortilleriaderek.data.local.dao.ProduccionDao = database.produccionDao()

    @Provides
    fun provideConfiguracionProduccionDao(database: TortilleriaDatabase): com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao = database.configuracionProduccionDao()

    @Provides
    fun provideRepartidorDao(database: TortilleriaDatabase): com.example.tortilleriaderek.data.local.dao.RepartidorDao = database.repartidorDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBackupStoragePort(
        backupStoragePortImpl: com.example.tortilleriaderek.data.repository.BackupStoragePortImpl
    ): com.example.tortilleriaderek.domain.repository.BackupStoragePort

    @Binds
    @Singleton
    abstract fun bindBatteryStatusProvider(
        defaultBatteryStatusProvider: com.example.tortilleriaderek.data.system.DefaultBatteryStatusProvider
    ): com.example.tortilleriaderek.domain.model.BatteryStatusProvider
}
