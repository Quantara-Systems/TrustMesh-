package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.data.remote.*
import com.trustmesh.domain.model.*
import com.trustmesh.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val escrowDao: EscrowDao,
    private val api: TrustMeshApi,
    private val okHttpClient: OkHttpClient
) : TransactionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null

    init {
        connectWebSocket()
    }

    private fun connectWebSocket() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8080/api/v1/transactions/ws/live")
            .build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    syncTransactions()
                    syncEscrowItems()
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Wait and attempt reconnection
                scope.launch {
                    kotlinx.coroutines.delay(5000)
                    connectWebSocket()
                }
            }
        })
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getTransactions().map { list -> list.map { it.toDomain() } }
    }

    override fun getTransactionsForAgent(agentId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForAgent(agentId).map { list -> list.map { it.toDomain() } }
    }

    override fun getEscrowItems(): Flow<List<EscrowItem>> {
        return escrowDao.getEscrowItems().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun approveEscrow(escrowId: String): Result<Unit> {
        return try {
            val response = api.resolveEscrow(escrowId, EscrowActionRequest("APPROVE"))
            if (response.isSuccessful) {
                syncEscrowItems()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to approve escrow"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun denyEscrow(escrowId: String): Result<Unit> {
        return try {
            val response = api.resolveEscrow(escrowId, EscrowActionRequest("DENY"))
            if (response.isSuccessful) {
                syncEscrowItems()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to deny escrow"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(): Result<Unit> {
        return try {
            val response = api.getTransactions()
            if (response.isSuccessful && response.body() != null) {
                transactionDao.insertTransactions(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync transactions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncEscrowItems(): Result<Unit> {
        return try {
            val response = api.getEscrowItems()
            if (response.isSuccessful && response.body() != null) {
                escrowDao.insertEscrowItems(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync escrow items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
