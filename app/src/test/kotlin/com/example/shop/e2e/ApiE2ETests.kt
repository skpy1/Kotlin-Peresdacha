package com.example.shop.e2e

import com.example.shop.domain.model.ApiMessage
import com.example.shop.plugins.configureHttp
import com.example.shop.plugins.configureMonitoring
import com.example.shop.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiE2ETests {
    @Test
    fun `health endpoint returns 200`() = testApplication {
        application {
            configureSerialization()
            configureMonitoring()
            configureHttp()
            routing { get("/") { call.respond(ApiMessage("ok")) } }
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `missing endpoint returns 404`() = testApplication {
        application {
            configureSerialization()
            configureMonitoring()
            configureHttp()
            routing { get("/") { call.respond(ApiMessage("ok")) } }
        }
        val response = client.get("/missing")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
