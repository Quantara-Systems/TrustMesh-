package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.data.remote.*
import com.trustmesh.domain.model.*
import com.trustmesh.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepositoryImpl @Inject constructor(
    private val agentDao: AgentDao,
    private val api: TrustMeshApi
) : AgentRepository {

    override fun getAgents(): Flow<List<Agent>> {
        return agentDao.getAgents().map { list -> list.map { it.toDomain() } }
    }

    override fun getAgentById(id: String): Flow<Agent?> {
        return agentDao.getAgentById(id).map { it?.toDomain() }
    }

    override suspend fun createAgent(
        name: String,
        intent: String,
        categories: List<Category>,
        limit: Double,
        window: WindowType,
        rules: List<EscalationRule>
    ): Result<Agent> {
        return try {
            val request = CreateAgentRequest(name, intent, categories, limit, window, rules)
            val response = api.createAgent(request)
            if (response.isSuccessful && response.body() != null) {
                val agentDto = response.body()!!
                val agent = Agent(
                    id = agentDto.id,
                    ownerId = agentDto.ownerId,
                    name = agentDto.name,
                    intentStatement = agentDto.intentStatement,
                    categoryScope = agentDto.categoryScope,
                    spendEnvelope = SpendEnvelope(agentDto.limitAmount, agentDto.windowType, agentDto.currentUtilization),
                    escalationRules = agentDto.escalationRules,
                    status = AgentStatus.valueOf(agentDto.status),
                    createdAt = agentDto.createdAt
                )
                agentDao.insertAgent(AgentEntity.fromDomain(agent))
                Result.success(agent)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Agent creation failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSpendEnvelope(
        agentId: String,
        limit: Double,
        window: WindowType
    ): Result<Agent> {
        return try {
            val request = UpdateEnvelopeRequest(limit, window)
            val response = api.updateEnvelope(agentId, request)
            if (response.isSuccessful && response.body() != null) {
                val agentDto = response.body()!!
                val agent = Agent(
                    id = agentDto.id,
                    ownerId = agentDto.ownerId,
                    name = agentDto.name,
                    intentStatement = agentDto.intentStatement,
                    categoryScope = agentDto.categoryScope,
                    spendEnvelope = SpendEnvelope(agentDto.limitAmount, agentDto.windowType, agentDto.currentUtilization),
                    escalationRules = agentDto.escalationRules,
                    status = AgentStatus.valueOf(agentDto.status),
                    createdAt = agentDto.createdAt
                )
                agentDao.insertAgent(AgentEntity.fromDomain(agent))
                Result.success(agent)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Spend envelope update failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAgentStatus(agentId: String, status: AgentStatus): Result<Agent> {
        return try {
            val request = StatusRequest(status.name)
            val response = api.updateStatus(agentId, request)
            if (response.isSuccessful && response.body() != null) {
                val agentDto = response.body()!!
                val agent = Agent(
                    id = agentDto.id,
                    ownerId = agentDto.ownerId,
                    name = agentDto.name,
                    intentStatement = agentDto.intentStatement,
                    categoryScope = agentDto.categoryScope,
                    spendEnvelope = SpendEnvelope(agentDto.limitAmount, agentDto.windowType, agentDto.currentUtilization),
                    escalationRules = agentDto.escalationRules,
                    status = AgentStatus.valueOf(agentDto.status),
                    createdAt = agentDto.createdAt
                )
                agentDao.insertAgent(AgentEntity.fromDomain(agent))
                Result.success(agent)
            } else {
                Result.failure(Exception(response.message().ifEmpty { "Agent status update failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAgents(): Result<Unit> {
        return try {
            val response = api.getAgents()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { dto ->
                    Agent(
                        id = dto.id,
                        ownerId = dto.ownerId,
                        name = dto.name,
                        intentStatement = dto.intentStatement,
                        categoryScope = dto.categoryScope,
                        spendEnvelope = SpendEnvelope(dto.limitAmount, dto.windowType, dto.currentUtilization),
                        escalationRules = dto.escalationRules,
                        status = AgentStatus.valueOf(dto.status),
                        createdAt = dto.createdAt
                    )
                }
                agentDao.insertAgents(list.map { AgentEntity.fromDomain(it) })
                Result.success(Unit)
            } else {
                Result.failure(Exception("Agents sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
