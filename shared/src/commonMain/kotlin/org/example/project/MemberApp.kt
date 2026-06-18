package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.api.RealApiService
import org.example.project.api.SportClubApiService
import org.example.project.member.navigation.MemberNavigator
import org.example.project.member.navigation.MemberRoute
import org.example.project.member.screens.*
import org.example.project.model.UserRole
import org.example.project.viewmodel.*

@Composable
fun MemberApp() {
    val api: SportClubApiService = remember { RealApiService() }
    val navigator = remember { MemberNavigator() }
    val authVm = remember { AuthViewModel(api) }
    val scheduleVm = remember { ScheduleViewModel(api) }
    val sessionVm = remember { MemberSessionViewModel(api) }
    val instructorLessonsVm = remember { InstructorLessonsViewModel(api) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1565C0),
            secondary = Color(0xFFFF6D00),
            background = Color(0xFFF8F9FA),
            surface = Color.White
        )
    ) {
        if (!navigator.isAuthenticated) {
            MemberLoginScreen(navigator, authVm)
        } else {
            val isInstructor = navigator.currentUser?.role == UserRole.INSTRUCTOR
            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        val items = if (isInstructor) {
                            listOf(
                                Triple(MemberRoute.InstructorLessons, "My Lessons", "📋"),
                                Triple(MemberRoute.Profile, "Profile", "👤")
                            )
                        } else {
                            listOf(
                                Triple(MemberRoute.Home, "Home", "🏠"),
                                Triple(MemberRoute.Schedule, "Schedule", "📅"),
                                Triple(MemberRoute.MyClasses, "My Classes", "🏋️"),
                                Triple(MemberRoute.Subscription, "Membership", "💳"),
                                Triple(MemberRoute.Profile, "Profile", "👤")
                            )
                        }
                        items.forEach { (route, label, icon) ->
                            NavigationBarItem(
                                selected = navigator.currentRoute == route,
                                onClick = { navigator.navigate(route) },
                                icon = { Text(icon, fontSize = 22.sp) },
                                label = {
                                    Text(
                                        label, fontSize = 10.sp,
                                        fontWeight = if (navigator.currentRoute == route) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = Color(0xFF1565C0),
                                    unselectedTextColor = Color(0xFF757575),
                                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    when (navigator.currentRoute) {
                        is MemberRoute.Home -> MemberHomeScreen(navigator, sessionVm)
                        is MemberRoute.Schedule -> MemberScheduleScreen(navigator, scheduleVm, sessionVm)
                        is MemberRoute.MyClasses -> MemberMyClassesScreen(navigator, sessionVm)
                        is MemberRoute.Subscription -> MemberSubscriptionScreen(navigator, sessionVm)
                        is MemberRoute.Profile -> MemberProfileScreen(navigator, sessionVm, authVm)
                        is MemberRoute.InstructorLessons -> InstructorLessonsScreen(navigator, instructorLessonsVm)
                    }
                }
            }
        }
    }
}
