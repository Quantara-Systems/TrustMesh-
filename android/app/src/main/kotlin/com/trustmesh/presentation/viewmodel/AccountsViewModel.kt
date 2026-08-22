package com.trustmesh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustmesh.domain.model.LinkedAccount
import com.trustmesh.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val accounts: StateFlow<List<LinkedAccount>> = accountRepository.getLinkedAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun syncAccounts() {
        viewModelScope.launch {
            _loading.value = true
            accountRepository.syncAccounts()
            _loading.value = false
        }
    }

    fun startPlaidFlow(onLinkTokenCreated: (String) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            accountRepository.generatePlaidLinkToken()
                .onSuccess { onLinkTokenCreated(it) }
                .onFailure { _error.value = it.message ?: "Failed to generate link token" }
            _loading.value = false
        }
    }

    fun completePlaidFlow(publicToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            accountRepository.exchangePlaidPublicToken(publicToken)
                .onFailure { _error.value = it.message ?: "Token exchange failed" }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
