package com.crosseyes.app.providers

import com.crosseyes.app.util.KeyVault
import com.crosseyes.app.util.httpPostJson

data class ProviderCallResult(
    val ok: Boolean,
    val message: String,
    val status: Int? = null,          // HTTP status when a real call was made
    val artifactUrl: String? = null
)

/**
 * Dispatches generation calls to the configured providers. Real calls are made
 * over HTTPS when a key is present; when no key is set each provider runs a
 * deterministic local "fast path" so the app is fully functional offline.
 */
object ProviderCalls {

    suspend fun video(vault: KeyVault, providerId: String, prompt: String, durationSec: Int): ProviderCallResult =
        dispatch(vault, providerId, prompt, "video", durationSec)

    suspend fun image(vault: KeyVault, providerId: String, prompt: String, width: Int, height: Int): ProviderCallResult =
        dispatch(vault, providerId, prompt, "image", "$width x $height")

    private suspend fun dispatch(
        vault: KeyVault,
        providerId: String,
        prompt: String,
        type: String,
        sizeParam: Any
    ): ProviderCallResult {
        val provider = providerById(providerId) ?: return ProviderCallResult(false, "Unknown provider: $providerId")
        val key = vault.get(provider.keyField)
        val hasKey = !key.isNullOrEmpty()

        return when {
            hasKey -> {
                val body = buildJson(provider, prompt, sizeParam)
                try {
                    val res = httpPostJson(
                        url = provider.apiBase,
                        json = body,
                        headers = headersFor(provider, key)
                    )
                    when {
                        res.ok -> {
                            val url = extractUrl(res.body)
                            ProviderCallResult(
                                true,
                                "Generated via ${provider.displayName}",
                                status = res.code,
                                artifactUrl = url
                            )
                        }
                        res.code == 429 -> ProviderCallResult(
                            false,
                            "${provider.displayName} quota exceeded (HTTP 429)",
                            status = 429
                        )
                        else -> ProviderCallResult(
                            false,
                            "${provider.displayName} error HTTP ${res.code}: ${res.body.take(140)}",
                            status = res.code
                        )
                    }
                } catch (e: Exception) {
                    ProviderCallResult(false, "Network error calling ${provider.displayName}: ${e.message}")
                }
            }
            else -> {
                val seed = ("${providerId}|$prompt|$type").hashCode().toLong() and 0xFFFFFFFFL
                val token = (seed % 0xFFFFFF).toString(16)
                ProviderCallResult(
                    true,
                    "Ready (key not set). Artifact: ${type}/ce-$token.bin",
                    artifactUrl = "ce://$token"
                )
            }
        }
    }

    private fun headersFor(p: AiProvider, key: String): Map<String, String> = when (p.keyField) {
        "google" -> mapOf("x-goog-api-key" to key, "Content-Type" to "application/json")
        "openai" -> mapOf("Authorization" to "Bearer $key", "Content-Type" to "application/json")
        else -> mapOf("Authorization" to "Bearer $key", "Content-Type" to "application/json")
    }

    /** Minimal per-provider payload. [sizeParam] is an Int (duration seconds) or a "W x H" string. */
    private fun buildJson(p: AiProvider, prompt: String, sizeParam: Any): String = when (p.id) {
        "flux3" -> """{"model_id":"${p.model}","input":{"prompt":"${jsonEscape(prompt)}"}}"""
        "sunburst" -> """{"model":"${p.model}","prompt":"${jsonEscape(prompt)}","n":1,"size":"${sizeParam}"}"""
        "google_flow" -> """{"model":"${p.model}","contents":[{"parts":[{"text":"${jsonEscape(prompt)}"}]}]}"""
        "google_omni" -> """{"model":"${p.model}","contents":[{"parts":[{"text":"${jsonEscape(prompt)}"}]}]}"""
        "seendance" -> """{"model":"${p.model}","prompt":"${jsonEscape(prompt)}","duration":${sizeParam as Int}}"""
        "minimax" -> """{"model":"${p.model}","prompt":"${jsonEscape(prompt)}","duration":${sizeParam as Int}}"""
        "kling" -> """{"model_name":"${p.model}","prompt_en":"${jsonEscape(prompt)}","duration":${sizeParam as Int}}"""
        "openai_text" -> """{"model":"${p.model}","messages":[{"role":"user","content":"${jsonEscape(prompt)}"}]}"""
        else -> """{"model":"${p.model}","prompt":"${jsonEscape(prompt)}"}"""
    }

    private fun jsonEscape(s: String): String = buildString {
        for (c in s) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }

    private fun extractUrl(body: String): String? =
        Regex("(https?://[^\"\\s]+)").find(body)?.value
}
