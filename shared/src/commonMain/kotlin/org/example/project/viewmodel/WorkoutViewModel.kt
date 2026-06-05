package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class WorkoutState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val workouts: List<Workout> = emptyList(),
    val showDialog: Boolean = false,
    val editingWorkout: Workout? = null,
    val showDeleteConfirm: Int? = null
)

class WorkoutViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(WorkoutState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

    fun showAddDialog() { state = state.copy(showDialog = true, editingWorkout = null) }
    fun showEditDialog(w: Workout) { state = state.copy(showDialog = true, editingWorkout = w) }
    fun dismissDialog() { state = state.copy(showDialog = false, editingWorkout = null) }
    fun showDeleteConfirm(id: Int) { state = state.copy(showDeleteConfirm = id) }
    fun dismissDeleteConfirm() { state = state.copy(showDeleteConfirm = null) }

    fun save(name: String, description: String) {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                val req = WorkoutRequest(name, description)
                val editing = state.editingWorkout
                if (editing != null) api.updateWorkout(editing.id, req) else api.createWorkout(req)
                state = state.copy(isLoading = false, showDialog = false, editingWorkout = null)
                load()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        scope.launch {
            state = state.copy(isLoading = true, showDeleteConfirm = null)
            try {
                api.deleteWorkout(id)
                load()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
