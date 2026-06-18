package org.example.project.member.navigation

import androidx.compose.runtime.*
import org.example.project.model.LoginResponse
import org.example.project.model.UserRole

class MemberNavigator {
    var currentRoute by mutableStateOf<MemberRoute>(MemberRoute.Home)
    var currentUser by mutableStateOf<LoginResponse?>(null)

    val isAuthenticated: Boolean get() = currentUser != null

    fun navigate(route: MemberRoute) { currentRoute = route }
    fun login(user: LoginResponse) {
        currentUser = user
        navigate(if (user.role == UserRole.INSTRUCTOR) MemberRoute.InstructorLessons else MemberRoute.Home)
    }
    fun logout() { currentUser = null }
}
