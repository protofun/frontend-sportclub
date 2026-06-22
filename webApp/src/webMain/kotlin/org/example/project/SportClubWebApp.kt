package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.browser.window
import kotlinx.coroutines.flow.drop
import org.example.project.api.RealApiService
import org.example.project.api.SportClubApiService
import org.example.project.model.LoginResponse
import org.example.project.model.UserRole
import org.example.project.components.TopNavBar
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.screens.*
import org.example.project.screens.admin.*
import org.example.project.theme.SportClubTheme
import org.example.project.viewmodel.*

// The global API
val api: SportClubApiService = RealApiService()

// Keys for browser localStorage — used to persist the session across page refreshes.
private const val KEY_TOKEN     = "sc_token"
private const val KEY_USER_ID   = "sc_user_id"
private const val KEY_ROLE      = "sc_role"
private const val KEY_FULL_NAME = "sc_full_name"
private const val KEY_EMAIL     = "sc_email"

// Saves login in localstorage
private fun saveSession(user: LoginResponse) {
    window.localStorage.setItem(KEY_TOKEN,     user.token)
    window.localStorage.setItem(KEY_USER_ID,   user.userId)
    window.localStorage.setItem(KEY_ROLE,      user.role.name) // stores "STAFF" as a string
    window.localStorage.setItem(KEY_FULL_NAME, user.fullName)
    window.localStorage.setItem(KEY_EMAIL,     user.email)
}

// Removes session data from localStorage on logout.
private fun clearSession() {
    window.localStorage.removeItem(KEY_TOKEN)
    window.localStorage.removeItem(KEY_USER_ID)
    window.localStorage.removeItem(KEY_ROLE)
    window.localStorage.removeItem(KEY_FULL_NAME)
    window.localStorage.removeItem(KEY_EMAIL)
}

// Reads a previously saved session from localStorage.
private fun loadSession(): LoginResponse? {
    val token    = window.localStorage.getItem(KEY_TOKEN)     ?: return null
    val userId   = window.localStorage.getItem(KEY_USER_ID)   ?: return null
    val roleStr  = window.localStorage.getItem(KEY_ROLE)      ?: return null
    val fullName = window.localStorage.getItem(KEY_FULL_NAME) ?: ""
    val email    = window.localStorage.getItem(KEY_EMAIL)     ?: return null
    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { return null }
    return LoginResponse(token, userId, role, fullName.ifBlank { email }, email)
}

// Root composable of the website.
// Owns all ViewModels and the Navigator; renders the correct screen based on the current route.
@Composable
fun SportClubWebApp() {
    val navigator      = remember { Navigator() }
    val authVm         = remember { AuthViewModel(api) }
    val scheduleVm     = remember { ScheduleViewModel(api) }
    val workoutVm      = remember { WorkoutViewModel(api) }
    val instructorVm   = remember { InstructorViewModel(api) }
    val lessonVm       = remember { LessonViewModel(api) }
    val memberVm       = remember { MemberViewModel(api) }
    val registrationVm = remember { RegistrationViewModel(api) }

    // LaunchedEffect(Unit) runs once when this composable first appears.
    LaunchedEffect(Unit) {
    // Gets saved session from the admin
        val saved = loadSession()
        if (saved != null && saved.role == UserRole.STAFF) {
            api.restoreSession(saved.token, saved.userId)
            authVm.restoreSession(saved)
            navigator.login(saved)
        }

        // Every time navigator.currentUser changes → save or clear the session.
        snapshotFlow { navigator.currentUser }
            .drop(1)
            .collect { user ->
                if (user != null) saveSession(user) else { clearSession(); authVm.logout() }
            }
    }

    // Uses the sportclubtheme everywhere
    SportClubTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TopNavBar(navigator, onLogout = { navigator.logout() })

            // Only rerenders when the route matches
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (navigator.currentRoute) {
                    is Route.Home             -> HomeScreen(navigator, scheduleVm)
                    is Route.Schedule         -> ScheduleScreen(navigator, scheduleVm)
                    is Route.Subscriptions    -> SubscriptionsScreen(navigator)
                    is Route.Register         -> RegisterScreen(navigator, registrationVm)
                    is Route.Login            -> LoginScreen(navigator, authVm)
                    is Route.AdminDashboard   -> AdminDashboardScreen(navigator, api, scheduleVm)
                    is Route.AdminWorkouts    -> WorkoutsScreen(workoutVm)
                    is Route.AdminInstructors -> InstructorsScreen(instructorVm)
                    is Route.AdminSchedule    -> LessonManagementScreen(lessonVm)
                    is Route.AdminMembers     -> MembersScreen(memberVm)
                    is Route.AdminOccupancy   -> OccupancyScreen(api)
                    is Route.Downloads        -> DownloadsScreen()
                }
            }
        }
    }
}
