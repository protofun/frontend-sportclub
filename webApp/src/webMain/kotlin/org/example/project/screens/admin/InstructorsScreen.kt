package org.example.project.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import org.example.project.components.ErrorBanner
import org.example.project.model.Instructor
import org.example.project.theme.*
import org.example.project.viewmodel.InstructorViewModel

// Admin screen for managing instructors.
// Same Add/Edit/Delete pattern as WorkoutsScreen but backed by InstructorViewModel.
// Instructors are stored as users with role "Instructor" in the backend.
// GET: /users filtered by role | POST/PUT: /users/instructors | DELETE: /users/{id}
@Composable
fun InstructorsScreen(vm: InstructorViewModel) {
    LaunchedEffect(Unit) { vm.load() }
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Instructors", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Manage club instructors", fontSize = 13.sp, color = Color.White.copy(0.8f))
                }
                Button(
                    onClick = { vm.showAddDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) { Text("+ Add Instructor") }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            state.error?.let {
                ErrorBanner(it) { vm.clearError() }
                Spacer(Modifier.height(12.dp))
            }

            if (state.isLoading && state.instructors.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    state.instructors.forEach { instructor ->
                        InstructorCard(
                            instructor = instructor,
                            onEdit = { vm.showEditDialog(instructor) },
                            onDelete = { vm.showDeleteConfirm(instructor.id) },
                            modifier = Modifier.weight(1f).widthIn(max = 280.dp)
                        )
                    }
                    if (state.instructors.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text("No instructors yet. Add your first instructor!", color = OnSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (state.showDialog) {
        InstructorDialog(
            existing = state.editingInstructor,
            onSave = { firstName, lastName, email, specialties -> vm.save(firstName, lastName, email, specialties) },
            onDismiss = { vm.dismissDialog() }
        )
    }

    state.showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteConfirm() },
            title = { Text("Remove Instructor") },
            text = { Text("Are you sure you want to remove this instructor?") },
            confirmButton = {
                Button(onClick = { vm.delete(id) }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { vm.dismissDeleteConfirm() }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun InstructorCard(instructor: Instructor, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${instructor.firstName.first()}${instructor.lastName.first()}",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(instructor.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(instructor.email, fontSize = 13.sp, color = OnSurfaceVariant)
            if (instructor.specialties.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    instructor.specialties.take(3).forEach { specialty ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PrimaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(specialty, fontSize = 11.sp, color = PrimaryDark)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit", fontSize = 13.sp) }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Remove", fontSize = 13.sp, color = Error)
                }
            }
        }
    }
}

@Composable
private fun InstructorDialog(
    existing: Instructor?,
    onSave: (String, String, String, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var firstName by remember { mutableStateOf(existing?.firstName ?: "") }
    var lastName by remember { mutableStateOf(existing?.lastName ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var specialties by remember { mutableStateOf(existing?.specialties?.joinToString(", ") ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing != null) "Edit Instructor" else "Add Instructor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    specialties, { specialties = it },
                    label = { Text("Specialties (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val specialtyList = specialties.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(firstName, lastName, email, specialtyList)
                    }
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank()
            ) { Text(if (existing != null) "Save Changes" else "Add Instructor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
