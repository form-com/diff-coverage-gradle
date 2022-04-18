package com.form.coverage.http

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder

fun executeGetRequest(url: String): String {
    return HttpClientBuilder.create().build().use { httpClient ->
        val httpResponse = httpClient.execute(HttpGet(url))

        httpResponse.entity.content
            .bufferedReader()
            .readText()
    }
}
