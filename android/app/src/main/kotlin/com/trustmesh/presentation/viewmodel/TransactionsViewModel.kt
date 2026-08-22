package com.trustmesh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustmesh.domain.model.EscrowItem
import com.trustmesh.domain.model.Transaction
import com.trustmesh.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = transactionRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val escrowItems: StateFlow<List<EscrowItem>> = transactionRepository.getEscrowItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun syncData() {
        viewModelScope.launch {
            _loading.value = true
            transactionRepository.syncTransactions()
            transactionRepository.syncEscrowItems()
            _loading.value = false
        }
    }

    fun approveEscrow(escrowId: String) {
        viewModelScope.launch {
            _loading.value = true
            transactionRepository.approveEscrow(escrowId)
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun denyEscrow(escrowId: String) {
        viewModelScope.launch {
            _loading.value = true
            transactionRepository.denyEscrow(escrowId)
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
