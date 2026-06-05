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
import org.example.project.viewmodel.LessonViewModel

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
            onSave = { wId, iId, lId, startTime, dur, rec, recEnd, recCount ->
                vm.save(wId, iId, lId, startTime, dur, rec, recEnd, recCount)
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
    onSave: (Int, Int?, Int, String, Int, RecurrenceType, String?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWorkout by remember { mutableStateOf(existing?.workoutId ?: workouts.firstOrNull()?.id ?: 0) }
    var selectedInstructor by remember { mutableStateOf<Int?>(existing?.instructorId) }
    var selectedLocation by remember { mutableStateOf(existing?.locationId ?: locations.firstOrNull()?.id ?: 0) }
    var startTime by remember { mutableStateOf(existing?.startTime ?: "2026-06-05T09:00:00") }
    var duration by remember { mutableStateOf((existing?.durationMinutes ?: 60).toString()) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }
    var recEndDate by remember { mutableStateOf("") }
    var recCount by remember { mutableStateOf("") }

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
                    OutlinedTextField(startTime, { startTime = it }, label = { Text("Start Time (YYYY-MM-DDThh:mm:ss)") }, modifier = Modifier.weight(2f))
                    OutlinedTextField(duration, { duration = it }, label = { Text("Duration (min)") }, modifier = Modifier.weight(1f))
                }

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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(recEndDate, { recEndDate = it }, label = { Text("End Date (optional)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(recCount, { recCount = it }, label = { Text("# Occurrences") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedWorkout, selectedInstructor, selectedLocation,
                        startTime, duration.toIntOrNull() ?: 60,
                        recurrence,
                        recEndDate.ifBlank { null },
                        recCount.toIntOrNull()
                    )
                },
                enabled = selectedWorkout > 0 && selectedLocation > 0 && startTime.isNotBlank()
            ) { Text(if (existing != null) "Save Changes" else "Add Class") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
