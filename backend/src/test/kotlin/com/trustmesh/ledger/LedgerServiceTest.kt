package com.trustmesh.ledger

import com.trustmesh.db.LedgerEntries
import com.trustmesh.db.Agents
import com.trustmesh.db.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LedgerServiceTest {

    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.drop(LedgerEntries, Agents, Users)
            SchemaUtils.create(Users, Agents, LedgerEntries)
        }
    }

    @Test
    fun testVerifyChainValid() {
        val userId = UUID.randomUUID()
        val agentId = UUID.randomUUID()

        transaction {
            Users.insert {
                it[id] = userId
                it[email] = "test@domain.com"
                it[passwordHash] = "hashed"
                it[displayName] = "Test User"
            }
            Agents.insert {
                it[id] = agentId
                it[ownerId] = userId
                it[name] = "Agent"
                it[intentStatement] = "Intent"
                it[categoryScope] = "ELECTRONICS"
                it[spendEnvelopeLimit] = java.math.BigDecimal.valueOf(100.0)
                it[spendEnvelopeWindow] = "DAILY"
                it[escalationRules] = "[]"
                it[status] = "ACTIVE"
            }
        }

        val entry1Id = LedgerService.appendEntry(
            agentId = agentId,
            intent = "Intent",
            action = "Bought item 1",
            outcome = "RELEASED"
        )

        val entry2Id = LedgerService.appendEntry(
            agentId = agentId,
            intent = "Intent",
            action = "Bought item 2",
            outcome = "RELEASED"
        )

        val (isValid, message) = LedgerService.verifyChain()
        assertTrue(isValid, "Ledger integrity verification should succeed for valid chain. Message: $message")
    }

    @Test
    fun testVerifyChainTampered() {
        val userId = UUID.randomUUID()
        val agentId = UUID.randomUUID()

        transaction {
            Users.insert {
                it[id] = userId
                it[email] = "test2@domain.com"
                it[passwordHash] = "hashed"
                it[displayName] = "Test User 2"
            }
            Agents.insert {
                it[id] = agentId
                it[ownerId] = userId
                it[name] = "Agent"
                it[intentStatement] = "Intent"
                it[categoryScope] = "ELECTRONICS"
                it[spendEnvelopeLimit] = java.math.BigDecimal.valueOf(100.0)
                it[spendEnvelopeWindow] = "DAILY"
                it[escalationRules] = "[]"
                it[status] = "ACTIVE"
            }
        }

        val entry1Id = LedgerService.appendEntry(
            agentId = agentId,
            intent = "Intent",
            action = "Bought milk",
            outcome = "RELEASED"
        )

        val entry2Id = LedgerService.appendEntry(
            agentId = agentId,
            intent = "Intent",
            action = "Bought bread",
            outcome = "RELEASED"
        )

        // Tamper with block 2 actionTaken in the database
        transaction {
            LedgerEntries.update({ LedgerEntries.id eq entry2Id }) {
                it[actionTaken] = "Bought a premium expensive laptop instead of bread!"
            }
        }

        val (isValid, message) = LedgerService.verifyChain()
        assertFalse(isValid, "Ledger integrity verification must fail for a tampered chain. Message: $message")
        assertTrue(message.contains("Data corruption"), "Message should indicate data corruption: $message")
    }
}
