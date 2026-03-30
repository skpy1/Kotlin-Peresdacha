package com.example.shop.plugins

import com.auth0.jwt.JWT
import com.example.shop.config.JwtConfig
import com.example.shop.domain.model.ApiMessage
import com.example.shop.domain.model.UserRole
import com.example.shop.domain.service.AuthService
import com.example.shop.domain.service.OrderService
import com.example.shop.domain.service.ProductService
import com.example.shop.domain.service.StatsService
import com.example.shop.routes.authRoutes
import com.example.shop.routes.productRoutes
import com.example.shop.security.AUTH_SCHEME
import com.example.shop.util.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(ContentNegotiation) { json(AppJson) }
}

fun Application.configureMonitoring() {
    install(CallLogging)
    install(StatusPages) {
        exception<ValidationException> { call, cause -> call.respond(HttpStatusCode.BadRequest, ApiMessage(cause.message ?: "Validation error")) }
        exception<UnauthorizedException> { call, cause -> call.respond(HttpStatusCode.Unauthorized, ApiMessage(cause.message ?: "Unauthorized")) }
        exception<ForbiddenException> { call, cause -> call.respond(HttpStatusCode.Forbidden, ApiMessage(cause.message ?: "Forbidden")) }
        exception<NotFoundException> { call, cause -> call.respond(HttpStatusCode.NotFound, ApiMessage(cause.message ?: "Not found")) }
        exception<ConflictException> { call, cause -> call.respond(HttpStatusCode.Conflict, ApiMessage(cause.message ?: "Conflict")) }
        exception<BusinessException> { call, cause -> call.respond(HttpStatusCode.UnprocessableEntity, ApiMessage(cause.message ?: "Business error")) }
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, ApiMessage("Internal server error"))
        }
    }
}

fun Application.configureHttp() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }
}

fun Application.configureSecurity(authService: AuthService, jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt(AUTH_SCHEME) {
            realm = jwtConfig.realm
            verifier(
                JWT.require(authService.verifierAlgorithm())
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.subject?.toLongOrNull() ?: return@validate null
                authService.findUser(userId)?.let {
                    authService.principalFromClaims(userId, credential.payload.getClaim("email").asString(), credential.payload.getClaim("role").asString())
                }
            }
        }
    }
}

fun Application.configureRouting(
    authService: AuthService,
    productService: ProductService,
    orderService: OrderService,
    statsService: StatsService,
) {
    routing {
        get("/") { call.respond(ApiMessage("Shop backend is running")) }
        get("/openapi") {
            val yaml = this::class.java.classLoader.getResource("openapi/documentation.yaml")?.readText()
                ?: error("OpenAPI file not found")
            call.respondText(yaml, ContentType.parse("application/yaml"))
        }
        get("/swagger") {
            val html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8" />
                  <title>Swagger UI</title>
                  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                  <script>
                    window.ui = SwaggerUIBundle({
                      url: '/openapi',
                      dom_id: '#swagger-ui'
                    });
                  </script>
                </body>
                </html>
            """.trimIndent()
            call.respondText(html, ContentType.Text.Html)
        }

        authRoutes(authService)
        productRoutes(productService, orderService, statsService)
    }
}

fun requireAdmin(role: UserRole) {
    if (role != UserRole.ADMIN) throw ForbiddenException("Admin access required")
}
