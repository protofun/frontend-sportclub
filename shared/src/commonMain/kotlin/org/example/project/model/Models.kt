package org.example.project.model

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------------------------
// Enums
//
// The backend serializes its Kotlin enums using their declared (PascalCase) names, e.g.
// MembershipType.Basic -> "Basic". To keep the UI-facing enums in the existing UPPER_CASE style
// (used throughout the screens for `.name` display and comparisons) we keep our own enum names
// and convert to/from the backend's wire format with the helper functions below instead of
// relying on kotlinx.serialization's default enum (de)serialization. This avoids hard crashes
// if the exact backend casing differs slightly from our guess.
// ---------------------------------------------------------------------------------------------

@Serializable
enum class UserRole { MEMBER, INSTRUCTOR, STAFF }

fun userRoleFromBackend(value: String?): UserRole = when {
    value == null -> UserRole.MEMBER
    value.equals("Employee", ignoreCase = true) || value.equals("Staff", ignoreCase = true) -> UserRole.STAFF
    value.equals("Instructor", ignoreCase = true) -> UserRole.INSTRUCTOR
    else -> UserRole.MEMBER
}

@Serializable
enum class LocationType { GROUP_INDOOR, OUTDOOR, SPINNING }

@Serializable
enum class MembershipType { BASIC, UNLIMITED }

fun membershipTypeFromBackend(value: String?): MembershipType = when {
    value.equals("Unlimited", ignoreCase = true) -> MembershipType.UNLIMITED
    else -> MembershipType.BASIC
}

fun MembershipType.toBackend(): String = when (this) {
    MembershipType.BASIC -> "Basic"
    MembershipType.UNLIMITED -> "Unlimited"
}

@Serializable
enum class BillingCycle { MONTHLY, YEARLY }

fun billingCycleFromBackend(value: String?): BillingCycle = when {
    value.equals("Yearly", ignoreCase = true) -> BillingCycle.YEARLY
    else -> BillingCycle.MONTHLY
}

fun BillingCycle.toBackend(): String = when (this) {
    BillingCycle.MONTHLY -> "Monthly"
    BillingCycle.YEARLY -> "Yearly"
}

@Serializable
enum class MembershipStatus { ACTIVE, EXPIRED, CANCELLED, PENDING }

fun membershipStatusFromBackend(value: String?): MembershipStatus = when {
    value.equals("Expired", ignoreCase = true) -> MembershipStatus.EXPIRED
    value.equals("Cancelled", ignoreCase = true) || value.equals("Canceled", ignoreCase = true) -> MembershipStatus.CANCELLED
    value.equals("Pending", ignoreCase = true) -> MembershipStatus.PENDING
    else -> MembershipStatus.ACTIVE
}

@Serializable
enum class EnrollmentStatus { ENROLLED, WAITLISTED, ATTENDED, CANCELLED }

fun enrollmentStatusFromBackend(value: String?): EnrollmentStatus = when {
    value.equals("Waitlisted", ignoreCase = true) -> EnrollmentStatus.WAITLISTED
    value.equals("Attended", ignoreCase = true) -> EnrollmentStatus.ATTENDED
    value.equals("Cancelled", ignoreCase = true) || value.equals("Canceled", ignoreCase = true) -> EnrollmentStatus.CANCELLED
    else -> EnrollmentStatus.ENROLLED
}

@Serializable
enum class RecurrenceType { NONE, DAILY, WEEKLY }

// ---------------------------------------------------------------------------------------------
// Domain models (UI-facing)
// ---------------------------------------------------------------------------------------------

@Serializable
data class Workout(
    val id: String,
    val name: String,
    val description: String
)

// NOTE: The backend has no /locations endpoint and Lesson only carries a locationId (UUID).
// This static reference list is used as a placeholder for the admin "location" picker until
// the backend exposes real locations. The ids below are NOT guaranteed to exist in the
// backend database.
@Serializable
data class Location(
    val id: String,
    val name: String,
    val type: LocationType,
    val capacity: Int
)

object ReferenceLocations {
    val all: List<Location> = listOf(
        Location("00000000-0000-0000-0000-000000000001", "Hall 1", LocationType.GROUP_INDOOR, 42),
        Location("00000000-0000-0000-0000-000000000002", "Hall 2", LocationType.GROUP_INDOOR, 32),
        Location("00000000-0000-0000-0000-000000000003", "Hall 3", LocationType.GROUP_INDOOR, 24),
        Location("00000000-0000-0000-0000-000000000004", "Outdoor Area", LocationType.OUTDOOR, 20),
        Location("00000000-0000-0000-0000-000000000005", "Spinning Room", LocationType.SPINNING, 24)
    )
}

@Serializable
data class Instructor(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val photoUrl: String? = null,
    val specialties: List<String> = emptyList()
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

@Serializable
data class MembershipPrice(
    val type: MembershipType,
    val billingCycle: BillingCycle,
    val priceInCents: Int
) {
    val priceEuros: Double get() = priceInCents / 100.0
    val name: String get() = when (type) {
        MembershipType.BASIC -> "Basic"
        MembershipType.UNLIMITED -> "Unlimited"
    } + " – " + when (billingCycle) {
        BillingCycle.MONTHLY -> "Monthly"
        BillingCycle.YEARLY -> "Yearly"
    }
    val description: String get() = when (type) {
        MembershipType.BASIC -> "Work out up to 2 times per week"
        MembershipType.UNLIMITED -> "Unlimited access to all classes"
    }
}

@Serializable
data class Membership(
    val id: String,
    val userId: String,
    val type: MembershipType,
    val billingCycle: BillingCycle,
    val startDate: String,
    val endDate: String?,
    val status: MembershipStatus
) {
    val planName: String get() = when (type) {
        MembershipType.BASIC -> "Basic"
        MembershipType.UNLIMITED -> "Unlimited"
    } + " – " + when (billingCycle) {
        BillingCycle.MONTHLY -> "Monthly"
        BillingCycle.YEARLY -> "Yearly"
    }
}

@Serializable
data class Member(
    val id: String,
    val email: String,
    val username: String? = null,
    val profileImageUrl: String? = null,
    val role: UserRole = UserRole.MEMBER,
    val activeMembership: Membership? = null
) {
    val fullName: String get() = username?.takeIf { it.isNotBlank() } ?: email
    val firstName: String get() = fullName.split(" ").firstOrNull() ?: fullName
    // Backend user model has no phone number field yet.
    val phone: String get() = ""
}

@Serializable
data class Lesson(
    val id: String,
    val workoutId: String,
    val workoutName: String = "",
    val instructorId: String? = null,
    val instructorName: String? = null,
    val locationId: String = "",
    val locationName: String = "",
    val locationType: LocationType? = null,
    val startTime: String,
    val durationMinutes: Int,
    val maxCapacity: Int,
    val enrolledCount: Int = 0,
    val waitlistCount: Int = 0
) {
    val availableSpots: Int get() = maxCapacity - enrolledCount
    val isFull: Boolean get() = enrolledCount >= maxCapacity
}

@Serializable
data class MemberEnrollment(
    val memberId: String,
    val memberName: String,
    val status: EnrollmentStatus,
    val checkedIn: Boolean = false
)

@Serializable
data class LessonOccupancy(
    val lesson: Lesson,
    val enrolledMembers: List<MemberEnrollment> = emptyList()
)

@Serializable
data class DashboardStats(
    val totalMembers: Int,
    val activeMembers: Int,
    val lessonsToday: Int,
    val enrollmentsToday: Int
)

// ---------------------------------------------------------------------------------------------
// Request / response models
// ---------------------------------------------------------------------------------------------

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val role: UserRole,
    val fullName: String,
    val email: String
)

@Serializable
data class WorkoutRequest(
    val name: String,
    val description: String
)

// Instructor management has no backend support yet (no /instructors endpoints, no way to
// create users with the Instructor role). Kept for UI compatibility; RealApiService will
// throw on create/update/delete.
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
    val workoutId: String,
    val instructorId: String? = null,
    val locationId: String,
    val startTime: String,
    val durationMinutes: Int,
    val capacity: Int,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val recurrenceEndDate: String? = null
)

@Serializable
data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val membershipType: MembershipType,
    val billingCycle: BillingCycle,
    val startDate: String
)

@Serializable
data class ReservationRequest(val lessonId: String, val bikeId: String? = null)

@Serializable
data class UpdateProfileRequest(val username: String? = null, val profileImageUrl: String? = null)
