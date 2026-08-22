package com.trustmesh.data.di

import android.content.Context
import androidx.room.Room
import com.trustmesh.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TrustMeshDatabase {
        return Room.databaseBuilder(
            context,
            TrustMeshDatabase::class.java,
            "trustmesh_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: TrustMeshDatabase): UserDao = db.userDao()

    @Provides
    fun provideAgentDao(db: TrustMeshDatabase): AgentDao = db.agentDao()

    @Provides
    fun provideTransactionDao(db: TrustMeshDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideEscrowDao(db: TrustMeshDatabase): EscrowDao = db.escrowDao()

    @Provides
    fun provideLedgerDao(db: TrustMeshDatabase): LedgerDao = db.ledgerDao()

    @Provides
    fun provideAccountDao(db: TrustMeshDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideMerchantDao(db: TrustMeshDatabase): MerchantDao = db.merchantDao()
}
