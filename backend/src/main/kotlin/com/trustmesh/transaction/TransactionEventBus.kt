package com.trustmesh.transaction

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TransactionEventBus {
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    suspend fun post(message: String) {
        _events.emit(message)
    }
}
