package com.example.solanakotlincomposescaffold.networking

import com.solana.networking.HttpNetworkDriver
import com.solana.networking.HttpRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod

class KtorHttpDriver : HttpNetworkDriver {
    override suspend fun makeHttpRequest(request: HttpRequest): String =
        HttpClient(Android).use { client ->
            client.request(request.url) {
                method = HttpMethod.parse(request.method)
                request.properties.forEach { (k, v) ->
                    header(k, v)
                }
                setBody(request.body)
            }.bodyAsText()
        }
}