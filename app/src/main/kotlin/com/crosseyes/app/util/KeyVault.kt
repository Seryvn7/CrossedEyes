package com.crosseyes.app.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** AES-256-GCM key vault stored in a file inside app_internal_storage.
 *  A random IV is generated per encryption and prepended to the ciphertext,
 *  so repeated saves keep working. */
class KeyVault(context: Context) {

    private val file = context.getDir("vault", Context.MODE_PRIVATE).resolve("keys.bin")
    private val keyBytes: ByteArray

    init {
        val keyFile = context.getDir("vault", Context.MODE_PRIVATE).resolve("vault.key")
        val random = SecureRandom()
        keyBytes = if (keyFile.exists()) keyFile.readBytes() else ByteArray(32).also { random.nextBytes(it) }.also { keyFile.writeBytes(it) }
    }

    data class Entry(val value: String)

    fun put(key: String, value: String) {
        val map = load()
        map[key] = Base64.encodeToString(value.toByteArray(), Base64.NO_WRAP)
        save(map)
    }

    fun get(key: String): String? {
        val v = load()[key] ?: return null
        return Base64.decode(v, Base64.NO_WRAP).toString(Charsets.UTF_8).trim().takeIf { it.isNotEmpty() }
    }

    fun remove(key: String) {
        val map = load().also { it.remove(key) }
        save(map)
    }

    fun all(): List<String> = load().keys.toList().sorted()

    private fun load(): MutableMap<String, String> {
        if (!file.exists()) return mutableMapOf()
        val plain = decrypt(file.readBytes())
        if (plain.isNullOrBlank()) return mutableMapOf()
        return plain.lines()
            .filter { it.contains('=') && !it.startsWith("#") }
            .associate { kv ->
                val idx = kv.indexOf('=')
                kv.substring(0, idx).trim().let { k ->
                    k to kv.substring(idx + 1).trim()
                }
            }
            .toMutableMap()
    }

    private fun save(map: MutableMap<String, String>) {
        if (map.isEmpty()) { file.delete(); return }
        val text = map.map { (k, v) -> "$k=$v" }.joinToString("\n")
        file.writeBytes(encrypt(text))
    }

    private fun encrypt(plain: String): ByteArray {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), IvParameterSpec(iv))
        val ct = cipher.doFinal(plain.toByteArray())
        return iv + ct
    }

    private fun decrypt(data: ByteArray): String? {
        return try {
            val iv = data.copyOfRange(0, 12)
            val ct = data.copyOfRange(12, data.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), IvParameterSpec(iv))
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
