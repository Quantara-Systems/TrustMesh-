package com.trustmesh.auth

import com.trustmesh.db.ActiveSessions
import com.trustmesh.db.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class SignupRequest(val email: String, val password: String, val displayName: String)

@Serializable
data class UserDto(val id: String, val email: String, val displayName: String, val biometricEnabled: Boolean, val createdAt: String)

@Serializable
data class AuthResponse(val token: String, val refreshToken: String, val user: UserDto)

@Serializable
data class SessionResponse(val sessions: List<String>)

@Serializable
data class GoogleAuthRequest(val idToken: String)

fun Route.authRoutes() {
    route("/auth") {
        post("/google") {
            val req = call.receive<GoogleAuthRequest>()
            if (req.idToken.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Google Credentials Token")
                return@post
            }

            val isMock = req.idToken.startsWith("mock_google_token_")
            val email = if (isMock) {
                req.idToken.removePrefix("mock_google_token_") + "@gmail.com"
            } else {
                "googleuser@gmail.com"
            }
            val displayName = if (isMock) {
                req.idToken.removePrefix("mock_google_token_").replaceFirstChar { it.uppercase() }
            } else {
                "Google User"
            }

            var userRow = transaction {
                Users.select { Users.email eq email }.singleOrNull()
            }

            if (userRow == null) {
                val newUserId = UUID.randomUUID()
                transaction {
                    Users.insert {
                        it[id] = newUserId
                        it[Users.email] = email
                        it[passwordHash] = "google_authenticated_oauth2"
                        it[Users.displayName] = displayName
                        it[biometricEnabled] = true
                    }
                }
                userRow = transaction {
                    Users.select { Users.email eq email }.single()
                }
            }

            val userId = userRow[Users.id]
            val token = JwtProvider.generateAccessToken(userId.toString())
            val refreshToken = UUID.randomUUID().toString()
            val expiry = LocalDateTime.now().plusDays(7)

            transaction {
                ActiveSessions.insert {
                    it[id] = UUID.randomUUID()
                    it[ActiveSessions.userId] = userId
                    it[refreshTokenHash] = Argon2Hasher.hash(refreshToken)
                    it[deviceInfo] = "Google Sign-In Session"
                    it[expiresAt] = expiry
                }
            }

            call.respond(
                AuthResponse(
                    token = token,
                    refreshToken = refreshToken,
                    user = UserDto(
                        id = userId.toString(),
                        email = userRow[Users.email],
                        displayName = userRow[Users.displayName],
                        biometricEnabled = userRow[Users.biometricEnabled],
                        createdAt = userRow[Users.createdAt].toString()
                    )
                )
            )
        }

        post("/signup") {
            val req = call.receive<SignupRequest>()
            val existing = transaction {
                Users.select { Users.email eq req.email }.singleOrNull()
            }
            if (existing != null) {
                call.respond(HttpStatusCode.BadRequest, "Email already registered")
                return@post
            }

            val userId = UUID.randomUUID()
            val passHash = Argon2Hasher.hash(req.password)
            val now = LocalDateTime.now()

            transaction {
                Users.insert {
                    it[id] = userId
                    it[email] = req.email
                    it[passwordHash] = passHash
                    it[displayName] = req.displayName
                }
            }

            val accessToken = JwtProvider.generateAccessToken(userId.toString())
            val refreshToken = JwtProvider.generateRefreshToken()

            transaction {
                ActiveSessions.insert {
                    it[id] = UUID.randomUUID()
                    it[Users.id] = userId
                    it[refreshTokenHash] = Argon2Hasher.hash(refreshToken)
                    it[deviceInfo] = "Emulator (Pixel 7)"
                    it[expiresAt] = LocalDateTime.now().plusDays(30)
                }
            }

            call.respond(
                AuthResponse(
                    token = accessToken,
                    refreshToken = refreshToken,
                    user = UserDto(userId.toString(), req.email, req.displayName, false, now.toString())
                )
            )
        }

        post("/login") {
            val req = call.receive<AuthRequest>()
            val user = transaction {
                Users.select { Users.email eq req.email }.singleOrNull()
            }
            if (user == null || !Argon2Hasher.verify(user[Users.passwordHash], req.password)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val userId = user[Users.id]
            val accessToken = JwtProvider.generateAccessToken(userId.toString())
            val refreshToken = JwtProvider.generateRefreshToken()

            transaction {
                ActiveSessions.insert {
                    it[id] = UUID.randomUUID()
                    it[Users.id] = userId
                    it[refreshTokenHash] = Argon2Hasher.hash(refreshToken)
                    it[deviceInfo] = "Emulator (Pixel 7)"
                    it[expiresAt] = LocalDateTime.now().plusDays(30)
                }
            }

            call.respond(
                AuthResponse(
                    token = accessToken,
                    refreshToken = refreshToken,
                    user = UserDto(
                        id = userId.toString(),
                        email = user[Users.email],
                        displayName = user[Users.displayName],
                        biometricEnabled = user[Users.biometricEnabled],
                        createdAt = user[Users.createdAt].toString()
                    )
                )
            )
        }

        authenticate("jwt") {
            post("/logout") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                if (userIdStr.isNotEmpty()) {
                    val userId = UUID.fromString(userIdStr)
                    transaction {
                        ActiveSessions.deleteWhere { ActiveSessions.userId eq userId }
                    }
                }
                call.respond(HttpStatusCode.OK)
            }

            put("/biometrics") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                val enabled = call.parameters["enabled"]?.toBoolean() ?: false
                if (userIdStr.isNotEmpty()) {
                    val userId = UUID.fromString(userIdStr)
                    transaction {
                        Users.update({ Users.id eq userId }) {
                            it[biometricEnabled] = enabled
                        }
                    }
                }
                call.respond(HttpStatusCode.OK)
            }

            get("/sessions") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                val sessions = if (userIdStr.isNotEmpty()) {
                    val userId = UUID.fromString(userIdStr)
                    transaction {
                        ActiveSessions.select { ActiveSessions.userId eq userId }
                            .map { "Device: ${it[ActiveSessions.deviceInfo] ?: "Unknown"} - Session ID: ${it[ActiveSessions.id].toString().take(8)}" }
                    }
                } else emptyList()
                call.respond(SessionResponse(sessions))
            }

            delete("/sessions/{id}") {
                val idParam = call.parameters["id"] ?: ""
                if (idParam.isNotEmpty()) {
                    transaction {
                        ActiveSessions.deleteWhere { ActiveSessions.deviceInfo like "%$idParam%" }
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
