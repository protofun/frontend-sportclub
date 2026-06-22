package org.example.project.screens.admin

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
import org.example.project.components.*
import org.example.project.model.*
import org.example.project.theme.*
import org.example.project.util.todayDateString
import org.example.project.viewmodel.LessonViewModel

// Admin lesson management screen — create, edit, and delete scheduled class sessions.
// Shows a week view; ◀ / ▶ navigate by week via vm.previousWeek() / vm.nextWeek().
//
// On open: vm.loadAll() fetches lessons, workouts, instructors, and locations in parallel.
// The edit dialog lets admins pick workout, instructor, location, time, duration, and capacity.
// Recurring lessons (DAILY/WEEKLY with an end date) create a series via POST /lessons/recurring.
@Composable
fun LessonManagementScreen(vm: LessonViewModel) {
    LaunchedEffect(Unit) { vm.loadAll() }
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Class Schedule", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Manage and schedule workout sessions", fontSize = 13.sp, color = Color.White.copy(0.8f))
                }
                Button(
                    onClick = { vm.showAddDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) { Text("+ Add Class") }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Week navigation
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { vm.previousWeek() }) { Text("◀", fontSize = 18.sp) }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Week of", fontSize = 13.sp, color = OnSurfaceVariant)
                        Text(formatDate(state.selectedWeekStart), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = { vm.nextWeek() }) { Text("▶", fontSize = 18.sp) }
                }
            }

            state.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Error.copy(0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(it, modifier = Modifier.padding(16.dp), color = Error)
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.lessons.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("No classes scheduled this week. Add the first one!", color = OnSurfaceVariant)
                }
            } else {
                // Group by date, sorted (toSortedMap not available in JS)
                val groupedEntries = state.lessons
                    .groupBy { it.startTime.substring(0, 10) }
                    .entries
                    .sortedBy { it.key }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (entry in groupedEntries) {
                        val date = entry.key
                        val dayLessons = entry.value.sortedBy { it.startTime }
                        Text(
                            "${dayOfWeek(date)} – ${formatDate(date)}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Primary
                        )
                        for (lesson in dayLessons) {
                            AdminLessonRow(
                                lesson = lesson,
                                onEdit = { vm.showEditDialog(lesson) },
                                onDelete = { vm.showDeleteConfirm(lesson.id) }
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }

    if (state.showDialog) {
        LessonDialog(
            existing = state.editingLesson,
            workouts = state.workouts,
            instructors = state.instructors,
            locations = state.locations,
            onSave = { wId, iId, lId, startTime, dur, capacity, rec, recEnd ->
                vm.save(wId, iId, lId, startTime, dur, capacity, rec, recEnd)
            },
            onDismiss = { vm.dismissDialog() }
        )
    }

    state.showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteConfirm() },
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete this class? Enrolled members will be notified.") },
            confirmButton = {
                Button(onClick = { vm.delete(id) }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissDeleteConfirm() }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminLessonRow(lesson: Lesson, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(formatTime(lesson.startTime), fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.width(48.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.workoutName, fontWeight = FontWeight.Medium)
                Text("${lesson.locationName} · ${lesson.instructorName ?: "TBD"} · ${lesson.durationMinutes} min", fontSize = 12.sp, color = OnSurfaceVariant)
            }
            OccupancyBadge(lesson.enrolledCount, lesson.maxCapacity)
            Row {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(4.dp)) { Text("Edit", fontSize = 12.sp, color = Primary) }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(4.dp)) { Text("Delete", fontSize = 12.sp, color = Error) }
            }
        }
    }
}

@Composable
private fun LessonDialog(
    existing: Lesson?,
    workouts: List<Workout>,
    instructors: List<Instructor>,
    locations: List<Location>,
    onSave: (String, String?, String, String, Int, Int, RecurrenceType, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWorkout by remember { mutableStateOf(existing?.workoutId ?: workouts.firstOrNull()?.id ?: "") }
    var selectedInstructor by remember { mutableStateOf<String?>(existing?.instructorId) }
    var selectedLocation by remember { mutableStateOf(existing?.locationId ?: locations.firstOrNull()?.id ?: "") }
    val existingDate = existing?.startTime?.substringBefore("T") ?: todayDateString()
    val existingTime = existing?.startTime?.substringAfter("T")?.substring(0, 5) ?: "09:00"
    var startDate by remember { mutableStateOf(existingDate) }
    var startTimeOfDay by remember { mutableStateOf(existingTime) }
    var duration by remember { mutableStateOf((existing?.durationMinutes ?: 60).toString()) }
    var capacity by remember { mutableStateOf((existing?.maxCapacity ?: 10).toString()) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var recEndDate by remember { mutableStateOf("") }

    var workoutExpanded by remember { mutableStateOf(false) }
    var instructorExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing != null) "Edit Class" else "Add Class") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Workout dropdown
                Text("Workout *", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Box {
                    OutlinedButton(onClick = { workoutExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(workouts.find { it.id == selectedWorkout }?.name ?: "Select workout")
                    }
                    DropdownMenu(expanded = workoutExpanded, onDismissRequest = { workoutExpanded = false }) {
                        workouts.forEach { w ->
                            DropdownMenuItem(text = { Text(w.name) }, onClick = { selectedWorkout = w.id; workoutExpanded = false })
                        }
                    }
                }

                // Location dropdown
                Text("Location *", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Box {
                    OutlinedButton(onClick = { locationExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(locations.find { it.id == selectedLocation }?.name ?: "Select location")
                    }
                    DropdownMenu(expanded = locationExpanded, onDismissRequest = { locationExpanded = false }) {
                        locations.forEach { l ->
                            DropdownMenuItem(
                                text = { Text("${l.name} (cap. ${l.capacity})") },
                                onClick = { selectedLocation = l.id; locationExpanded = false }
                            )
                        }
                    }
                }

                // Instructor dropdown
                Text("Instructor", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Box {
                    OutlinedButton(onClick = { instructorExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(instructors.find { it.id == selectedInstructor }?.fullName ?: "TBD")
                    }
                    DropdownMenu(expanded = instructorExpanded, onDismissRequest = { instructorExpanded = false }) {
                        DropdownMenuItem(text = { Text("TBD") }, onClick = { selectedInstructor = null; instructorExpanded = false })
                        instructors.forEach { ins ->
                            DropdownMenuItem(text = { Text(ins.fullName) }, onClick = { selectedInstructor = ins.id; instructorExpanded = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(startDate, { startDate = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(2f), placeholder = { Text("2026-06-21") })
                    OutlinedTextField(startTimeOfDay, { startTimeOfDay = it }, label = { Text("Time (HH:MM)") }, modifier = Modifier.weight(1f), placeholder = { Text("09:00") })
                    OutlinedTextField(duration, { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(capacity, { capacity = it }, label = { Text("Capacity") }, modifier = Modifier.fillMaxWidth())

                // Recurrence
                Text("Recurrence", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceType.values().forEach { type ->
                        FilterChip(
                            selected = recurrence == type,
                            onClick = { recurrence = type },
                            label = { Text(type.name, fontSize = 12.sp) }
                        )
                    }
                }

                if (recurrence != RecurrenceType.NONE) {
                    OutlinedTextField(recEndDate, { recEndDate = it }, label = { Text("Repeat until (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("2026-12-31") })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val time = if (startTimeOfDay.length == 5) "${startTimeOfDay}:00" else startTimeOfDay
                    val combinedStartTime = "${startDate}T${time}"
                    onSave(
                        selectedWorkout, selectedInstructor, selectedLocation,
                        combinedStartTime, duration.toIntOrNull() ?: 60,
                        capacity.toIntOrNull() ?: 10,
                        recurrence,
                        recEndDate.ifBlank { null }
                    )
                },
                enabled = selectedWorkout.isNotBlank() && selectedLocation.isNotBlank() &&
                          startDate.isNotBlank() && startTimeOfDay.isNotBlank()
            ) { Text(if (existing != null) "Save Changes" else "Add Class") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
