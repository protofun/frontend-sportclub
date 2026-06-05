package org.example.project.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { MEMBER, INSTRUCTOR, STAFF }

@Serializable
enum class LocationType { GROUP_INDOOR, OUTDOOR, SPINNING }

@Serializable
enum class SubscriptionType { TWO_PER_WEEK, UNLIMITED }

@Serializable
enum class BillingCycle { MONTHLY, YEARLY }

@Serializable
enum class SubscriptionStatus { ACTIVE, EXPIRED, CANCELLED, PENDING }

@Serializable
enum class EnrollmentStatus { ENROLLED, WAITLISTED, ATTENDED, CANCELLED }

@Serializable
enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY }

@Serializable
data class Workout(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String? = null
)

@Serializable
data class Location(
    val id: Int,
    val name: String,
    val type: LocationType,
    val capacity: Int
)

@Serializable
data class Instructor(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String? = null,
    val specialties: List<String> = emptyList()
) {
    val fullName: String get() = "$firstName $lastName"
}

@Serializable
data class SubscriptionPlan(
    val id: Int,
    val type: SubscriptionType,
    val billingCycle: BillingCycle,
    val priceEuros: Double,
    val name: String,
    val description: String
)

@Serializable
data class MemberSubscription(
    val id: Int,
    val planId: Int,
    val planName: String,
    val type: SubscriptionType,
    val billingCycle: BillingCycle,
    val startDate: String,
    val endDate: String?,
    val status: SubscriptionStatus
)

@Serializable
data class Member(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val photoUrl: String? = null,
    val activeSubscription: MemberSubscription? = null
) {
    val fullName: String get() = "$firstName $lastName"
}

@Serializable
data class Lesson(
    val id: Int,
    val workoutId: Int,
    val workoutName: String,
    val instructorId: Int?,
    val instructorName: String?,
    val locationId: Int,
    val locationName: String,
    val locationType: LocationType,
    val startTime: String,
    val durationMinutes: Int,
    val maxCapacity: Int,
    val enrolledCount: Int,
    val waitlistCount: Int = 0
) {
    val availableSpots: Int get() = maxCapacity - enrolledCount
    val isFull: Boolean get() = enrolledCount >= maxCapacity
}

@Serializable
data class SpinningBike(val row: Int, val seat: Int)

@Serializable
data class MemberEnrollment(
    val memberId: Int,
    val memberName: String,
    val status: EnrollmentStatus,
    val checkedIn: Boolean = false
)

@Serializable
data class LessonOccupancy(
    val lesson: Lesson,
    val enrolledMembers: List<MemberEnrollment>
)

@Serializable
data class DashboardStats(
    val totalMembers: Int,
    val activeMembers: Int,
    val lessonsToday: Int,
    val enrollmentsToday: Int
)

// Request models
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: Int,
    val role: UserRole,
    val fullName: String,
    val email: String
)

@Serializable
data class WorkoutRequest(
    val name: String,
    val description: String,
    val imageUrl: String? = null
)

@Serializable
data class InstructorRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String? = null,
    val specialties: List<String> = emptyList()
)

@Serializable
data class LessonRequest(
    val workoutId: Int,
    val instructorId: Int?,
    val locationId: Int,
    val startTime: String,
    val durationMinutes: Int,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndDate: String? = null,
    val recurrenceCount: Int? = null
)

@Serializable
data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val subscriptionPlanId: Int,
    val startDate: String
)
