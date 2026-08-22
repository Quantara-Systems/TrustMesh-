package com.trustmesh.transaction

import com.trustmesh.db.*
import com.trustmesh.ledger.LedgerService
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
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch

@Serializable
data class TransactionRequest(
    val agentId: String,
    val merchantName: String,
    val merchantCategory: String,
    val amount: Double,
    val negotiationDetail: String
)

@Serializable
data class TransactionDto(
    val id: String,
    val agentId: String,
    val merchantName: String,
    val merchantCategory: String,
    val amount: Double,
    val status: String,
    val negotiationDetail: String,
    val createdAt: String
)

@Serializable
data class EscrowItemDto(
    val id: String,
    val transactionId: String,
    val state: String,
    val conditionType: String,
    val conditionThreshold: Double,
    val createdAt: String,
    val resolvedAt: String?
)

@Serializable
data class EscrowActionRequest(val action: String)

fun Route.transactionRoutes() {
    route("/transactions") {
        authenticate("jwt") {
            get {
                val agentIdParam = call.parameters["agentId"]
                val list = transaction {
                    val query = if (agentIdParam != null) {
                        Transactions.select { Transactions.agentId eq UUID.fromString(agentIdParam) }
                    } else {
                        Transactions.selectAll()
                    }
                    query.orderBy(Transactions.createdAt to SortOrder.DESC)
                        .map {
                            TransactionDto(
                                id = it[Transactions.id].toString(),
                                agentId = it[Transactions.agentId].toString(),
                                merchantName = it[Transactions.merchantName],
                                merchantCategory = it[Transactions.merchantCategory],
                                amount = it[Transactions.amount].toDouble(),
                                status = it[Transactions.status],
                                negotiationDetail = it[Transactions.negotiationDetail],
                                createdAt = it[Transactions.createdAt].toString()
                            )
                        }
                }
                call.respond(list)
            }

            get("/escrow") {
                val list = transaction {
                    EscrowItems.selectAll()
                        .orderBy(EscrowItems.createdAt to SortOrder.DESC)
                        .map {
                            EscrowItemDto(
                                id = it[EscrowItems.id].toString(),
                                transactionId = it[EscrowItems.transactionId].toString(),
                                state = it[EscrowItems.state],
                                conditionType = it[EscrowItems.conditionType],
                                conditionThreshold = it[EscrowItems.conditionThreshold].toDouble(),
                                createdAt = it[EscrowItems.createdAt].toString(),
                                resolvedAt = it[EscrowItems.resolvedAt]?.toString()
                            )
                        }
                }
                call.respond(list)
            }

            post("/escrow/{id}/action") {
                val escrowId = UUID.fromString(call.parameters["id"] ?: "")
                val req = call.receive<EscrowActionRequest>()

                val resolved = transaction {
                    val escrow = EscrowItems.select { EscrowItems.id eq escrowId }.singleOrNull() ?: return@transaction false
                    val txId = escrow[EscrowItems.transactionId]
                    val tx = Transactions.select { Transactions.id eq txId }.singleOrNull() ?: return@transaction false
                    val agentId = tx[Transactions.agentId]
                    val agent = Agents.select { Agents.id eq agentId }.singleOrNull() ?: return@transaction false

                    if (req.action == "APPROVE") {
                        EscrowItems.update({ EscrowItems.id eq escrowId }) {
                            it[state] = "APPROVED"
                            it[resolvedAt] = LocalDateTime.now()
                        }
                        Transactions.update({ Transactions.id eq txId }) {
                            it[status] = "RELEASED"
                        }
                        Agents.update({ Agents.id eq agentId }) {
                            it[currentUtilization] = agent[Agents.currentUtilization] + tx[Transactions.amount]
                        }
                        LedgerService.appendEntry(
                            agentId = agentId,
                            intent = agent[Agents.intentStatement],
                            action = "Approved purchase at ${tx[Transactions.merchantName]} for $${tx[Transactions.amount]} via override",
                            outcome = "RELEASED"
                        )
                        application.launch {
                            TransactionEventBus.post("RELEASED|$txId")
                        }
                    } else {
                        EscrowItems.update({ EscrowItems.id eq escrowId }) {
                            it[state] = "DENIED"
                            it[resolvedAt] = LocalDateTime.now()
                        }
                        Transactions.update({ Transactions.id eq txId }) {
                            it[status] = "CANCELLED"
                        }
                        application.launch {
                            TransactionEventBus.post("CANCELLED|$txId")
                        }
                    }
                    true
                }

                if (resolved) call.respond(HttpStatusCode.OK)
                else call.respond(HttpStatusCode.NotFound, "Escrow record not found")
            }

            post("/request") {
                val req = call.receive<TransactionRequest>()
                val agentId = UUID.fromString(req.agentId)
                val txId = UUID.randomUUID()

                val result = transaction {
                    val agent = Agents.select { Agents.id eq agentId }.singleOrNull()
                        ?: return@transaction "AGENT_NOT_FOUND" to null
                    val status = agent[Agents.status]
                    if (status != "ACTIVE") {
                        return@transaction "AGENT_INACTIVE" to null
                    }

                    val limit = agent[Agents.spendEnvelopeLimit]
                    val utilization = agent[Agents.currentUtilization]
                    val requestedAmt = BigDecimal.valueOf(req.amount)

                    if (utilization + requestedAmt > limit) {
                        Transactions.insert {
                            it[id] = txId
                            it[Transactions.agentId] = agentId
                            it[merchantName] = req.merchantName
                            it[merchantCategory] = req.merchantCategory
                            it[amount] = requestedAmt
                            it[Transactions.status] = "PENDING_CONDITION"
                            it[negotiationDetail] = req.negotiationDetail
                        }
                        
                        val escrowId = UUID.randomUUID()
                        EscrowItems.insert {
                            it[id] = escrowId
                            it[transactionId] = txId
                            it[state] = "PENDING"
                            it[conditionType] = "SPEND_LIMIT_EXCEEDED"
                            it[conditionThreshold] = limit
                        }

                        LedgerService.appendEntry(
                            agentId = agentId,
                            intent = agent[Agents.intentStatement],
                            action = "Attempted purchase at ${req.merchantName} of $${req.amount} exceeded envelope limit",
                            outcome = "DRIFT_DETECTED"
                        )
                        application.launch {
                            TransactionEventBus.post("ESCROW_HOLD|$txId")
                        }
                        "ESCROW_HOLD" to txId
                    } else {
                        Transactions.insert {
                            it[id] = txId
                            it[Transactions.agentId] = agentId
                            it[merchantName] = req.merchantName
                            it[merchantCategory] = req.merchantCategory
                            it[amount] = requestedAmt
                            it[Transactions.status] = "RELEASED"
                            it[negotiationDetail] = req.negotiationDetail
                        }

                        Agents.update({ Agents.id eq agentId }) {
                            it[currentUtilization] = utilization + requestedAmt
                        }

                        LedgerService.appendEntry(
                            agentId = agentId,
                            intent = agent[Agents.intentStatement],
                            action = "Purchased ${req.merchantCategory} items at ${req.merchantName} for $${req.amount}",
                            outcome = "RELEASED"
                        )
                        application.launch {
                            TransactionEventBus.post("RELEASED|$txId")
                        }
                        "RELEASED" to txId
                    }
                }

                when (result.first) {
                    "AGENT_NOT_FOUND" -> call.respond(HttpStatusCode.NotFound, "Agent not found")
                    "AGENT_INACTIVE" -> call.respond(HttpStatusCode.Forbidden, "Agent authority revoked or paused")
                    "ESCROW_HOLD" -> call.respond(HttpStatusCode.Accepted, mapOf("status" to "ESCROW_HOLD", "transactionId" to result.second.toString()))
                    else -> call.respond(HttpStatusCode.Created, mapOf("status" to "RELEASED", "transactionId" to result.second.toString()))
                }
            }

            webSocket("/ws/live") {
                val job = launch {
                    TransactionEventBus.events.collect { event ->
                        send(Frame.Text(event))
                    }
                }
                try {
                    for (frame in incoming) {
                        // keep alive
                    }
                } finally {
                    job.cancel()
                }
            }
        }
    }
}
