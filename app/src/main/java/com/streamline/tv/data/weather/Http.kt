package com.streamline.tv.data.weather

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal fun httpGet(url: String, timeoutMs: Int = 12_000): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = timeoutMs
        readTimeout = timeoutMs
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "StreamLineTV/0.1 (Android TV)")
    }
    return try {
        val code = connection.responseCode
        if (code !in 200..299) throw IOException("HTTP $code for $url")
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}
