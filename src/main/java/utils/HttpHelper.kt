package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val HttpReadTimeout = 60L
private const val HttpConnectTimeout = 5L

private var httpClient = OkHttpClient.Builder()
    .connectTimeout(HttpConnectTimeout, TimeUnit.SECONDS)
    .readTimeout(HttpReadTimeout, TimeUnit.SECONDS)
    .build()

fun resetHttpClient(host: String? = null, port: Int? = null) {
    val proxy = if (host.isNullOrEmpty() || port == null) {
        null
    } else Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port))

    httpClient = OkHttpClient.Builder()
        .connectTimeout(HttpConnectTimeout, TimeUnit.SECONDS)
        .readTimeout(HttpReadTimeout, TimeUnit.SECONDS)
        .proxy(proxy)
        .build()
}

suspend fun String.httpGet(): String {
    val request = Request.Builder().url(this).build()

    return suspendCoroutine<String> { handler ->
        httpClient.newCall(request).execute().use {
            if (it.isSuccessful) {
                handler.resume(it.body?.string() ?: "")
            } else {
                handler.resume("")
            }
        }
    }
}