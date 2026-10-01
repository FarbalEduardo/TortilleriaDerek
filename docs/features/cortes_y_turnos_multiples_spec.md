# Especificación: Múltiples Cierres de Corte de Ventas en un Mismo Día

## 1. Objetivo
Permitir que la Tortillería Derek opere múltiples turnos y genere más de dos cierres de corte de ventas en una misma fecha cronológica (ej. Turno Matutino, Turno Vespertino, Turno Nocturno o arqueos de relevo de cajero), garantizando el aislamiento contable de cada corte, la apertura fluida del turno subsiguiente y la generación de reportes individuales y consolidados diarios (Corte Z).

---

## 2. Historias de Usuario (US)

*   **US1 (Identificación Secuencial de Cortes):** Como cajero o administrador, al realizar un corte en el día, el sistema debe asignarle automáticamente un número correlativo en el día (`numeroTurnoDia = 1, 2, 3...`) y un identificador único legible (`folioCorte`: ej. `CORTE-20260917-01`, `CORTE-20260917-02`).
*   **US2 (Aislamiento de Transacciones por Corte):** Toda venta efectuada en mostrador y toda liquidación de repartidor debe asociarse estrictamente al `turnoId` activo. Las transacciones del Corte #1 quedan congeladas al cerrarse y no se mezclan con las del Corte #2.
*   **US3 (Arqueo de Caja y Fondo Inicial):** Al realizar el cierre de cualquier turno, el sistema debe calcular el total esperado (ventas en mostrador + cobros de reparto), solicitar el efectivo físico contado, calcular automáticamente cualquier diferencia (sobrante/faltante) y registrar el fondo de caja asignado para el siguiente turno.
*   **US4 (Apertura Inmediata de Turno Subsiguiente):** Tras cerrar un turno previo en cualquier momento del día, el sistema queda listo para que el mismo u otro operador inicie sesión y abra el siguiente turno (`numeroTurnoDia + 1`) de manera transparente.
*   **US5 (Consulta Individual y Consolidada del Día):** En las pantallas de Historial y Métricas, el administrador debe poder filtrar las ventas por corte específico (`Corte #1`, `Corte #2`, `Corte #3...`) o visualizar el **Reporte Consolidado Diario** (suma de todos los cortes cerrados más el turno en curso de la fecha).
*   **US6 (Auditoría y Exportación SAF):** El sistema debe permitir exportar a CSV mediante SAF el desglose de cada corte individual o el gran consolidado diario, incluyendo usuario que abrió, usuario que cerró, horas, montos y checksum SHA-256.

---

## 3. Criterios de Seguridad y Reglas de Negocio (by 🛡️ security-expert)

*   **Inmutabilidad de Cortes Cerrados:** Un turno en estado `CERRADO` es estrictamente de solo lectura en la base de datos local Room. Ninguna venta posterior puede ser insertada con un `turnoId` cerrado.
*   **Transaccionalidad Atómica:** El cierre de turno y la consolidación de montos se ejecutan dentro de una transacción Room (`@Transaction`) para prevenir inconsistencias si la app se suspende durante el proceso de arqueo.
*   **Control de Discrepancias:** Si la diferencia entre el efectivo contado y el esperado excede un margen de tolerancia (ej. ±$50.00 MXN), el sistema requiere una nota explicativa o confirmación explícita del operador antes de finalizar el cierre.

---

## 4. Diseño Técnico y Arquitectura (by 🏗️ mobile-developer)

### 4.1. Modelo de Datos Room (`data/local/entity`)

#### `TurnoEntity` (Actualización)
```kotlin
@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey val id: String, // UUID v4
    val folioCorte: String,     // Ej: "CORTE-20260917-01"
    val fechaDiaTexto: String,  // Formato "yyyy-MM-dd" para agrupaciones diarias eficientes
    val numeroTurnoDia: Int,    // 1, 2, 3... correlativo del día
    val fechaApertura: Long,    // Timestamp millis
    val fechaCierre: Long? = null,
    val estado: String,         // "ABIERTO", "CERRADO"
    val usuarioId: String,      // Quien abrió el turno
    val usuarioCierreId: String? = null,
    val fondoInicial: Double = 0.0,
    val totalVentasMostrador: Double = 0.0,
    val totalCobradoReparto: Double = 0.0,
    val totalVentas: Double = 0.0,
    val efectivoEsperado: Double = 0.0,
    val efectivoContado: Double = 0.0,
    val diferenciaArqueo: Double = 0.0,
    val notasCierre: String? = null
)
```

#### Relaciones
- `VentaEntity.turnoId` -> Clave foránea indexada a `TurnoEntity.id`.
- `RutaRepartidorEntity.turnoId` -> Clave foránea indexada a `TurnoEntity.id`.

### 4.2. Operaciones DAO (`TurnoDao`)
```kotlin
@Dao
interface TurnoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoEntity)

    @Update
    suspend fun updateTurno(turno: TurnoEntity)

    // Obtener turno actualmente abierto (máximo 1 simultáneo)
    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' ORDER BY fechaApertura DESC LIMIT 1")
    fun getTurnoActivo(): Flow<TurnoEntity?>

    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' ORDER BY fechaApertura DESC LIMIT 1")
    suspend fun getTurnoActivoSync(): TurnoEntity?

    // Calcular el siguiente número de turno para el día dado
    @Query("SELECT COALESCE(MAX(numeroTurnoDia), 0) + 1 FROM turnos WHERE fechaDiaTexto = :fechaDiaTexto")
    suspend fun getSiguienteNumeroTurnoDia(fechaDiaTexto: String): Int

    // Lista de todos los cortes ocurridos en un día particular
    @Query("SELECT * FROM turnos WHERE fechaDiaTexto = :fechaDiaTexto ORDER BY numeroTurnoDia ASC")
    fun getCortesDelDia(fechaDiaTexto: String): Flow<List<TurnoEntity>>

    // Resumen consolidado diario (Suma de todos los cortes de esa fecha)
    @Query("""
        SELECT 
            COUNT(id) as cantidadCortes,
            SUM(totalVentasMostrador) as granTotalMostrador,
            SUM(totalCobradoReparto) as granTotalReparto,
            SUM(totalVentas) as granTotalDia,
            SUM(diferenciaArqueo) as balanceDiferencias
        FROM turnos 
        WHERE fechaDiaTexto = :fechaDiaTexto
    """)
    fun getConsolidadoDiario(fechaDiaTexto: String): Flow<ConsolidadoDiarioResult?>
}
```

### 4.3. Casos de Uso (Domain)
1. **`AbrirNuevoTurnoUseCase(usuarioId, fondoInicial)`**:
   - Obtiene la fecha actual en formato `yyyy-MM-dd`.
   - Consulta `getSiguienteNumeroTurnoDia(fechaDiaTexto)` para determinar si es el turno 1, 2, 3 o posterior.
   - Genera el folio `CORTE-YYYYMMDD-0X`.
   - Inserta el nuevo `TurnoEntity` con estado `ABIERTO`.
2. **`CalcularArqueoTurnoUseCase(turnoId)`**:
   - Suma en tiempo real las ventas de mostrador y liquidaciones de reparto ligadas al `turnoId`.
   - Suma el `fondoInicial` para calcular el `efectivoEsperado`.
3. **`CerrarTurnoConArqueoUseCase(turnoId, efectivoContado, usuarioCierreId, notas)`**:
   - Ejecuta transacción: actualiza `estado = 'CERRADO'`, montos finales, diferencia y `fechaCierre = now()`.
4. **`ConsultarCortesYConsolidadoUseCase(fechaDiaTexto)`**:
   - Emite la lista de cortes del día y los totales consolidados.

---

## 5. Diseño de Interfaz y UX (by 🎨 design-ui-expert)

### 5.1. Hero Sales Card (Mostrador)
- En la cabecera del Mostrador se muestra la etiqueta dinámica del turno:
  `"Turno #2 • Corte en Curso"` junto a la fecha.
- El botón de cierre indica claramente `"Cerrar Turno / Corte #X"`.

### 5.2. Diálogo de Cierre y Arqueo de Turno
- **Header:** "Corte de Caja - Turno #X" con folio `CORTE-YYYYMMDD-0X`.
- **Desglose de Cifras:**
  - Ventas Mostrador: `$X,XXX.XX`
  - Cobros Repartidores: `$X,XXX.XX`
  - Fondo Inicial: `$XXX.XX`
  - **Total Efectivo Esperado en Caja:** `$X,XXX.XX`
- **Campo de Captura:** "Efectivo Físico Contado" (numérico con teclado de moneda).
- **Indicador Dinámico:** Muestra en tiempo real "Diferencia: $0.00 (Exacto)", "+$XX.XX (Sobrante)" o "-$XX.XX (Faltante)".
- **Acción:** Botón "Finalizar y Cerrar Corte #X".

### 5.3. Historial de Ventas con Selector de Cortes
- Selector horizontal de chips en la pantalla de Historial:
  `[ Consolidado Día ] [ Corte #1 ] [ Corte #2 ] [ Corte #3 ]`
- Al seleccionar un corte específico, el listado filtra solo las ventas y liquidaciones de ese turno.
- Al seleccionar "Consolidado Día", muestra el gran acumulado del día con separación visual por turnos.

---

## 6. Plan de Contingencia y Casos Extremos

| Escenario | Comportamiento del Sistema |
| :--- | :--- |
| **Más de 2 cortes en el mismo día (3, 4 o más)** | El sistema no tiene límite arbitrario de cortes diarios. Cada nuevo turno incrementa `numeroTurnoDia` correlativamente (`01`, `02`, `03`, `04`...). |
| **Intento de abrir un turno cuando ya hay uno abierto** | El sistema valida previamente con `getTurnoActivoSync()`. Si ya existe uno abierto, redirige al `Mostrador` existente y notifica que se debe cerrar el actual antes de iniciar uno nuevo. |
| **Corte que cruza la medianoche (ej. abre 11 PM y cierra 2 AM)** | El `turnoId` y su fecha de apertura determinan la pertenencia del turno; las ventas se vinculan por `turnoId`, garantizando que ninguna transacción quede huérfana. |
| **Apagado inesperado del dispositivo en pleno turno** | Al reiniciar, la bandera de Room detecta el turno en estado `ABIERTO` y el usuario regresa inmediatamente al Mostrador con todas las ventas previas intactas. |

---

## 7. Criterios de Aceptación y QA (by 🏆 quality-pm-expert)

- [ ] **Test (Numeración Secuencial de Cortes):**
  - Dado un día sin cortes previos, al abrir turno se le asigna `numeroTurnoDia = 1` y folio terminado en `-01`.
  - Al cerrar dicho turno y abrir otro el mismo día, se le asigna `numeroTurnoDia = 2` y folio terminado en `-02`.
  - Al cerrar el segundo y abrir un tercero, se le asigna `numeroTurnoDia = 3` y folio terminado en `-03`.
- [ ] **Test (Aislamiento de Ventas por Corte):**
  - Las ventas registradas en el Turno 1 no figuran en el total ni en el arqueo del Turno 2 ni del Turno 3.
- [ ] **Test (Consolidado Diario de 3+ Cortes):**
  - La consulta consolidada de la fecha suma con exactitud los totales de los cortes 1, 2 y 3 del día.
- [ ] **Test (Diferencia de Arqueo):**
  - El cálculo de `diferenciaArqueo = efectivoContado - efectivoEsperado` es matemáticamente exacto para sobrantes, faltantes y valores exactos.
