package org.example.project.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.model.*

// MemberSessionState holds all UI state related to the member's session
// enrollments, waitlist, profile data and loading flags
data class MemberSessionState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val enrolledLessonIds: Set<String> = emptySet(),
    val waitlistLessonIds: Set<String> = emptySet(),
    val enrolledLessons: List<Lesson> = emptyList(),
    val memberInfo: Member? = null,
    val isSavingProfile: Boolean = false,
    val notifications: List<AppNotification> = emptyList(),
    val upcomingMembership: Membership? = null
)

// MemberSessionViewModel manages all actions a member can perform
class MemberSessionViewModel(private val api: SportClubApiService) {
    var state by mutableStateOf(MemberSessionState())
        private set

    // SupervisorJob ensures that if one coroutine fails, the others are not cancelled
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Load all lessons the member is enrolled in
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

    // Load only the lesson IDs on the waitlist
    fun loadWaitlist(memberId: String) {
        scope.launch {
            try {
                val lessonIds = api.getMyWaitlist(memberId)
                state = state.copy(waitlistLessonIds = lessonIds.toSet())
            } catch (e: Exception) { }
        }
    }

    // Load the full profile of the member + any upcoming (pending) membership
    fun loadMemberInfo(memberId: String) {
        scope.launch {
            try {
                val member = api.getMember(memberId)
                val upcoming = api.getUpcomingMembership()
                state = state.copy(memberInfo = member, upcomingMembership = upcoming)
            } catch (e: Exception) { }
        }
    }

    // Enrol the member in a lesson. bikeId is optional
    // After success the lesson list is refreshed via onRefresh()
    fun reserve(lessonId: String, memberId: String, bikeId: String? = null, onRefresh: () -> Unit = {}) {
        scope.launch {
            try {
                api.reserveLesson(lessonId, memberId, bikeId)
                val ids = state.enrolledLessonIds + lessonId
                state = state.copy(enrolledLessonIds = ids, error = null)
                loadEnrollments(memberId)
                onRefresh()
            } catch (e: Exception) {
                state = state.copy(error = e.message)
            }
        }
    }

    // Cancel the member's enrolment in a lesson
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

    // Add the member to the waitlist for a full lesson
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

    // Remove the member from the waitlist
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

    // Save a new username and/or profile picture via the API
    // onDone(true) = success, onDone(false) = failure
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

    // Take out a new membership (Basic or Unlimited, monthly or yearly)
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

    // Upgrade a Basic membership to Unlimited
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

    // Cancel a monthly membership
    fun cancelMembership(membershipId: String, memberId: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            try {
                api.cancelMembership(membershipId)
                loadMemberInfo(memberId)
                state = state.copy(error = null)
                onDone(true)
            } catch (e: Exception) {
                state = state.copy(error = e.message)
                onDone(false)
            }
        }
    }

    // Load in-app notifications for this member
    fun loadNotifications() {
        scope.launch {
            try {
                val list = api.getNotifications()
                state = state.copy(notifications = list)
            } catch (e: Exception) { }
        }
    }

    // Mark a single notification as read
    fun markNotificationRead(id: String) {
        scope.launch {
            try {
                api.markNotificationRead(id)
                state = state.copy(
                    notifications = state.notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                )
            } catch (e: Exception) { }
        }
    }

    fun dismissError() { state = state.copy(error = null) }
}
