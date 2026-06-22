package org.example.project.navigation

import androidx.compose.runtime.*
import org.example.project.model.LoginResponse

// Navigator holds the two pieces of state that drive the entire website:
//   currentRoute -> What screen is visible
//   currentUser  -> Who is logged in
class Navigator {

    // Only navigator can change this
    var currentRoute by mutableStateOf<Route>(Route.Home)
        private set

    // Only navigator can change this
    var currentUser by mutableStateOf<LoginResponse?>(null)
        private set

    val isAuthenticated: Boolean get() = currentUser != null

    // Redirects to login if you are not authenticated
    fun navigate(route: Route) {
        if (route.isAdminRoute() && !isAuthenticated) {
            currentRoute = Route.Login
            return
        }
        currentRoute = route
    }

    // Succesful login, stores the user and reroutes to the dashboard
    fun login(user: LoginResponse) {
        currentUser = user
        currentRoute = Route.AdminDashboard
    }

    // Logout user, clears the user and back the landingpage
    fun logout() {
        currentUser = null
        currentRoute = Route.Home
    }
}
