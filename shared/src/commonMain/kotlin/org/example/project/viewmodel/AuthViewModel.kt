package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

// Snapshot of the current login state
data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: LoginResponse? = null,
    val isAuthenticated: Boolean = false
)

// Handles all login/logout logic.
class AuthViewModel(private val api: SportClubApiService) {

    // mutableStateOf makes Compose automatically redraw any screen that reads vm.state
    var state by mutableStateOf(AuthState())
        private set // screens can read but not write state directly

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Called by LoginScreen when the user taps Sign In.
    // requiredRole
    fun login(email: String, password: String, requiredRole: UserRole? = null) {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                // POST /auth/login
                val response = api.login(LoginRequest(email, password))
                if (requiredRole != null && response.role != requiredRole) {
                    api.logout()
                    state = AuthState(error = "Only staff members can log in to this website.")
                } else {
                    state = state.copy(isLoading = false, currentUser = response, isAuthenticated = true)
                }
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message ?: "Login failed")
            }
        }
    }

    // Called on startup when a session is restored from localStorage this is faster
    fun restoreSession(user: LoginResponse) {
        state = AuthState(currentUser = user, isAuthenticated = true)
    }

    // Resets state to empty (logged-out) and clears the token in RealApiService.
    fun logout() {
        api.logout()
        state = AuthState()
    }

    // When the user dismisses the error
    fun clearError() { state = state.copy(error = null) }
}
