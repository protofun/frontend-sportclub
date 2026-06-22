package org.example.project.member.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.member.navigation.MemberNavigator
import org.example.project.model.Lesson
import org.example.project.model.LessonRosterEntry
import org.example.project.util.todayDateString
import org.example.project.viewmodel.InstructorLessonsViewModel

// InstructorLessonsScreen is the main screen for instructors
// Shows all lessons assigned to the instructor, split into "Upcoming" and "Past"
@Composable
fun InstructorLessonsScreen(navigator: MemberNavigator, vm: InstructorLessonsViewModel) {
    val state = vm.state
    val scroll = rememberScrollState()

    // Load all lessons for this instructor when the screen opens
    LaunchedEffect(navigator.currentUser?.userId) {
        vm.loadMyLessons()
    }

    val today = todayDateString()
    // Split into upcoming and past lessons based on start date
    val upcoming = state.lessons.filter { it.startTime >= today }
    val past = state.lessons.filter { it.startTime < today }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Column {
                Text("My Lessons", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "See which members are registered for your classes",
                    fontSize = 13.sp, color = Color.White.copy(0.8f)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err, color = Color(0xFFC62828), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.dismissError() }) { Text("OK") }
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else if (state.lessons.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No lessons assigned", fontWeight = FontWeight.Medium)
                        Text("Lessons assigned to you will appear here", fontSize = 13.sp, color = Color(0xFF757575))
                    }
                }
            } else {
                if (upcoming.isNotEmpty()) {
                    Text("Upcoming", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF757575))
                    upcoming.forEach { lesson ->
                        InstructorLessonCard(lesson, state, vm)
                    }
                }
                if (past.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Past", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF757575))
                    past.forEach { lesson ->
                        InstructorLessonCard(lesson, state, vm)
                    }
                }
            }
        }
    }
}

// InstructorLessonCard shows one lesson as a collapsible card
// Tapping it expands the card and loads the participant list
@Composable
private fun InstructorLessonCard(
    lesson: Lesson,
    state: org.example.project.viewmodel.InstructorLessonsState,
    vm: InstructorLessonsViewModel
) {
    // expanded is true when this lesson is the currently selected one
    val expanded = state.selectedLessonId == lesson.id
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { vm.selectLesson(lesson.id) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.width(52.dp).height(52.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1565C0).copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lesson.startTime.substring(11, 16), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(lesson.workoutName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "${lesson.locationName} · ${lesson.durationMinutes} min",
                        fontSize = 12.sp, color = Color(0xFF757575)
                    )
                    Text(
                        "${lesson.startTime.substring(0, 10)} · ${lesson.enrolledCount}/${lesson.maxCapacity} enrolled",
                        fontSize = 12.sp, color = Color(0xFF1565C0)
                    )
                }
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp, color = Color(0xFF757575))
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                when {
                    state.isLoadingRoster -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1565C0), strokeWidth = 2.dp)
                        }
                    }
                    state.roster.isEmpty() -> {
                        Text("No members registered yet", fontSize = 13.sp, color = Color(0xFF757575))
                    }
                    else -> {
                        Text("Registered members (${state.roster.size})", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        state.roster.forEach { entry ->
                            RosterEntryRow(entry)
                        }
                    }
                }
            }
        }
    }
}

// RosterEntryRow shows one member from the participant list: avatar initial, name, email and status
@Composable
private fun RosterEntryRow(entry: LessonRosterEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF1565C0).copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.name.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(entry.email, fontSize = 12.sp, color = Color(0xFF757575))
        }
        Text(entry.status, fontSize = 11.sp, color = Color(0xFF1565C0))
    }
}
