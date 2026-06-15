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
import org.example.project.viewmodel.MemberSessionViewModel
import org.example.project.viewmodel.ScheduleViewModel

@Composable
fun MemberScheduleScreen(
    navigator: MemberNavigator,
    scheduleVm: ScheduleViewModel,
    sessionVm: MemberSessionViewModel
) {
    val state = scheduleVm.state
    val session = sessionVm.state
    val user = navigator.currentUser
    val scroll = rememberScrollState()

    LaunchedEffect(state.selectedDate) { scheduleVm.load() }
    LaunchedEffect(user?.userId) { user?.let { sessionVm.loadWaitlist(it.userId) } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Column {
                Text("Class Schedule", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Reserve your spot in a class", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Day navigation
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scheduleVm.previousDay() }) { Text("◀") }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayOfWeekName(state.selectedDate), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(state.selectedDate, fontSize = 13.sp, color = Color(0xFF757575))
                    }
                    IconButton(onClick = { scheduleVm.nextDay() }) { Text("▶") }
                }
            }

            session.error?.let { err ->
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
                        TextButton(onClick = { sessionVm.dismissError() }) { Text("OK") }
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                val lessons = state.filteredLessons
                if (lessons.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📅", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No classes today", fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lessons.forEach { lesson ->
                            val isEnrolled = lesson.id in session.enrolledLessonIds
                            val isWaitlisted = lesson.id in session.waitlistLessonIds
                            ScheduleLessonCard(
                                lesson = lesson,
                                isEnrolled = isEnrolled,
                                isWaitlisted = isWaitlisted,
                                onReserve = { user?.let { sessionVm.reserve(lesson.id, it.userId) { scheduleVm.load() } } },
                                onCancel = { user?.let { sessionVm.cancel(lesson.id, it.userId) { scheduleVm.load() } } },
                                onJoinWaitlist = { user?.let { sessionVm.joinWaitlist(lesson.id, it.userId) } },
                                onLeaveWaitlist = { user?.let { sessionVm.leaveWaitlist(lesson.id, it.userId) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleLessonCard(
    lesson: Lesson,
    isEnrolled: Boolean,
    isWaitlisted: Boolean,
    onReserve: () -> Unit,
    onCancel: () -> Unit,
    onJoinWaitlist: () -> Unit,
    onLeaveWaitlist: () -> Unit
) {
    val cardColor = when {
        isEnrolled -> Color(0xFFE8F5E9)
        isWaitlisted -> Color(0xFFFFF8E1)
        else -> Color.White
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.width(54.dp).height(54.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1565C0).copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lesson.startTime.substring(11, 16), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(lesson.workoutName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("${lesson.instructorName ?: "TBD"} · ${lesson.durationMinutes} min", fontSize = 13.sp, color = Color(0xFF757575))
                    Text(lesson.locationName, fontSize = 13.sp, color = Color(0xFF757575))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${lesson.enrolledCount}/${lesson.maxCapacity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        if (lesson.isFull) "Full" else "${lesson.availableSpots} spots",
                        fontSize = 11.sp,
                        color = if (lesson.isFull) Color(0xFFF44336) else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isEnrolled -> {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF4CAF50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Enrolled", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onCancel,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, Color(0xFFF44336))
                        ) { Text("Cancel", fontSize = 12.sp, color = Color(0xFFF44336)) }
                    }
                    isWaitlisted -> {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFF9800))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Waitlisted", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = onLeaveWaitlist,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Leave Waitlist", fontSize = 12.sp) }
                    }
                    lesson.isFull -> {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFCDD2))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Full", color = Color(0xFFC62828), fontSize = 12.sp) }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onJoinWaitlist,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) { Text("Waitlist", fontSize = 12.sp) }
                    }
                    else -> {
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onReserve,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                        ) { Text("Reserve", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

private fun dayOfWeekName(date: String): String {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    return try {
        val parts = date.split("-")
        val y = parts[0].toInt(); val m = parts[1].toInt(); val d = parts[2].toInt()
        val t = (14 - m) / 12; val yr = y - t; val mr = m + 12 * t - 2
        val dow = (d + yr + yr / 4 - yr / 100 + yr / 400 + (31 * mr) / 12) % 7
        days[if (dow == 0) 6 else dow - 1]
    } catch (e: Exception) { date }
}
