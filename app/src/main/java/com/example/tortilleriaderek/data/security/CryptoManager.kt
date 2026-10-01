package com.example.tortilleriaderek.data.security

import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Creado por 🛡️ security-expert.
 * Se encarga del Hashing de contraseñas usando PBKDF2 (Hash + Salt).
 * Las contraseñas en texto plano jamás llegan a Room.
 * Utiliza java.util.Base64 (nativo en Android API 26+ y JVM estándar).
 */
object CryptoManager {
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hash)
    }
}
