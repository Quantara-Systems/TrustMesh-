package com.trustmesh.ledger

import com.trustmesh.db.LedgerEntries
import com.trustmesh.db.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@Serializable
data class LedgerEntryDto(
    val id: String,
    val agentId: String,
    val timestamp: String,
    val statedIntentSnapshot: String,
    val actionTaken: String,
    val outcome: String,
    val hash: String,
    val previousHash: String
)

fun Route.ledgerRoutes() {
    route("/ledger") {
        authenticate("jwt") {
            get {
                val list = transaction {
                    LedgerEntries.selectAll()
                        .orderBy(LedgerEntries.timestamp to SortOrder.DESC)
                        .map {
                            LedgerEntryDto(
                                id = it[LedgerEntries.id].toString(),
                                agentId = it[LedgerEntries.agentId].toString(),
                                timestamp = it[LedgerEntries.timestamp].toString(),
                                statedIntentSnapshot = it[LedgerEntries.statedIntentSnapshot],
                                actionTaken = it[LedgerEntries.actionTaken],
                                outcome = it[LedgerEntries.outcome],
                                hash = it[LedgerEntries.entryHash],
                                previousHash = it[LedgerEntries.previousHash]
                            )
                        }
                }
                call.respond(list)
            }

            get("/verify") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                if (userIdStr.isEmpty()) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val isAdmin = transaction {
                    val userId = UUID.fromString(userIdStr)
                    val user = Users.select { Users.id eq userId }.singleOrNull()
                    user?.get(Users.email) == "test@trustmesh.in" // Seeded default system admin
                }

                if (!isAdmin) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("isValid" to false, "message" to "Access denied: administrator credentials required"))
                    return@get
                }

                try {
                    val (isValid, message) = LedgerService.verifyChain()
                    call.respond(mapOf("isValid" to isValid, "message" to message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("isValid" to false, "message" to "Database validation failed: ${e.message}")
                    )
                }
            }
        }
    }
}
