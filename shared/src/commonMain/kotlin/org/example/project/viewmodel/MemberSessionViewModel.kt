package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

data class MemberSessionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val enrolledLessonIds: Set<String> = emptySet(),
    val waitlistLessonIds: Set<String> = emptySet(),
    val enrolledLessons: List<Lesson> = emptyList(),
    val memberInfo: Member? = null,
    val isSavingProfile: Boolean = false
)

class MemberSessionViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(MemberSessionState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadEnrollments(memberId: String) {
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

    fun loadWaitlist(memberId: String) {
        scope.launch {
            try {
                val lessonIds = api.getMyWaitlist(memberId)
                state = state.copy(waitlistLessonIds = lessonIds.toSet())
            } catch (e: Exception) { }
        }
    }

    fun loadMemberInfo(memberId: String) {
        scope.launch {
            try {
                val member = api.getMember(memberId)
                state = state.copy(memberInfo = member)
            } catch (e: Exception) { }
        }
    }

    fun reserve(lessonId: String, memberId: String, onRefresh: () -> Unit = {}) {
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

    fun cancel(lessonId: String, memberId: String, onRefresh: () -> Unit = {}) {
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

    fun joinWaitlist(lessonId: String, memberId: String) {
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

    fun leaveWaitlist(lessonId: String, memberId: String) {
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

    fun updateProfile(username: String, profileImageUrl: String?, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            state = state.copy(isSavingProfile = true)
            try {
                val updated = api.updateProfile(username, profileImageUrl)
                state = state.copy(memberInfo = updated, isSavingProfile = false, error = null)
                onDone(true)
            } catch (e: Exception) {
                state = state.copy(isSavingProfile = false, error = e.message)
                onDone(false)
            }
        }
    }

    fun subscribe(memberId: String, type: MembershipType, billingCycle: BillingCycle, startDate: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            try {
                api.subscribeMembership(type, billingCycle, startDate)
                loadMemberInfo(memberId)
                state = state.copy(error = null)
                onDone(true)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
                onDone(false)
            }
        }
    }

    fun upgrade(memberId: String, membershipId: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            try {
                api.upgradeMembership(membershipId)
                loadMemberInfo(memberId)
                state = state.copy(error = null)
                onDone(true)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
                onDone(false)
            }
        }
    }

    fun dismissError() { state = state.copy(error = null) }
}
