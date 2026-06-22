package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*
import org.example.project.util.todayDateString

// State for ScheduleScreen and the "Today's Classes" section on HomeScreen.
data class ScheduleState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lessons: List<Lesson> = emptyList(),
    val workouts: List<Workout> = emptyList(),
    val selectedDate: String = todayDateString(),
    val filterWorkoutId: String? = null           // null = show all workout types
) {
    // applies the active filter without an extra API call.
    val filteredLessons: List<Lesson>
        get() = if (filterWorkoutId == null) lessons else lessons.filter { it.workoutId == filterWorkoutId }
}

class ScheduleViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(ScheduleState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Fetches lessons and workout types for the given date.
    // Called by ScheduleScreen on open
    fun load(date: String = state.selectedDate) {
        scope.launch {
            state = state.copy(isLoading = true, error = null, selectedDate = date)
            try {
                // Both calls run sequentially
                val lessons  = api.getLessons(date, date)  // GET /lessons?from=...&to=...
                val workouts = api.getWorkouts()            // GET /workouts
                state = state.copy(isLoading = false, lessons = lessons, workouts = workouts)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Moves to the previous calendar day and reloads
    // because Kotlin/Wasm doesn't have java.time available in the browser.
    fun previousDay() {
        val parts = state.selectedDate.split("-")
        val day = parts[2].toInt() - 1
        val newDate = if (day > 0) {
            "${parts[0]}-${parts[1]}-${day.toString().padStart(2, '0')}"
        } else {
            val month = parts[1].toInt() - 1
            val year = if (month == 0) parts[0].toInt() - 1 else parts[0].toInt()
            val newMonth = if (month == 0) 12 else month
            val daysInPrevMonth = listOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            val newDay = daysInPrevMonth[newMonth]
            "$year-${newMonth.toString().padStart(2, '0')}-${newDay.toString().padStart(2, '0')}"
        }
        load(newDate)
    }

    // Moves to the next calendar day and reloads.
    fun nextDay() {
        val parts = state.selectedDate.split("-")
        val daysInMonth = listOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val month = parts[1].toInt()
        val day = parts[2].toInt() + 1
        val newDate = if (day <= daysInMonth[month]) {
            "${parts[0]}-${parts[1]}-${day.toString().padStart(2, '0')}"
        } else {
            val newMonth = if (month == 12) 1 else month + 1
            val newYear  = if (month == 12) parts[0].toInt() + 1 else parts[0].toInt()
            "$newYear-${newMonth.toString().padStart(2, '0')}-01"
        }
        load(newDate)
    }

    // Sets the active workout filter
    fun setFilter(workoutId: String?) { state = state.copy(filterWorkoutId = workoutId) }

    // Gets the available bikes
    suspend fun getAvailableBikes(lessonId: String) = api.getAvailableBikes(lessonId)
}
