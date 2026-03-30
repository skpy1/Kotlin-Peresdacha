package com.example.shop.security

import com.example.shop.domain.model.UserRole
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

const val AUTH_SCHEME = "auth-jwt"

data class JwtPrincipalData(val userId: Long, val email: String, val role: UserRole) : Principal

fun JWTPrincipal.toPrincipalData(): JwtPrincipalData = JwtPrincipalData(
    userId = getClaim("sub", String::class)?.toLong() ?: subject?.toLong() ?: error("Missing subject"),
    email = getClaim("email", String::class) ?: error("Missing email"),
    role = UserRole.valueOf(getClaim("role", String::class) ?: error("Missing role")),
)
