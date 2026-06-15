package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.example.project.api.RealApiService
import org.example.project.api.SportClubApiService
import org.example.project.components.TopNavBar
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.screens.*
import org.example.project.screens.admin.*
import org.example.project.theme.SportClubTheme
import org.example.project.viewmodel.*

val api: SportClubApiService = RealApiService()

@Composable
fun SportClubWebApp() {
    val navigator = remember { Navigator() }
    val authVm = remember { AuthViewModel(api) }
    val scheduleVm = remember { ScheduleViewModel(api) }
    val workoutVm = remember { WorkoutViewModel(api) }
    val instructorVm = remember { InstructorViewModel(api) }
    val lessonVm = remember { LessonViewModel(api) }
    val memberVm = remember { MemberViewModel(api) }
    val registrationVm = remember { RegistrationViewModel(api) }

    SportClubTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TopNavBar(navigator)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (navigator.currentRoute) {
                    is Route.Home -> HomeScreen(navigator, scheduleVm)
                    is Route.Schedule -> ScheduleScreen(navigator, scheduleVm)
                    is Route.Subscriptions -> SubscriptionsScreen(navigator)
                    is Route.Register -> RegisterScreen(navigator, registrationVm)
                    is Route.Login -> LoginScreen(navigator, authVm)
                    is Route.AdminDashboard -> AdminDashboardScreen(navigator, api, scheduleVm)
                    is Route.AdminWorkouts -> WorkoutsScreen(workoutVm)
                    is Route.AdminInstructors -> InstructorsScreen(instructorVm)
                    is Route.AdminSchedule -> LessonManagementScreen(lessonVm)
                    is Route.AdminMembers -> MembersScreen(memberVm)
                    is Route.AdminOccupancy -> OccupancyScreen(api)
                    is Route.Downloads -> DownloadsScreen()
                }
            }
        }
    }
}
