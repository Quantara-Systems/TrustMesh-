package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.data.remote.*
import com.trustmesh.domain.model.*
import com.trustmesh.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val api: TrustMeshApi
) : AccountRepository {

    override fun getLinkedAccounts(): Flow<List<LinkedAccount>> {
        return accountDao.getLinkedAccounts().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun generatePlaidLinkToken(): Result<String> {
        return try {
            val response = api.createPlaidLinkToken()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.linkToken)
            } else {
                Result.failure(Exception("Failed to generate Plaid Link Token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exchangePlaidPublicToken(publicToken: String): Result<Unit> {
        return try {
            val response = api.exchangePlaidPublicToken(PlaidExchangeRequest(publicToken))
            if (response.isSuccessful) {
                syncAccounts()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to exchange Plaid public token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAccounts(): Result<Unit> {
        return try {
            val response = api.getLinkedAccounts()
            if (response.isSuccessful && response.body() != null) {
                accountDao.insertLinkedAccounts(response.body()!!)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to sync accounts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
