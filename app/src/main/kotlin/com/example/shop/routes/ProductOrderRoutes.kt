package com.example.shop.routes

import com.example.shop.domain.model.*
import com.example.shop.domain.service.OrderService
import com.example.shop.domain.service.ProductService
import com.example.shop.domain.service.StatsService
import com.example.shop.plugins.requireAdmin
import com.example.shop.security.AUTH_SCHEME
import com.example.shop.security.JwtPrincipalData
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productService: ProductService, orderService: OrderService, statsService: StatsService) {
    route("/products") {
        get { call.respond(productService.list()) }
        get("/{id}") { call.respond(productService.get(call.parameters["id"]!!.toLong())) }

        authenticate(AUTH_SCHEME) {
            post {
                val principal = call.principal<JwtPrincipalData>()!!
                requireAdmin(principal.role)
                call.respond(productService.create(call.receive<CreateProductRequest>()))
            }
            put("/{id}") {
                val principal = call.principal<JwtPrincipalData>()!!
                requireAdmin(principal.role)
                call.respond(productService.update(call.parameters["id"]!!.toLong(), call.receive<UpdateProductRequest>()))
            }
            delete("/{id}") {
                val principal = call.principal<JwtPrincipalData>()!!
                requireAdmin(principal.role)
                productService.delete(call.parameters["id"]!!.toLong())
                call.respond(ApiMessage("Deleted"))
            }
        }
    }

    authenticate(AUTH_SCHEME) {
        route("/orders") {
            post {
                val principal = call.principal<JwtPrincipalData>()!!
                call.respond(orderService.create(principal.userId, call.receive<CreateOrderRequest>()))
            }
            get {
                val principal = call.principal<JwtPrincipalData>()!!
                call.respond(orderService.list(principal.userId))
            }
            delete("/{id}") {
                val principal = call.principal<JwtPrincipalData>()!!
                orderService.cancel(principal.userId, call.parameters["id"]!!.toLong())
                call.respond(ApiMessage("Cancelled"))
            }
        }

        get("/stats/orders") {
            val principal = call.principal<JwtPrincipalData>()!!
            requireAdmin(principal.role)
            call.respond(statsService.ordersStats())
        }
    }
}
