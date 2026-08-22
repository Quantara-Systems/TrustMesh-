package com.trustmesh.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        AgentEntity::class,
        TransactionEntity::class,
        EscrowItemEntity::class,
        LedgerEntryEntity::class,
        LinkedAccountEntity::class,
        MerchantEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrustMeshDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun agentDao(): AgentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun escrowDao(): EscrowDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun accountDao(): AccountDao
    abstract fun merchantDao(): MerchantDao
}
