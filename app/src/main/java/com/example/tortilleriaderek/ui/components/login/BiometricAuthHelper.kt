package com.example.tortilleriaderek.ui.components.login

import android.app.Activity
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Creado por 🛡️ security-expert y 🏗️ mobile-developer.
 * Utiliza el framework nativo de Android (API 29+) para autenticación biométrica offline.
 */
object BiometricAuthHelper {

    fun tieneHardwareBiometrico(context: Context): Boolean {
        return try {
            val biometricManager = context.getSystemService(BiometricManager::class.java) ?: return false
            biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    fun autenticar(
        activity: Activity,
        titulo: String = "Tortillería Derek POS",
        subtitulo: String = "Toca el sensor de huella dactilar para acceder",
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val cancellationSignal = CancellationSignal()

            val prompt = BiometricPrompt.Builder(activity)
                .setTitle(titulo)
                .setSubtitle(subtitulo)
                .setNegativeButton("Usar PIN", executor) { _, _ ->
                    cancellationSignal.cancel()
                }
                .build()

            prompt.authenticate(
                cancellationSignal,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        onExito()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                            onError(errString?.toString() ?: "Error de autenticación")
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(activity, "Huella no reconocida. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (e: Exception) {
            onError(e.message ?: "Sensor biométrico no disponible")
        }
    }
}
