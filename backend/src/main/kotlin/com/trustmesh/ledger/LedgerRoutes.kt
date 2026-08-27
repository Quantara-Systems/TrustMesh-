package com.trustmesh.ledger

import com.trustmesh.db.LedgerEntries
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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
                val (isValid, message) = LedgerService.verifyChain()
                call.respond(
                    mapOf(
                        "isValid" to isValid,
                        "message" to message
                    )
                )
            }
        }
    }
}
