package org.example.project.navigation

import androidx.compose.runtime.*
import org.example.project.model.LoginResponse

class Navigator {
    var currentRoute by mutableStateOf<Route>(Route.Home)
        private set

    var currentUser by mutableStateOf<LoginResponse?>(null)
        private set

    val isAuthenticated: Boolean get() = currentUser != null

    fun navigate(route: Route) {
        currentRoute = route
    }

    fun login(user: LoginResponse) {
        currentUser = user
        navigate(Route.AdminDashboard)
    }

    fun logout() {
        currentUser = null
        navigate(Route.Home)
    }
}
