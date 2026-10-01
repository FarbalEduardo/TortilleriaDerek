package com.example.tortilleriaderek.presentation.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val configuracionProduccionDao: ConfiguracionProduccionDao,
    private val repartidorDao: RepartidorDao,
    private val rutaRepartidorDao: RutaRepartidorDao,
    private val turnoDao: TurnoDao,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _configuracion = MutableStateFlow(ConfiguracionProduccionEntity())
    val configuracion: StateFlow<ConfiguracionProduccionEntity> = _configuracion.asStateFlow()

    val repartidores: StateFlow<List<RepartidorEntity>> = repartidorDao.getAllRepartidores()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val usuarios: StateFlow<List<UsuarioEntity>> = usuarioDao.getAllUsuarios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observarConfiguracion()
        verificarUsuarioSeed()
    }

    private fun verificarUsuarioSeed() {
        viewModelScope.launch {
            if (usuarioDao.countUsuarios() == 0) {
                val salt = CryptoManager.generateSalt()
                val hash = CryptoManager.hashPassword("admin123", salt)
                usuarioDao.insertUsuario(
                    UsuarioEntity(
                        id = "1",
                        username = "admin1",
                        passwordHash = hash,
                        salt = salt,
                        rol = "ADMIN"
                    )
                )
            }
        }
    }

    private fun observarConfiguracion() {
        viewModelScope.launch {
            configuracionProduccionDao.getConfiguracion().collectLatest { config ->
                if (config != null) {
                    _configuracion.value = config
                } else {
                    // Sembrar valores por defecto (todos en 0.0)
                    val defaultConfig = ConfiguracionProduccionEntity()
                    configuracionProduccionDao.insertOrUpdateConfiguracion(defaultConfig)
                    _configuracion.value = defaultConfig
                }
            }
        }
    }

    fun guardarEstandaresProduccion(
        pesoBulto: Double,
        kgMasa: Double,
        rendimiento: Double,
        merma: Double,
        porcentajeMerma: Double
    ) {
        viewModelScope.launch {
            val updated = _configuracion.value.copy(
                pesoBultoHarinaKg = pesoBulto,
                kgMasaPorBulto = kgMasa,
                rendimientoTortillaPorBulto = rendimiento,
                mermaToleradaKgPorBulto = merma,
                porcentajeMermaTolerada = porcentajeMerma,
                fechaModificacion = System.currentTimeMillis()
            )
            configuracionProduccionDao.insertOrUpdateConfiguracion(updated)
        }
    }

    fun guardarPrecioYPesoProducto(idProducto: String, nuevoPrecio: Double, nuevoPesoGramos: Int? = null) {
        viewModelScope.launch {
            val actual = _configuracion.value
            val updated = when (idProducto) {
                "1" -> actual.copy(
                    precioKilo = nuevoPrecio,
                    fechaModificacion = System.currentTimeMillis()
                )
                "2" -> actual.copy(
                    precioMedioPaquete = nuevoPrecio,
                    pesoMedioPaqueteGramos = nuevoPesoGramos ?: actual.pesoMedioPaqueteGramos,
                    fechaModificacion = System.currentTimeMillis()
                )
                "3" -> actual.copy(
                    precioPaquete = nuevoPrecio,
                    pesoPaqueteGramos = nuevoPesoGramos ?: actual.pesoPaqueteGramos,
                    fechaModificacion = System.currentTimeMillis()
                )
                "4" -> actual.copy(
                    precioPaqueteRepartidor = nuevoPrecio,
                    pesoPaqueteGramos = nuevoPesoGramos ?: actual.pesoPaqueteGramos,
                    fechaModificacion = System.currentTimeMillis()
                )
                else -> actual
            }
            configuracionProduccionDao.insertOrUpdateConfiguracion(updated)
        }
    }

    fun guardarPrecioProducto(idProducto: String, nuevoPrecio: Double) {
        guardarPrecioYPesoProducto(idProducto, nuevoPrecio, null)
    }

    fun guardarPrecioRepartidor(nuevoPrecio: Double) {
        guardarPrecioProducto("4", nuevoPrecio)
    }

    fun agregarRepartidor(nombre: String, motoRuta: String) {
        viewModelScope.launch {
            val actualList = repartidores.value
            val nextNum = String.format("#%02d", actualList.size + 1)
            val id = UUID.randomUUID().toString()

            val moto = if (motoRuta.contains("•")) {
                motoRuta.substringBefore("•").trim()
            } else {
                "Moto $nextNum"
            }

            val detalle = if (motoRuta.isNotBlank()) motoRuta.trim() else "Ruta General"

            val repartidor = RepartidorEntity(
                id = id,
                numeroBadge = nextNum,
                nombre = nombre.trim(),
                moto = moto,
                detalleRuta = detalle,
                fechaCreacion = System.currentTimeMillis()
            )
            repartidorDao.insertRepartidor(repartidor)

            // Si hay un turno activo en curso, agregamos la ruta correspondiente al turno
            val turnoActivo = turnoDao.getTurnoActivoSync()
            if (turnoActivo != null) {
                val rutaNombre = if (detalle.contains("•")) {
                    detalle.substringAfter("•").trim()
                } else {
                    detalle
                }
                val rutaEntity = RutaRepartidorEntity(
                    id = "${turnoActivo.id}_${id}",
                    turnoId = turnoActivo.id,
                    moto = moto,
                    repartidorNombre = nombre.trim(),
                    nombreRuta = rutaNombre,
                    status = "PENDIENTE_SALIDA",
                    cargaInicialKg = 0.0,
                    pendienteCobro = 0.00
                )
                rutaRepartidorDao.insertRuta(rutaEntity)
            }
        }
    }

    fun eliminarRepartidor(id: String) {
        viewModelScope.launch {
            val rep = repartidorDao.getRepartidorById(id)
            repartidorDao.deleteRepartidorById(id)

            // Si hay turno activo y la ruta no ha salido o fue generada para este repartidor, retirarla
            val turnoActivo = turnoDao.getTurnoActivoSync()
            if (turnoActivo != null) {
                rutaRepartidorDao.deleteRutaById("${turnoActivo.id}_${id}")
                if (rep != null) {
                    rutaRepartidorDao.deleteRutasByRepartidorNombre(turnoActivo.id, rep.nombre)
                }
            }
        }
    }

    fun crearUsuario(
        username: String,
        passwordRaw: String,
        rol: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val cleanUsername = username.trim()
            val cleanPassword = passwordRaw.trim()
            if (cleanUsername.length < 3) {
                onResult(false, "El nombre de usuario debe tener al menos 3 caracteres")
                return@launch
            }
            if (cleanPassword.length < 4) {
                onResult(false, "La contraseña debe tener al menos 4 caracteres")
                return@launch
            }
            val existing = usuarioDao.getUsuarioByUsername(cleanUsername)
            if (existing != null) {
                onResult(false, "El usuario '$cleanUsername' ya existe en el sistema")
                return@launch
            }
            val salt = CryptoManager.generateSalt()
            val hash = CryptoManager.hashPassword(cleanPassword, salt)
            val entity = UsuarioEntity(
                id = UUID.randomUUID().toString(),
                username = cleanUsername,
                passwordHash = hash,
                salt = salt,
                rol = rol
            )
            usuarioDao.insertUsuario(entity)
            onResult(true, null)
        }
    }

    fun editarUsuario(
        id: String,
        nuevoUsername: String,
        nuevoRol: String,
        nuevaPasswordRaw: String?,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val cleanUsername = nuevoUsername.trim()
            if (cleanUsername.length < 3) {
                onResult(false, "El nombre de usuario debe tener al menos 3 caracteres")
                return@launch
            }
            val usuarioActual = usuarioDao.getUsuarioById(id)
            if (usuarioActual == null) {
                onResult(false, "Usuario no encontrado")
                return@launch
            }
            // Si cambia de nombre, validar que no choque con otro
            if (!usuarioActual.username.equals(cleanUsername, ignoreCase = true)) {
                val existing = usuarioDao.getUsuarioByUsername(cleanUsername)
                if (existing != null && existing.id != id) {
                    onResult(false, "El nombre de usuario '$cleanUsername' ya está en uso")
                    return@launch
                }
            }
            // Validar no dejar el sistema sin ADMIN
            if (usuarioActual.rol == "ADMIN" && nuevoRol != "ADMIN") {
                val adminCount = usuarioDao.countAdmins()
                if (adminCount <= 1) {
                    onResult(false, "No es posible cambiar el rol: debe existir al menos un Administrador en el sistema.")
                    return@launch
                }
            }
            val updated = if (!nuevaPasswordRaw.isNullOrBlank()) {
                val salt = CryptoManager.generateSalt()
                val hash = CryptoManager.hashPassword(nuevaPasswordRaw.trim(), salt)
                usuarioActual.copy(username = cleanUsername, rol = nuevoRol, salt = salt, passwordHash = hash)
            } else {
                usuarioActual.copy(username = cleanUsername, rol = nuevoRol)
            }
            usuarioDao.insertUsuario(updated)
            onResult(true, null)
        }
    }

    fun eliminarUsuario(id: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val usuario = usuarioDao.getUsuarioById(id)
            if (usuario == null) {
                onResult(false, "Usuario no encontrado")
                return@launch
            }
            if (usuario.rol == "ADMIN") {
                val adminCount = usuarioDao.countAdmins()
                if (adminCount <= 1) {
                    onResult(false, "Operación bloqueada: No se puede eliminar al único Administrador del sistema.")
                    return@launch
                }
            }
            usuarioDao.deleteUsuarioById(id)
            onResult(true, null)
        }
    }
}
