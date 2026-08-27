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
        val now = LocalDateTime.now().withNano(0)

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

    fun verifyChain(): Pair<Boolean, String> {
        return transaction {
            val entries = LedgerEntries.selectAll()
                .orderBy(LedgerEntries.timestamp to SortOrder.ASC)
                .map {
                    LedgerEntryData(
                        id = it[LedgerEntries.id],
                        agentId = it[LedgerEntries.agentId],
                        timestamp = it[LedgerEntries.timestamp],
                        statedIntentSnapshot = it[LedgerEntries.statedIntentSnapshot],
                        actionTaken = it[LedgerEntries.actionTaken],
                        outcome = it[LedgerEntries.outcome],
                        hash = it[LedgerEntries.entryHash],
                        previousHash = it[LedgerEntries.previousHash]
                    )
                }

            if (entries.isEmpty()) return@transaction true to "Ledger is empty"

            var prevHash = "GENESIS"
            for (entry in entries) {
                if (entry.previousHash != prevHash) {
                    return@transaction false to "Hash mismatch: expected previous hash '$prevHash' but got '${entry.previousHash}' in block ${entry.id}"
                }

                val contentString = "${entry.id}|${entry.agentId}|${entry.timestamp}|${entry.statedIntentSnapshot}|${entry.actionTaken}|${entry.outcome}|${entry.previousHash}"
                val computedHash = sha256(contentString)
                if (entry.hash != computedHash) {
                    return@transaction false to "Data corruption: computed hash '$computedHash' does not match stored hash '${entry.hash}' in block ${entry.id}"
                }
                prevHash = entry.hash
            }

            true to "Ledger cryptographic integrity verified successfully"
        }
    }

    private data class LedgerEntryData(
        val id: UUID,
        val agentId: UUID,
        val timestamp: LocalDateTime,
        val statedIntentSnapshot: String,
        val actionTaken: String,
        val outcome: String,
        val hash: String,
        val previousHash: String
    )

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
