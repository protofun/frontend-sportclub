package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: LoginResponse? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(AuthState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun login(email: String, password: String) {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val response = api.login(LoginRequest(email, password))
                state = state.copy(isLoading = false, currentUser = response, isAuthenticated = true)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        state = AuthState()
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}
