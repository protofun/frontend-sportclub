package org.example.project.member.navigation

import androidx.compose.runtime.*
import org.example.project.model.LoginResponse
import org.example.project.model.UserRole

// tracks which screen the user is currently on and who is logged in
class MemberNavigator {
    // The currently displayed screen
    var currentRoute by mutableStateOf<MemberRoute>(MemberRoute.Home)

    // The logged-in user
    var currentUser by mutableStateOf<LoginResponse?>(null)

    // true if someone is logged in.
    val isAuthenticated: Boolean get() = currentUser != null

    // Switch to a different screen by updating the route.
    fun navigate(route: MemberRoute) { currentRoute = route }

    // Store the logged-in user and redirect to the appropriate starting screen.
    fun login(user: LoginResponse) {
        currentUser = user
        navigate(if (user.role == UserRole.INSTRUCTOR) MemberRoute.InstructorLessons else MemberRoute.Home)
    }

    // clear the logged in user
    fun logout() { currentUser = null }
}
