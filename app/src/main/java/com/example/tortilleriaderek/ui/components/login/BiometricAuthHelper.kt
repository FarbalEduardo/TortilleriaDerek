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
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
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
            val biometricManager = activity.getSystemService(BiometricManager::class.java)
            if (biometricManager == null) {
                onError(activity.getString(com.example.tortilleriaderek.R.string.biometria_no_hardware))
                return
            }

            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    onError(activity.getString(com.example.tortilleriaderek.R.string.biometria_no_hardware))
                    return
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    onError(activity.getString(com.example.tortilleriaderek.R.string.biometria_no_huellas_registradas))
                    return
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    onError("El sensor de huellas dactilares no está disponible temporalmente.")
                    return
                }
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    // Proceder con el diálogo biométrico nativo
                }
                else -> {
                    // Si el sistema responde con otro estado, permitimos intentar o usar PIN
                }
            }

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
