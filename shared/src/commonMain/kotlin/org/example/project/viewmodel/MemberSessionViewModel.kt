package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class MemberSessionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val enrolledLessonIds: Set<Int> = emptySet(),
    val waitlistLessonIds: Set<Int> = emptySet(),
    val enrolledLessons: List<Lesson> = emptyList(),
    val memberInfo: Member? = null
)

class MemberSessionViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(MemberSessionState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadEnrollments(memberId: Int) {
        scope.launch {
            state = state.copy(isLoading = true)
            try {
                val lessons = api.getMemberEnrollments(memberId)
                state = state.copy(
                    enrolledLessons = lessons,
                    enrolledLessonIds = lessons.map { it.id }.toSet(),
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadMemberInfo(memberId: Int) {
        scope.launch {
            try {
                val member = api.getMember(memberId)
                state = state.copy(memberInfo = member)
            } catch (e: Exception) { }
        }
    }

    fun reserve(lessonId: Int, memberId: Int, onRefresh: () -> Unit = {}) {
        scope.launch {
            try {
                api.reserveLesson(lessonId, memberId)
                val ids = state.enrolledLessonIds + lessonId
                state = state.copy(enrolledLessonIds = ids, error = null)
                loadEnrollments(memberId)
                onRefresh()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun cancel(lessonId: Int, memberId: Int, onRefresh: () -> Unit = {}) {
        scope.launch {
            try {
                api.cancelReservation(lessonId, memberId)
                val ids = state.enrolledLessonIds - lessonId
                state = state.copy(enrolledLessonIds = ids, error = null)
                loadEnrollments(memberId)
                onRefresh()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun joinWaitlist(lessonId: Int, memberId: Int) {
        scope.launch {
            try {
                api.joinWaitlist(lessonId, memberId)
                val ids = state.waitlistLessonIds + lessonId
                state = state.copy(waitlistLessonIds = ids, error = null)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun leaveWaitlist(lessonId: Int, memberId: Int) {
        scope.launch {
            try {
                api.leaveWaitlist(lessonId, memberId)
                val ids = state.waitlistLessonIds - lessonId
                state = state.copy(waitlistLessonIds = ids, error = null)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    fun dismissError() { state = state.copy(error = null) }
}
