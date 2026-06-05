package org.example.project.member.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.member.navigation.MemberNavigator
import org.example.project.viewmodel.MemberSessionViewModel

@Composable
fun MemberMyClassesScreen(navigator: MemberNavigator, sessionVm: MemberSessionViewModel) {
    val state = sessionVm.state
    val user = navigator.currentUser
    var showUpcoming by remember { mutableStateOf(true) }
    val scroll = rememberScrollState()

    LaunchedEffect(user?.userId) {
        user?.let { sessionVm.loadEnrollments(it.userId) }
    }

    val today = "2026-06-05"
    val upcoming = state.enrolledLessons.filter { it.startTime >= today }.sortedBy { it.startTime }
    val past = state.enrolledLessons.filter { it.startTime < today }.sortedByDescending { it.startTime }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Column {
                Text("My Classes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${upcoming.size} upcoming · ${past.size} past", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            TabItem("Upcoming (${upcoming.size})", showUpcoming) { showUpcoming = true }
            TabItem("History (${past.size})", !showUpcoming) { showUpcoming = false }
        }
        HorizontalDivider()

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                val lessons = if (showUpcoming) upcoming else past
                if (lessons.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (showUpcoming) "🗓️" else "📋", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (showUpcoming) "No upcoming classes" else "No class history",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (showUpcoming) "Reserve a spot in the schedule" else "Classes you attend will appear here",
                                fontSize = 13.sp, color = Color(0xFF757575)
                            )
                        }
                    }
                } else {
                    lessons.forEach { lesson ->
                        MemberLessonCard(
                            lesson = lesson,
                            onCancel = if (showUpcoming) ({
                                user?.let { u -> sessionVm.cancel(lesson.id, u.userId) }
                            }) else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color(0xFF1565C0) else Color(0xFF757575),
                fontSize = 14.sp
            )
            if (selected) {
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color(0xFF1565C0)))
            }
        }
    }
}
