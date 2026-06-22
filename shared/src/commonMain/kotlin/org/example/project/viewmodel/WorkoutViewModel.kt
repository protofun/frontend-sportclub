package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

// State for the admin Workouts screen.
data class WorkoutState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workouts: List<Workout> = emptyList(),
    val showDialog: Boolean = false,
    val editingWorkout: Workout? = null,
    val showDeleteConfirm: String? = null
)

// Manages CRUD operations for workout types
// WorkoutsScreen only reads vm.state and calls vm functions
class WorkoutViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(WorkoutState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Loads all workouts from GET /api/v1/workouts.
    fun load() {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                state = state.copy(isLoading = false, workouts = api.getWorkouts())
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Dialog control called by edit and delete
    fun showAddDialog() { state = state.copy(showDialog = true, editingWorkout = null) }
    fun showEditDialog(w: Workout) { state = state.copy(showDialog = true, editingWorkout = w) }
    fun dismissDialog() { state = state.copy(showDialog = false, editingWorkout = null) }

    // Delete confirmation stores the ID
    fun showDeleteConfirm(id: String) { state = state.copy(showDeleteConfirm = id) }
    fun dismissDeleteConfirm() { state = state.copy(showDeleteConfirm = null) }

    // Saves a workout: PUT /workouts/{id} if editing, POST /workouts if new
    // Reloads the list after a successful save
    fun save(name: String, description: String) {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                val req     = WorkoutRequest(name, description)
                val editing = state.editingWorkout
                if (editing != null) api.updateWorkout(editing.id, req) else api.createWorkout(req)
                state = state.copy(isLoading = false, showDialog = false, editingWorkout = null)
                load()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Deletes a workout by ID via DELETE /workouts/{id}, then reloads
    fun delete(id: String) {
        scope.launch {
            state = state.copy(isLoading = true, showDeleteConfirm = null)
            try { api.deleteWorkout(id); load() }
            catch (e: Exception) { state = state.copy(isLoading = false, error = e.message) }
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
