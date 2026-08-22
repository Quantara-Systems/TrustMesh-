package com.trustmesh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustmesh.domain.model.LedgerEntry
import com.trustmesh.domain.model.Merchant
import com.trustmesh.domain.repository.LedgerRepository
import com.trustmesh.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val merchantRepository: MerchantRepository
) : ViewModel() {

    val ledgerEntries: StateFlow<List<LedgerEntry>> = ledgerRepository.getLedgerEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChainValid = MutableStateFlow<Boolean?>(null)
    val isChainValid: StateFlow<Boolean?> = _isChainValid.asStateFlow()

    private val _merchants = MutableStateFlow<List<Merchant>>(emptyList())
    val merchants: StateFlow<List<Merchant>> = _merchants.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun syncLedger() {
        viewModelScope.launch {
            _loading.value = true
            ledgerRepository.syncLedger()
            verifyChain()
            _loading.value = false
        }
    }

    fun verifyChain() {
        viewModelScope.launch {
            ledgerRepository.verifyLedgerChainIntegrity()
                .onSuccess { _isChainValid.value = it }
                .onFailure { _isChainValid.value = false }
        }
    }

    fun searchMerchants(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                _merchants.value = emptyList()
                return@launch
            }
            _loading.value = true
            merchantRepository.searchMerchants(query)
                .onSuccess { _merchants.value = it }
                .onFailure { _merchants.value = emptyList() }
            _loading.value = false
        }
    }
}
