package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class InstructorState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val instructors: List<Instructor> = emptyList(),
    val showDialog: Boolean = false,
    val editingInstructor: Instructor? = null,
    val showDeleteConfirm: Int? = null
)

class InstructorViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(InstructorState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun load() {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                state = state.copy(isLoading = false, instructors = api.getInstructors())
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun showAddDialog() { state = state.copy(showDialog = true, editingInstructor = null) }
    fun showEditDialog(ins: Instructor) { state = state.copy(showDialog = true, editingInstructor = ins) }
    fun dismissDialog() { state = state.copy(showDialog = false, editingInstructor = null) }
    fun showDeleteConfirm(id: Int) { state = state.copy(showDeleteConfirm = id) }
    fun dismissDeleteConfirm() { state = state.copy(showDeleteConfirm = null) }

    fun save(firstName: String, lastName: String, email: String, specialties: List<String>) {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                val req = InstructorRequest(firstName, lastName, email, null, specialties)
                val editing = state.editingInstructor
                if (editing != null) api.updateInstructor(editing.id, req) else api.createInstructor(req)
                state = state.copy(isLoading = false, showDialog = false, editingInstructor = null)
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
                api.deleteInstructor(id)
                load()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
