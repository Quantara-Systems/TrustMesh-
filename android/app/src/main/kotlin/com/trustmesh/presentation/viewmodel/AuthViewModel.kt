package com.trustmesh.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trustmesh.domain.model.User
import com.trustmesh.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authPreferences: com.trustmesh.data.local.AuthPreferences
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(authPreferences.isDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        authPreferences.setDarkTheme(next)
    }

    val user: StateFlow<User?> = authRepository.getSessionUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sessions = MutableStateFlow<List<String>>(emptyList())
    val sessions: StateFlow<List<String>> = _sessions.asStateFlow()

    fun signup(email: String, secret: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            authRepository.signup(email, secret, name)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Registration failed" }
            _loading.value = false
        }
    }

    fun login(email: String, secret: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            authRepository.login(email, secret)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Authentication failed" }
            _loading.value = false
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            authRepository.googleLogin(idToken)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Google Authentication failed" }
            _loading.value = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun setBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.setBiometricEnabled(enabled)
        }
    }

    fun fetchSessions() {
        viewModelScope.launch {
            authRepository.getActiveSessions()
                .onSuccess { _sessions.value = it }
        }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            authRepository.revokeSession(sessionId)
                .onSuccess { fetchSessions() }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
