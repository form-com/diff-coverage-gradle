package com.form.coverage.gradle

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class MockHttpServer(
    port: Int,
    responseContent: String
): AutoCloseable {

    private val httpServer = HttpServer.create(InetSocketAddress(port), 0)
    private val response: ByteArray = responseContent.toByteArray()

    init {
        httpServer.createContext("/") { httpExchange ->
            httpExchange.sendResponseHeaders(200, response.size.toLong())
            httpExchange.responseBody.use {
                it.write(response)
            }
        }
        httpServer.executor = null
        httpServer.start()
    }

    override fun close() {
        httpServer.stop(3)
    }

}
