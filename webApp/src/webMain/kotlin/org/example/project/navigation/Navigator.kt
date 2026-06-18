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
        // Blokkeer navigatie naar admin-pagina's als er niemand is ingelogd
        if (route.isAdminRoute() && !isAuthenticated) {
            currentRoute = Route.Login
            return
        }
        currentRoute = route
    }

    fun login(user: LoginResponse) {
        currentUser = user
        currentRoute = Route.AdminDashboard
    }

    fun logout() {
        currentUser = null
        currentRoute = Route.Home
    }
}
