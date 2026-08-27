package com.trustmesh.account

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

object PlaidClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    private val clientId = System.getenv("PLAID_CLIENT_ID") ?: "test_client_id"
    private val secret = System.getenv("PLAID_SECRET") ?: "test_secret"
    private const val PLAID_URL = "https://sandbox.plaid.com"

    suspend fun createLinkToken(userId: String): String {
        return try {
            val response = client.post("$PLAID_URL/link/token/create") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("client_id", clientId)
                    put("secret", secret)
                    put("user", buildJsonObject { put("client_user_id", userId.ifEmpty { "trustmesh_user_1" }) })
                    put("client_name", "TrustMesh Wallet")
                    put("products", JsonArray(listOf(JsonPrimitive("auth"), JsonPrimitive("transactions"))))
                    put("country_codes", JsonArray(listOf(JsonPrimitive("US"))))
                    put("language", "en")
                })
            }
            if (response.status == HttpStatusCode.OK) {
                val json = Json.parseToJsonElement(response.bodyAsText())
                json.jsonObject["link_token"]?.jsonPrimitive?.content ?: "mock_link_token_12345"
            } else {
                "mock_link_token_12345"
            }
        } catch (e: Exception) {
            "mock_link_token_12345"
        }
    }

    suspend fun exchangePublicToken(publicToken: String): String {
        return "mock_access_token_12345"
    }

    suspend fun getBalances(accessToken: String): List<PlaidAccountDto> {
        return listOf(
            PlaidAccountDto("acc_1", "Plaid Checking (Sandbox)", 25000.0, 24500.0),
            PlaidAccountDto("acc_2", "Plaid Savings (Sandbox)", 120000.0, 120000.0)
        )
    }
}

@Serializable
data class PlaidAccountDto(val accountId: String, val name: String, val currentBalance: Double, val availableBalance: Double)
