package com.trustmesh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.trustmesh.domain.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val biometricEnabled: Boolean,
    val createdAt: String
) {
    fun toDomain() = User(id, email, displayName, biometricEnabled, createdAt)
    companion object {
        fun fromDomain(user: User) = UserEntity(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            biometricEnabled = user.biometricEnabled,
            createdAt = user.createdAt
        )
    }
}

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val intentStatement: String,
    val categoryScope: List<Category>,
    val limitAmount: Double,
    val windowType: WindowType,
    val currentUtilization: Double,
    val escalationRules: List<EscalationRule>,
    val status: AgentStatus,
    val createdAt: String
) {
    fun toDomain() = Agent(
        id = id,
        ownerId = ownerId,
        name = name,
        intentStatement = intentStatement,
        categoryScope = categoryScope,
        spendEnvelope = SpendEnvelope(limitAmount, windowType, currentUtilization),
        escalationRules = escalationRules,
        status = status,
        createdAt = createdAt
    )
    companion object {
        fun fromDomain(agent: Agent) = AgentEntity(
            id = agent.id,
            ownerId = agent.ownerId,
            name = agent.name,
            intentStatement = agent.intentStatement,
            categoryScope = agent.categoryScope,
            limitAmount = agent.spendEnvelope.amountLimit,
            windowType = agent.spendEnvelope.windowType,
            currentUtilization = agent.spendEnvelope.currentUtilization,
            escalationRules = agent.escalationRules,
            status = agent.status,
            createdAt = agent.createdAt
        )
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val agentId: String,
    val merchantName: String,
    val merchantCategory: Category,
    val amount: Double,
    val status: TransactionStatus,
    val negotiationDetail: String,
    val createdAt: String
) {
    fun toDomain() = Transaction(
        id = id,
        agentId = agentId,
        merchantName = merchantName,
        merchantCategory = merchantCategory,
        amount = amount,
        status = status,
        negotiationDetail = negotiationDetail,
        createdAt = createdAt
    )
    companion object {
        fun fromDomain(t: Transaction) = TransactionEntity(
            id = t.id,
            agentId = t.agentId,
            merchantName = t.merchantName,
            merchantCategory = t.merchantCategory,
            amount = t.amount,
            status = t.status,
            negotiationDetail = t.negotiationDetail,
            createdAt = t.createdAt
        )
    }
}

@Entity(tableName = "escrow_items")
data class EscrowItemEntity(
    @PrimaryKey val id: String,
    val transactionId: String,
    val state: EscrowState,
    val conditionType: EscrowConditionType,
    val conditionThreshold: Double,
    val createdAt: String,
    val resolvedAt: String?
) {
    fun toDomain() = EscrowItem(
        id = id,
        transactionId = transactionId,
        state = state,
        conditionType = conditionType,
        conditionThreshold = conditionThreshold,
        createdAt = createdAt,
        resolvedAt = resolvedAt
    )
    companion object {
        fun fromDomain(e: EscrowItem) = EscrowItemEntity(
            id = e.id,
            transactionId = e.transactionId,
            state = e.state,
            conditionType = e.conditionType,
            conditionThreshold = e.conditionThreshold,
            createdAt = e.createdAt,
            resolvedAt = e.resolvedAt
        )
    }
}

@Entity(tableName = "ledger_entries")
data class LedgerEntryEntity(
    @PrimaryKey val id: String,
    val agentId: String,
    val timestamp: String,
    val statedIntentSnapshot: String,
    val actionTaken: String,
    val outcome: String,
    val hash: String,
    val previousHash: String
) {
    fun toDomain() = LedgerEntry(
        id = id,
        agentId = agentId,
        timestamp = timestamp,
        statedIntentSnapshot = statedIntentSnapshot,
        actionTaken = actionTaken,
        outcome = outcome,
        hash = hash,
        previousHash = previousHash
    )
    companion object {
        fun fromDomain(l: LedgerEntry) = LedgerEntryEntity(
            id = l.id,
            agentId = l.agentId,
            timestamp = l.timestamp,
            statedIntentSnapshot = l.statedIntentSnapshot,
            actionTaken = l.actionTaken,
            outcome = l.outcome,
            hash = l.hash,
            previousHash = l.previousHash
        )
    }
}

@Entity(tableName = "linked_accounts")
data class LinkedAccountEntity(
    @PrimaryKey val id: String,
    val plaidAccountId: String,
    val institutionName: String,
    val currentBalance: Double,
    val availableBalance: Double,
    val lastSyncedAt: String
) {
    fun toDomain() = LinkedAccount(
        id = id,
        plaidAccountId = plaidAccountId,
        institutionName = institutionName,
        currentBalance = currentBalance,
        availableBalance = availableBalance,
        lastSyncedAt = lastSyncedAt
    )
    companion object {
        fun fromDomain(a: LinkedAccount) = LinkedAccountEntity(
            id = a.id,
            plaidAccountId = a.plaidAccountId,
            institutionName = a.institutionName,
            currentBalance = a.currentBalance,
            availableBalance = a.availableBalance,
            lastSyncedAt = a.lastSyncedAt
        )
    }
}

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: Category,
    val externalReputationScore: Double,
    val internalTrustScore: Double
) {
    fun toDomain() = Merchant(
        id = id,
        name = name,
        category = category,
        externalReputationScore = externalReputationScore,
        internalTrustScore = internalTrustScore
    )
    companion object {
        fun fromDomain(m: Merchant) = MerchantEntity(
            id = m.id,
            name = m.name,
            category = m.category,
            externalReputationScore = m.externalReputationScore,
            internalTrustScore = m.internalTrustScore
        )
    }
}
