package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.domain.model.LedgerEntry
import com.trustmesh.domain.repository.LedgerRepository
import com.trustmesh.data.remote.TrustMeshApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerRepositoryImpl @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val api: TrustMeshApi
) : LedgerRepository {

    override fun getLedgerEntries(): Flow<List<LedgerEntry>> {
        return ledgerDao.getLedgerEntries().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun syncLedger(): Result<Unit> {
        return try {
            val response = api.getLedger()
            if (response.isSuccessful && response.body() != null) {
                ledgerDao.insertLedgerEntries(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync ledger"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyLedgerChainIntegrity(): Result<Boolean> {
        return try {
            // Read all entries from Room (ordered by timestamp descending from DB, so we reverse to verify chronologically)
            val entries = getLedgerEntries().firstOrNull() ?: emptyList()
            if (entries.isEmpty()) return Result.success(true)

            val sortedEntries = entries.sortedBy { it.timestamp }
            var isValid = true

            for (i in sortedEntries.indices) {
                val entry = sortedEntries[i]
                val expectedPrevHash = if (i == 0) "GENESIS" else sortedEntries[i - 1].hash
                
                if (entry.previousHash != expectedPrevHash) {
                    isValid = false
                    break
                }

                // Recompute SHA-256 for the current block
                val contentString = "${entry.id}|${entry.agentId}|${entry.timestamp}|${entry.statedIntentSnapshot}|${entry.actionTaken}|${entry.outcome}|${entry.previousHash}"
                val computedHash = sha256(contentString)
                if (entry.hash != computedHash) {
                    isValid = false
                    break
                }
            }
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
