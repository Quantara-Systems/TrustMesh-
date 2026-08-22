package com.trustmesh.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtProvider {
    private val secret = System.getenv("JWT_SECRET") ?: "super-secure-jwt-secret-key-12345678"
    private val issuer = System.getenv("JWT_ISSUER") ?: "http://localhost:8080/"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "http://localhost:8080/"
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateAccessToken(userId: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 900_000)) // 15 minutes validity
            .sign(algorithm)
    }

    fun generateRefreshToken(): String {
        return UUID.randomUUID().toString()
    }
}
