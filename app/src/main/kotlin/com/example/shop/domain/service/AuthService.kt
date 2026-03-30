package com.example.shop.domain.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.shop.config.JwtConfig
import com.example.shop.domain.model.*
import com.example.shop.domain.repository.UserRepository
import com.example.shop.security.JwtPrincipalData
import com.example.shop.util.ConflictException
import com.example.shop.util.UnauthorizedException
import com.example.shop.util.ValidationException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class AuthService(
    private val jwtConfig: JwtConfig,
    private val userRepository: UserRepository,
) {
    fun register(request: RegisterRequest): AuthResponse {
        validateEmail(request.email)
        require(request.password.length >= 6) { "Password must have at least 6 characters" }
        if (userRepository.findByEmail(request.email) != null) throw ConflictException("User already exists")

        val passwordHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
        val role = if (request.email.endsWith("@admin.local")) UserRole.ADMIN else UserRole.USER
        val user = userRepository.create(request.email.trim().lowercase(), passwordHash, request.fullName.trim(), role)
        return AuthResponse(generateToken(user), user)
    }

    fun login(request: LoginRequest): AuthResponse {
        val row = userRepository.findByEmail(request.email.trim().lowercase()) ?: throw UnauthorizedException("Invalid credentials")
        val result = BCrypt.verifyer().verify(request.password.toCharArray(), row.passwordHash)
        if (!result.verified) throw UnauthorizedException("Invalid credentials")
        return AuthResponse(generateToken(row.user), row.user)
    }

    fun findUser(id: Long): User? = userRepository.findById(id)

    fun verifierAlgorithm(): Algorithm = Algorithm.HMAC256(jwtConfig.secret)

    fun generateToken(user: User): String {
        val expiresAt = Instant.now().plus(jwtConfig.expiresInMinutes, ChronoUnit.MINUTES)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withExpiresAt(Date.from(expiresAt))
            .sign(verifierAlgorithm())
    }

    fun principalFromClaims(userId: Long, email: String, role: String) = JwtPrincipalData(userId, email, UserRole.valueOf(role))

    private fun validateEmail(email: String) {
        if (!email.contains("@")) throw ValidationException("Invalid email")
    }
}
