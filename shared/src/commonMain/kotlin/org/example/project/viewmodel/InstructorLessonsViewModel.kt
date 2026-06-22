package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

// InstructorLessonsState holds the UI state for the instructor's lesson overview.
data class InstructorLessonsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lessons: List<Lesson> = emptyList(),
    val selectedLessonId: String? = null,
    val isLoadingRoster: Boolean = false,
    val roster: List<LessonRosterEntry> = emptyList()
)

// InstructorLessonsViewModel manages loading lessons and toggling the participant list
class InstructorLessonsViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(InstructorLessonsState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Load all lessons for the logged-in instructor, sorted by start time
    fun loadMyLessons() {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val lessons = api.getMyLessons().sortedBy { it.startTime }
                state = state.copy(isLoading = false, lessons = lessons)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, error = e.message)
            }
        }
    }

    // Select a lesson to show its participant list
    // Tapping the same lesson a second time collapses the card again
    fun selectLesson(lessonId: String) {
        if (state.selectedLessonId == lessonId) {
            state = state.copy(selectedLessonId = null, roster = emptyList())
            return
        }
        state = state.copy(selectedLessonId = lessonId, roster = emptyList(), isLoadingRoster = true, error = null)
        scope.launch {
            try {
                val roster = api.getLessonRoster(lessonId)
                state = state.copy(isLoadingRoster = false, roster = roster)
            } catch (e: Exception) {
                state = state.copy(isLoadingRoster = false, error = e.message)
            }
        }
    }

    fun dismissError() { state = state.copy(error = null) }
}
