package com.trustmesh.domain

import com.trustmesh.domain.model.LedgerEntry
import org.junit.Test
import java.security.MessageDigest
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class LedgerIntegrityTest {

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun testValidChainVerification() {
        val agentId = "agent-123"
        
        // Genesis Block
        val genesisId = "genesis-id"
        val genesisPrevHash = "GENESIS"
        val genesisTimestamp = "2026-08-22T00:00:00"
        val genesisIntent = "Buy weekly grocery items"
        val genesisAction = "Bought organic milk"
        val genesisOutcome = "RELEASED"

        val genesisContent = "$genesisId|$agentId|$genesisTimestamp|$genesisIntent|$genesisAction|$genesisOutcome|$genesisPrevHash"
        val genesisHash = sha256(genesisContent)

        val entry1 = LedgerEntry(
            id = genesisId,
            agentId = agentId,
            timestamp = genesisTimestamp,
            statedIntentSnapshot = genesisIntent,
            actionTaken = genesisAction,
            outcome = genesisOutcome,
            hash = genesisHash,
            previousHash = genesisPrevHash
        )

        // Block 2
        val block2Id = "block-2"
        val block2Timestamp = "2026-08-22T00:01:00"
        val block2Action = "Bought whole wheat bread"
        val block2Content = "$block2Id|$agentId|$block2Timestamp|$genesisIntent|$block2Action|$genesisOutcome|$genesisHash"
        val block2Hash = sha256(block2Content)

        val entry2 = LedgerEntry(
            id = block2Id,
            agentId = agentId,
            timestamp = block2Timestamp,
            statedIntentSnapshot = genesisIntent,
            actionTaken = block2Action,
            outcome = genesisOutcome,
            hash = block2Hash,
            previousHash = genesisHash
        )

        val chain = listOf(entry1, entry2)
        val sortedEntries = chain.sortedBy { it.timestamp }
        var isValid = true

        for (i in sortedEntries.indices) {
            val entry = sortedEntries[i]
            val expectedPrevHash = if (i == 0) "GENESIS" else sortedEntries[i - 1].hash
            
            if (entry.previousHash != expectedPrevHash) {
                isValid = false
                break
            }
            
            val contentString = "${entry.id}|${entry.agentId}|${entry.timestamp}|${entry.statedIntentSnapshot}|${entry.actionTaken}|${entry.outcome}|${entry.previousHash}"
            val computedHash = sha256(contentString)
            if (entry.hash != computedHash) {
                isValid = false
                break
            }
        }

        assertTrue(isValid, "Cryptographic ledger hash chain verification should pass successfully.")
    }

    @Test
    fun testCorruptedChainFails() {
        val agentId = "agent-123"

        // Genesis Block
        val genesisId = "genesis-id"
        val genesisPrevHash = "GENESIS"
        val genesisTimestamp = "2026-08-22T00:00:00"
        val genesisIntent = "Buy groceries"
        val genesisAction = "Bought bread"
        val genesisOutcome = "RELEASED"

        val genesisContent = "$genesisId|$agentId|$genesisTimestamp|$genesisIntent|$genesisAction|$genesisOutcome|$genesisPrevHash"
        val genesisHash = sha256(genesisContent)

        val entry1 = LedgerEntry(
            id = genesisId,
            agentId = agentId,
            timestamp = genesisTimestamp,
            statedIntentSnapshot = genesisIntent,
            actionTaken = genesisAction,
            outcome = genesisOutcome,
            hash = genesisHash,
            previousHash = genesisPrevHash
        )

        // Block 2 with mutated action to simulate a database tampering hack
        val block2Id = "block-2"
        val block2Timestamp = "2026-08-22T00:01:00"
        val block2ActionReal = "Bought organic milk"
        val block2ContentReal = "$block2Id|$agentId|$block2Timestamp|$genesisIntent|$block2ActionReal|$genesisOutcome|$genesisHash"
        val block2HashReal = sha256(block2ContentReal)

        val entry2Corrupted = LedgerEntry(
            id = block2Id,
            agentId = agentId,
            timestamp = block2Timestamp,
            statedIntentSnapshot = genesisIntent,
            actionTaken = "Bought a premium $1000 laptop instead of milk!", // TAMPERED!
            outcome = genesisOutcome,
            hash = block2HashReal,
            previousHash = genesisHash
        )

        val chain = listOf(entry1, entry2Corrupted)
        val sortedEntries = chain.sortedBy { it.timestamp }
        var isValid = true

        for (i in sortedEntries.indices) {
            val entry = sortedEntries[i]
            val expectedPrevHash = if (i == 0) "GENESIS" else sortedEntries[i - 1].hash
            
            if (entry.previousHash != expectedPrevHash) {
                isValid = false
                break
            }
            
            val contentString = "${entry.id}|${entry.agentId}|${entry.timestamp}|${entry.statedIntentSnapshot}|${entry.actionTaken}|${entry.outcome}|${entry.previousHash}"
            val computedHash = sha256(contentString)
            if (entry.hash != computedHash) {
                isValid = false
                break
            }
        }

        assertFalse(isValid, "Cryptographic verification check must fail when any ledger entry has been tampered with.")
    }
}
