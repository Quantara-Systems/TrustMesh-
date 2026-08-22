package com.trustmesh.data.repository

import com.trustmesh.data.local.*
import com.trustmesh.data.remote.TrustMeshApi
import com.trustmesh.domain.model.Merchant
import com.trustmesh.domain.repository.MerchantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantRepositoryImpl @Inject constructor(
    private val merchantDao: MerchantDao,
    private val api: TrustMeshApi
) : MerchantRepository {

    override fun getMerchants(): Flow<List<Merchant>> {
        return merchantDao.getMerchants().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun searchMerchants(query: String): Result<List<Merchant>> {
        return try {
            val response = api.searchMerchants(query)
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!
                merchantDao.insertMerchants(entities)
                Result.success(entities.map { it.toDomain() })
            } else {
                Result.failure(Exception("Merchant search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
