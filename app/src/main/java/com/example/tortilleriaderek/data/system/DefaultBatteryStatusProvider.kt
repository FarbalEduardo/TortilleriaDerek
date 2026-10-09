package com.example.tortilleriaderek.data.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.tortilleriaderek.domain.model.BatteryStatusProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Implementación de producción para consultar el nivel de batería y estado de carga
 * mediante el BatteryManager nativo del sistema operativo Android.
 */
@Singleton
class DefaultBatteryStatusProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryStatusProvider {

    override fun obtenerNivelBateria(): Int {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            100 // Fallback seguro si el sensor del dispositivo no responde
        }
    }

    override fun estaCargando(): Boolean {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
}
