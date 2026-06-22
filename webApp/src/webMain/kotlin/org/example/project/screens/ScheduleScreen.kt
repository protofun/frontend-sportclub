package org.example.project.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.*
import org.example.project.navigation.Navigator
import org.example.project.theme.*
import org.example.project.viewmodel.ScheduleViewModel

// Public schedule page. Shows all lessons for a selected date with an optional workout filter.
// Backend call: vm.load(date) → GET /api/v1/lessons?from=<date>T00:00:00&to=<date>T23:59:59
//
// Navigation: ◀ / ▶ buttons call vm.previousDay() / vm.nextDay() which re-fetch lessons

@Composable
fun ScheduleScreen(navigator: Navigator, vm: ScheduleViewModel) {
    LaunchedEffect(Unit) { vm.load() } // load today's lessons on first render
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(32.dp)) {
            Column {
                Text("Class Schedule", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Find and book the perfect class for you", fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            // Date navigation card
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // previousDay re-fetches lessons for date - 1
                    IconButton(onClick = { vm.previousDay() }) { Text("◀", fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        // dayOfWeek and formatDate
                        Text(dayOfWeek(state.selectedDate), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(formatDate(state.selectedDate), fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                    IconButton(onClick = { vm.nextDay() }) { Text("▶", fontSize = 18.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Workout filter chips only shown after workouts have loaded.
            // Selecting a chip calls vm.setFilter(id) which filters state.lessons client-side.
            if (state.workouts.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChipItem("All", selected = state.filterWorkoutId == null, onClick = { vm.setFilter(null) })
                    state.workouts.forEach { workout ->
                        FilterChipItem(
                            label = workout.name,
                            selected = state.filterWorkoutId == workout.id,
                            // Toggle: clicking an already-selected filter removes it
                            onClick = { vm.setFilter(if (state.filterWorkoutId == workout.id) null else workout.id) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Lesson list
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.error != null) {
                ErrorBanner(state.error ?: "", {})
            } else {
                val lessons = state.filteredLessons
                if (lessons.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(48.dp), tint = OnSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("No classes scheduled", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            Text("Try a different date or filter", color = OnSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${lessons.size} class${if (lessons.size != 1) "es" else ""} available", fontSize = 14.sp, color = OnSurfaceVariant)
                        // LessonCard is the shared card component from CommonComponents.kt
                        lessons.forEach { lesson -> LessonCard(lesson, Modifier.fillMaxWidth()) }
                    }
                }
            }
        }
    }
}

// Pill-shaped filter chip Blue fill when selected, white when not.
@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Primary else Surface)
            .border(1.dp, if (selected) Primary else Color.LightGray, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, color = if (selected) Color.White else OnSurface, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
    }
}
