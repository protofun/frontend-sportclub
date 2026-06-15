package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*
import org.example.project.util.todayDateString

data class LessonManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lessons: List<Lesson> = emptyList(),
    val workouts: List<Workout> = emptyList(),
    val instructors: List<Instructor> = emptyList(),
    val locations: List<Location> = emptyList(),
    val showDialog: Boolean = false,
    val editingLesson: Lesson? = null,
    val showDeleteConfirm: String? = null,
    val selectedWeekStart: String = todayDateString()
)

class LessonViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(LessonManagementState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadAll(weekStart: String = state.selectedWeekStart) {
        scope.launch {
            state = state.copy(isLoading = true, error = null, selectedWeekStart = weekStart)
            try {
                val weekEnd = weekStart // simplified; real impl adds 6 days
                val (lessons, workouts, instructors, locations) = listOf(
                    async { api.getLessons(weekStart, addDays(weekStart, 6)) },
                    async { api.getWorkouts() },
                    async { api.getInstructors() },
                    async { api.getLocations() }
                ).let { (a, b, c, d) ->
                    listOf(a.await(), b.await(), c.await(), d.await())
                }
                @Suppress("UNCHECKED_CAST")
                state = state.copy(
                    isLoading = false,
                    lessons = lessons as List<Lesson>,
                    workouts = workouts as List<Workout>,
                    instructors = instructors as List<Instructor>,
                    locations = locations as List<Location>
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun addDays(date: String, days: Int): String {
        // Simple date arithmetic for ISO dates (YYYY-MM-DD)
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt() + days
        val daysInMonth = listOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        return if (day <= daysInMonth[month]) {
            "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        } else {
            val newDay = day - daysInMonth[month]
            val newMonth = if (month == 12) 1 else month + 1
            val newYear = if (month == 12) year + 1 else year
            "$newYear-${newMonth.toString().padStart(2, '0')}-${newDay.toString().padStart(2, '0')}"
        }
    }

    fun showAddDialog() { state = state.copy(showDialog = true, editingLesson = null) }
    fun showEditDialog(lesson: Lesson) { state = state.copy(showDialog = true, editingLesson = lesson) }
    fun dismissDialog() { state = state.copy(showDialog = false, editingLesson = null) }
    fun showDeleteConfirm(id: String) { state = state.copy(showDeleteConfirm = id) }
    fun dismissDeleteConfirm() { state = state.copy(showDeleteConfirm = null) }

    fun save(
        workoutId: String, instructorId: String?, locationId: String,
        startTime: String, durationMinutes: Int, capacity: Int,
        recurrence: RecurrenceType, recurrenceEndDate: String?
    ) {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                val req = LessonRequest(workoutId, instructorId, locationId, startTime, durationMinutes, capacity, recurrence, recurrenceEndDate)
                val editing = state.editingLesson
                if (editing != null) api.updateLesson(editing.id, req) else api.createLesson(req)
                state = state.copy(isLoading = false, showDialog = false, editingLesson = null)
                loadAll()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun delete(id: String) {
        scope.launch {
            state = state.copy(isLoading = true, showDeleteConfirm = null)
            try {
                api.deleteLesson(id)
                loadAll()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun previousWeek() {
        val parts = state.selectedWeekStart.split("-")
        val day = parts[2].toInt() - 7
        val newDate = if (day > 0) {
            "${parts[0]}-${parts[1]}-${day.toString().padStart(2, '0')}"
        } else {
            val month = parts[1].toInt() - 1
            val year = if (month == 0) parts[0].toInt() - 1 else parts[0].toInt()
            val newMonth = if (month == 0) 12 else month
            val daysInPrevMonth = listOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val newDay = daysInPrevMonth[newMonth] + day
            "$year-${newMonth.toString().padStart(2, '0')}-${newDay.toString().padStart(2, '0')}"
        }
        loadAll(newDate)
    }

    fun nextWeek() {
        loadAll(addDays(state.selectedWeekStart, 7))
    }

    fun clearError() { state = state.copy(error = null) }
}
