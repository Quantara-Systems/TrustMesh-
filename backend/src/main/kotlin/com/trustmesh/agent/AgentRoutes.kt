package com.trustmesh.agent

import com.trustmesh.db.Agents
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*

@Serializable
data class EscalationRuleDto(val type: String, val threshold: Double, val description: String)

@Serializable
data class CreateAgentRequest(
    val name: String,
    val intentStatement: String,
    val categoryScope: List<String>,
    val limitAmount: Double,
    val windowType: String,
    val escalationRules: List<EscalationRuleDto>
)

@Serializable
data class UpdateEnvelopeRequest(val limitAmount: Double, val windowType: String)

@Serializable
data class StatusRequest(val status: String)

@Serializable
data class AgentDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val intentStatement: String,
    val categoryScope: List<String>,
    val limitAmount: Double,
    val windowType: String,
    val currentUtilization: Double,
    val escalationRules: List<EscalationRuleDto>,
    val status: String,
    val createdAt: String
)

fun Route.agentRoutes() {
    route("/agents") {
        authenticate("jwt") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                val list = if (userIdStr.isNotEmpty()) {
                    val userId = UUID.fromString(userIdStr)
                    transaction {
                        Agents.select { Agents.ownerId eq userId }
                            .map {
                                val categories = it[Agents.categoryScope].split(",").filter { c -> c.isNotEmpty() }
                                val rules = try {
                                    kotlinx.serialization.json.Json.decodeFromString<List<EscalationRuleDto>>(it[Agents.escalationRules])
                                } catch (e: Exception) {
                                    emptyList()
                                }
                                AgentDto(
                                    id = it[Agents.id].toString(),
                                    ownerId = it[Agents.ownerId].toString(),
                                    name = it[Agents.name],
                                    intentStatement = it[Agents.intentStatement],
                                    categoryScope = categories,
                                    limitAmount = it[Agents.spendEnvelopeLimit].toDouble(),
                                    windowType = it[Agents.spendEnvelopeWindow],
                                    currentUtilization = it[Agents.currentUtilization].toDouble(),
                                    escalationRules = rules,
                                    status = it[Agents.status],
                                    createdAt = it[Agents.createdAt].toString()
                                )
                            }
                    }
                } else emptyList()
                call.respond(list)
            }

            post {
                val req = call.receive<CreateAgentRequest>()
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.subject ?: ""
                
                if (userIdStr.isNotEmpty()) {
                    val userId = UUID.fromString(userIdStr)
                    val agentId = UUID.randomUUID()
                    val categoriesStr = req.categoryScope.joinToString(",")
                    val rulesJson = kotlinx.serialization.json.Json.encodeToString(req.escalationRules)

                    transaction {
                        Agents.insert {
                            it[id] = agentId
                            it[ownerId] = userId
                            it[name] = req.name
                            it[intentStatement] = req.intentStatement
                            it[categoryScope] = categoriesStr
                            it[spendEnvelopeLimit] = BigDecimal.valueOf(req.limitAmount)
                            it[spendEnvelopeWindow] = req.windowType
                            it[currentUtilization] = BigDecimal.ZERO
                            it[escalationRules] = rulesJson
                            it[status] = "ACTIVE"
                        }
                    }

                    call.respond(
                        AgentDto(
                            id = agentId.toString(),
                            ownerId = userId.toString(),
                            name = req.name,
                            intentStatement = req.intentStatement,
                            categoryScope = req.categoryScope,
                            limitAmount = req.limitAmount,
                            windowType = req.windowType,
                            currentUtilization = 0.0,
                            escalationRules = req.escalationRules,
                            status = "ACTIVE",
                            createdAt = LocalDateTime.now().toString()
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            put("/{id}/envelope") {
                val agentId = UUID.fromString(call.parameters["id"] ?: "")
                val req = call.receive<UpdateEnvelopeRequest>()

                val updatedAgent = transaction {
                    Agents.update({ Agents.id eq agentId }) {
                        it[spendEnvelopeLimit] = BigDecimal.valueOf(req.limitAmount)
                        it[spendEnvelopeWindow] = req.windowType
                    }
                    Agents.select { Agents.id eq agentId }.singleOrNull()
                }

                if (updatedAgent == null) {
                    call.respond(HttpStatusCode.NotFound, "Agent profile not found")
                    return@put
                }

                val categories = updatedAgent[Agents.categoryScope].split(",").filter { c -> c.isNotEmpty() }
                val rules = try {
                    kotlinx.serialization.json.Json.decodeFromString<List<EscalationRuleDto>>(updatedAgent[Agents.escalationRules])
                } catch (e: Exception) {
                    emptyList()
                }

                call.respond(
                    AgentDto(
                        id = updatedAgent[Agents.id].toString(),
                        ownerId = updatedAgent[Agents.ownerId].toString(),
                        name = updatedAgent[Agents.name],
                        intentStatement = updatedAgent[Agents.intentStatement],
                        categoryScope = categories,
                        limitAmount = updatedAgent[Agents.spendEnvelopeLimit].toDouble(),
                        windowType = updatedAgent[Agents.spendEnvelopeWindow],
                        currentUtilization = updatedAgent[Agents.currentUtilization].toDouble(),
                        escalationRules = rules,
                        status = updatedAgent[Agents.status],
                        createdAt = updatedAgent[Agents.createdAt].toString()
                    )
                )
            }

            put("/{id}/status") {
                val agentId = UUID.fromString(call.parameters["id"] ?: "")
                val req = call.receive<StatusRequest>()

                val updatedAgent = transaction {
                    Agents.update({ Agents.id eq agentId }) {
                        it[status] = req.status
                    }
                    Agents.select { Agents.id eq agentId }.singleOrNull()
                }

                if (updatedAgent == null) {
                    call.respond(HttpStatusCode.NotFound, "Agent profile not found")
                    return@put
                }

                val categories = updatedAgent[Agents.categoryScope].split(",").filter { c -> c.isNotEmpty() }
                val rules = try {
                    kotlinx.serialization.json.Json.decodeFromString<List<EscalationRuleDto>>(updatedAgent[Agents.escalationRules])
                } catch (e: Exception) {
                    emptyList()
                }

                call.respond(
                    AgentDto(
                        id = updatedAgent[Agents.id].toString(),
                        ownerId = updatedAgent[Agents.ownerId].toString(),
                        name = updatedAgent[Agents.name],
                        intentStatement = updatedAgent[Agents.intentStatement],
                        categoryScope = categories,
                        limitAmount = updatedAgent[Agents.spendEnvelopeLimit].toDouble(),
                        windowType = updatedAgent[Agents.spendEnvelopeWindow],
                        currentUtilization = updatedAgent[Agents.currentUtilization].toDouble(),
                        escalationRules = rules,
                        status = updatedAgent[Agents.status],
                        createdAt = updatedAgent[Agents.createdAt].toString()
                    )
                )
            }
        }
    }
}
