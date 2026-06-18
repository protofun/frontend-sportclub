package org.example.project.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.components.*
import org.example.project.model.DashboardStats
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*
import org.example.project.viewmodel.ScheduleViewModel

@Composable
fun AdminDashboardScreen(navigator: Navigator, api: SportClubApiService, scheduleVm: ScheduleViewModel) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scheduleVm.load()
        try {
            stats = api.getDashboardStats()
        } catch (e: Exception) { }
        isLoading = false
    }

    val scroll = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Column {
                Text("Admin Dashboard", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Welcome back, ${navigator.currentUser?.fullName ?: "Admin"}", fontSize = 14.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Stats
            if (isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { Box(modifier = Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant)) }
                }
            } else {
                stats?.let { s ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard("Total Members", s.totalMembers.toString(), Primary, Modifier.weight(1f))
                        StatCard("Active Members", s.activeMembers.toString(), Success, Modifier.weight(1f))
                        StatCard("Classes Today", s.lessonsToday.toString(), Secondary, Modifier.weight(1f))
                        StatCard("Enrollments Today", s.enrollmentsToday.toString(), Color(0xFF7B1FA2), Modifier.weight(1f))
                    }
                }
            }

            // Quick actions
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val actions = listOf(
                    Triple(Icons.Default.FitnessCenter, "Manage Workouts", Route.AdminWorkouts),
                    Triple(Icons.Default.Person, "Manage Instructors", Route.AdminInstructors),
                    Triple(Icons.Default.CalendarMonth, "Schedule Classes", Route.AdminSchedule),
                    Triple(Icons.Default.Group, "View Members", Route.AdminMembers),
                    Triple(Icons.Default.BarChart, "Class Occupancy", Route.AdminOccupancy)
                )
                actions.forEach { (icon, label, route) ->
                    QuickActionCard(icon, label, { navigator.navigate(route) }, Modifier.weight(1f))
                }
            }

            // Today's schedule preview
            Text("Today's Classes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            val s = scheduleVm.state
            if (s.isLoading) {
                CircularProgressIndicator()
            } else {
                val today = s.lessons.take(5)
                if (today.isEmpty()) {
                    Text("No classes scheduled for today.", color = OnSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        today.forEach { lesson ->
                            LessonCard(lesson, Modifier.fillMaxWidth())
                        }
                        if (s.lessons.size > 5) {
                            TextButton(onClick = { navigator.navigate(Route.AdminSchedule) }) {
                                Text("View all ${s.lessons.size} classes →")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Primary)
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
