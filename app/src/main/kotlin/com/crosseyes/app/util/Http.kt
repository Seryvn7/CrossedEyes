package com.crosseyes.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL as JUrl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class HttpResult(
    val code: Int,
    val body: String,
    val contentType: String
) {
    val ok: Boolean get() = code in 200..299
}

/** Minimal non-blocking HTTP client using HttpURLConnection on the network dispatcher. */
suspend fun httpGet(url: String, headers: Map<String, String> = emptyMap(), timeoutMs: Int = 20000): HttpResult =
    withContext(Dispatchers.IO) { httpCall("GET", url, null, headers, timeoutMs) }

suspend fun httpPostJson(url: String, json: String, headers: Map<String, String> = emptyMap(), timeoutMs: Int = 60000): HttpResult =
    withContext(Dispatchers.IO) { httpCall("POST", url, json, headers, timeoutMs) }

suspend fun downloadBytes(url: String, headers: Map<String, String> = emptyMap(), timeoutMs: Int = 60000): ByteArray =
    withContext(Dispatchers.IO) { download(url, headers, timeoutMs) }

private suspend fun httpCall(
    method: String,
    url: String,
    json: String?,
    headers: Map<String, String>,
    timeoutMs: Int
): HttpResult = suspendCancellableCoroutine { cont ->
    if (!cont.isActive) return@suspendCancellableCoroutine
    val conn = try {
        (JUrl(url).openConnection() as HttpURLConnection).also {
            it.requestMethod = method
            it.connectTimeout = timeoutMs
            it.readTimeout = timeoutMs
            it.doInput = json != null
            it.instanceFollowRedirects = true
            if (json != null) {
                it.setRequestProperty("Content-Type", "application/json")
                it.outputStream.use { it.write(json.toByteArray()) }
            }
            headers.forEach { (k, v) -> it.setRequestProperty(k, v) }
        }
    } catch (e: Throwable) {
        if (cont.isActive) cont.resumeWithException(e)
        return@suspendCancellableCoroutine
    }
    try {
        val code = conn.responseCode
        val isErr = code !in 200..299
        val stream = if (isErr) conn.errorStream ?: conn.inputStream else conn.inputStream
        val body = stream.use { it.readBytes() }.toString(Charsets.UTF_8).take(200_000)
        if (cont.isActive) cont.resume(HttpResult(code, body, conn.contentType ?: "text/plain"))
    } catch (e: Throwable) {
        if (cont.isActive) cont.resumeWithException(e)
    } finally {
        conn.disconnect()
    }
}

private suspend fun download(url: String, headers: Map<String, String>, timeoutMs: Int): ByteArray =
    suspendCancellableCoroutine { cont ->
        if (!cont.isActive) return@suspendCancellableCoroutine
        val conn = try {
            (JUrl(url).openConnection() as HttpURLConnection).also {
                it.connectTimeout = timeoutMs
                it.readTimeout = timeoutMs
                headers.forEach { (k, v) -> it.setRequestProperty(k, v) }
            }
        } catch (e: Throwable) {
            if (cont.isActive) cont.resumeWithException(e)
            return@suspendCancellableCoroutine
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                if (cont.isActive) cont.resumeWithException(Exception("HTTP $code"))
                return@suspendCancellableCoroutine
            }
            val bytes = conn.inputStream.use { it.readBytes() }
            if (cont.isActive) cont.resume(bytes)
        } catch (e: Throwable) {
            if (cont.isActive) cont.resumeWithException(e)
        } finally {
            conn.disconnect()
        }
    }
