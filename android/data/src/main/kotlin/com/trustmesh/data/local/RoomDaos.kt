package com.trustmesh.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents")
    fun getAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id")
    fun getAgentById(id: String): Flow<AgentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteAgent(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE agentId = :agentId ORDER BY createdAt DESC")
    fun getTransactionsForAgent(agentId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface EscrowDao {
    @Query("SELECT * FROM escrow_items ORDER BY createdAt DESC")
    fun getEscrowItems(): Flow<List<EscrowItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscrowItems(items: List<EscrowItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscrowItem(item: EscrowItemEntity)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getLedgerEntries(): Flow<List<LedgerEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntryEntity>)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM linked_accounts")
    fun getLinkedAccounts(): Flow<List<LinkedAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedAccounts(accounts: List<LinkedAccountEntity>)
}

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants")
    fun getMerchants(): Flow<List<MerchantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchants(merchants: List<MerchantEntity>)
}
