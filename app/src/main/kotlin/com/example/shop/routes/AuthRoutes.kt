package com.example.shop.routes

import com.example.shop.domain.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.shop.domain.model.LoginRequest
import com.example.shop.domain.model.RegisterRequest

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            call.respond(authService.register(call.receive<RegisterRequest>()))
        }
        post("/login") {
            call.respond(authService.login(call.receive<LoginRequest>()))
        }
    }
}
