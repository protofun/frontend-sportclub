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
import org.example.project.components.ErrorBanner
import org.example.project.components.SectionTitle
import org.example.project.model.Workout
import org.example.project.theme.*
import org.example.project.viewmodel.WorkoutViewModel

// Admin screen for managing workout types (Yoga, Boxing, Spinning, …).
// Displays workouts in a table. Add/Edit opens an AlertDialog; Delete shows a confirm dialog.
//
// State flow: vm.load() → api.getWorkouts() → state.workouts → renders table rows
// Edit flow:  row "Edit" click → vm.showEditDialog(workout) → state.showDialog = true → WorkoutDialog shown
// Save flow:  dialog Save → vm.save(name, desc) → api.createWorkout/updateWorkout → vm.load()
@Composable
fun WorkoutsScreen(vm: WorkoutViewModel) {
    LaunchedEffect(Unit) { vm.load() }
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Workouts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Manage workout types offered at the club", fontSize = 13.sp, color = Color.White.copy(0.8f))
                }
                Button(onClick = { vm.showAddDialog() }, colors = ButtonDefaults.buttonColors(containerColor = Secondary)) {
                    Text("+ Add Workout")
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            state.error?.let { ErrorBanner(it) { vm.clearError() }; Spacer(Modifier.height(12.dp)) }

            if (state.isLoading && state.workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        // Table header row
                        Row(modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("#",           fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(32.dp))
                            Text("Name",        fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Description", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                            Text("Actions",     fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(120.dp))
                        }
                        HorizontalDivider()
                        state.workouts.forEachIndexed { i, workout ->
                            WorkoutRow(index = i + 1, workout = workout, onEdit = { vm.showEditDialog(workout) }, onDelete = { vm.showDeleteConfirm(workout.id) })
                            if (i < state.workouts.size - 1) HorizontalDivider(color = Color.LightGray.copy(0.5f))
                        }
                        if (state.workouts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text("No workouts yet. Add your first workout!", color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit dialog — shown when state.showDialog = true
    if (state.showDialog) {
        WorkoutDialog(existing = state.editingWorkout, onSave = { name, desc -> vm.save(name, desc) }, onDismiss = { vm.dismissDialog() })
    }

    // Delete confirmation — shown when state.showDeleteConfirm holds an ID
    state.showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteConfirm() },
            title = { Text("Delete Workout") },
            text  = { Text("Are you sure? This will also delete all associated lessons.") },
            confirmButton = { Button(onClick = { vm.delete(id) }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { vm.dismissDeleteConfirm() }) { Text("Cancel") } }
        )
    }
}

// Single table row for one workout.
@Composable
private fun WorkoutRow(index: Int, workout: Workout, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$index", fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.width(32.dp))
        Text(workout.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(workout.description, fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(2f))
        Row(modifier = Modifier.width(120.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onEdit,   contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("Edit",   fontSize = 13.sp, color = Primary) }
            TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) { Text("Delete", fontSize = 13.sp, color = Error) }
        }
    }
}

// AlertDialog for adding or editing a workout.
// existing = null → "Add Workout"; existing = Workout → "Edit Workout" with pre-filled fields.
@Composable
private fun WorkoutDialog(existing: Workout?, onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    // Pre-fill fields from the existing workout, or start empty for a new one.
    var name        by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing != null) "Edit Workout" else "Add Workout") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Workout Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            // Button is disabled until the name field has text
            Button(onClick = { if (name.isNotBlank()) onSave(name, description) }, enabled = name.isNotBlank()) {
                Text(if (existing != null) "Save Changes" else "Add Workout")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
