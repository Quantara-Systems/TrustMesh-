package com.trustmesh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustmesh.domain.model.Agent
import com.trustmesh.domain.model.AgentStatus
import com.trustmesh.domain.model.Transaction
import com.trustmesh.domain.repository.AgentRepository
import com.trustmesh.domain.repository.LedgerRepository
import com.trustmesh.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository
) : ViewModel() {

    val agents: StateFlow<List<Agent>> = agentRepository.getAgents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = transactionRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExposure: StateFlow<Double> = agents.map { list ->
        list.filter { it.status == AgentStatus.ACTIVE }.sumOf { it.spendEnvelope.amountLimit }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val anomalyAlert: StateFlow<String?> = ledgerRepository.getLedgerEntries().map { list ->
        list.firstOrNull { it.outcome.contains("DRIFT", ignoreCase = true) }?.let {
            "Anomaly: Agent action drifted from intent statement!"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun syncDashboardData() {
        viewModelScope.launch {
            _loading.value = true
            agentRepository.syncAgents()
            transactionRepository.syncTransactions()
            ledgerRepository.syncLedger()
            _loading.value = false
        }
    }
}
