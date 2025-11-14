package com.example.snookerstats.util

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarManager @Inject constructor() {
    val messages: MutableStateFlow<String?> = MutableStateFlow(null)

    fun showMessage(message: String) {
        messages.value = message
    }

    fun clearMessage() {
        messages.value = null
    }
}