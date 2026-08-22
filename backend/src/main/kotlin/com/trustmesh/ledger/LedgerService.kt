package com.trustmesh.ledger

import com.trustmesh.db.LedgerEntries
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

object LedgerService {

    fun appendEntry(agentId: UUID, intent: String, action: String, outcome: String): UUID {
        val entryId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val prevHash = transaction {
            val last = LedgerEntries.selectAll()
                .orderBy(LedgerEntries.timestamp to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
            last?.get(LedgerEntries.entryHash) ?: "GENESIS"
        }

        // Formula defined in Section 5.4 for client-side auditing consistency
        val contentString = "$entryId|$agentId|$now|$intent|$action|$outcome|$prevHash"
        val hash = sha256(contentString)

        transaction {
            LedgerEntries.insert {
                it[id] = entryId
                it[LedgerEntries.agentId] = agentId
                it[timestamp] = now
                it[statedIntentSnapshot] = intent
                it[actionTaken] = action
                it[LedgerEntries.outcome] = outcome
                it[entryHash] = hash
                it[previousHash] = prevHash
            }
        }
        return entryId
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
