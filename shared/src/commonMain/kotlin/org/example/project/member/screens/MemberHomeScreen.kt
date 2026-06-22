package org.example.project.member.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.member.navigation.MemberNavigator
import org.example.project.member.navigation.MemberRoute
import org.example.project.model.*
import org.example.project.util.todayDateString
import org.example.project.viewmodel.MemberSessionViewModel

// MemberHomeScreen is the first screen a member sees after logging in.
// It shows subscription status, upcoming classes and quick-action shortcuts.
@Composable
fun MemberHomeScreen(navigator: MemberNavigator, sessionVm: MemberSessionViewModel) {
    val state = sessionVm.state
    val user = navigator.currentUser
    val scroll = rememberScrollState()

    // Load data once the user is known
    LaunchedEffect(user?.userId) {
        user?.let { u ->
            sessionVm.loadMemberInfo(u.userId)
            sessionVm.loadEnrollments(u.userId)
        }
    }

    // Derive first name and initials for the greeting header and avatar.
    val firstName = user?.fullName?.split(" ")?.firstOrNull() ?: "Member"
    val initials = user?.fullName?.split(" ")?.take(2)
        ?.mapNotNull { it.firstOrNull()?.toString() }?.joinToString("") ?: "?"

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2))))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Column {
                    Text("Hello, $firstName!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Welcome back to SportClub", color = Color.White.copy(0.85f), fontSize = 14.sp)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SubscriptionStatusCard(state.memberInfo?.activeMembership)

            Text("Upcoming Classes", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            val upcoming = state.enrolledLessons
                .filter { it.startTime >= todayDateString() }
                .sortedBy { it.startTime }
                .take(3)

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (upcoming.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No upcoming classes", fontWeight = FontWeight.Medium)
                        Text("Reserve a spot in the schedule!", fontSize = 13.sp, color = Color(0xFF757575))
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navigator.navigate(MemberRoute.Schedule) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) { Text("Browse Schedule") }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcoming.forEach { lesson -> MemberLessonCard(lesson, onCancel = null) }
                    TextButton(onClick = { navigator.navigate(MemberRoute.MyClasses) }) {
                        Text("View all my classes →", color = Color(0xFF1565C0))
                    }
                }
            }

            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard("📅", "Schedule", { navigator.navigate(MemberRoute.Schedule) }, Modifier.weight(1f))
                QuickActionCard("🏋️", "My Classes", { navigator.navigate(MemberRoute.MyClasses) }, Modifier.weight(1f))
                QuickActionCard("💳", "Membership", { navigator.navigate(MemberRoute.Subscription) }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// SubscriptionStatusCard shows a green or red banner with the current subscription status.
@Composable
private fun SubscriptionStatusCard(sub: Membership?) {
    val isActive = sub?.status == MembershipStatus.ACTIVE
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Subscription", fontSize = 13.sp, color = Color(0xFF757575))
                Text(sub?.planName ?: "No active subscription", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (sub != null) {
                    Text(
                        if (isActive) "Active · since ${sub.startDate}" else "Expired",
                        fontSize = 12.sp,
                        color = if (isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    sub?.status?.name ?: "NONE",
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// MemberLessonCard displays one lesson
// onCancel is null for past lessons (no cancel button is shown in that case).
@Composable
fun MemberLessonCard(lesson: org.example.project.model.Lesson, onCancel: (() -> Unit)?) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.width(52.dp).height(52.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1565C0).copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    lesson.startTime.substring(11, 16),
                    fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.workoutName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "${lesson.locationName} · ${lesson.instructorName ?: "TBD"} · ${lesson.durationMinutes} min",
                    fontSize = 12.sp, color = Color(0xFF757575)
                )
                Text(
                    "${lesson.startTime.substring(0, 10)} at ${lesson.startTime.substring(11, 16)}",
                    fontSize = 12.sp, color = Color(0xFF1565C0)
                )
            }
            if (onCancel != null) {
                TextButton(onClick = onCancel, contentPadding = PaddingValues(4.dp)) {
                    Text("Cancel", fontSize = 12.sp, color = Color(0xFFF44336))
                }
            }
        }
    }
}

// QuickActionCard fro shortcuts
@Composable
private fun QuickActionCard(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}
