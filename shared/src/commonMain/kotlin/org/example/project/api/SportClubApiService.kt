package org.example.project.api

import org.example.project.model.*

// Contract for all backend communication. Every function maps to one HTTP endpoint.
interface SportClubApiService {

    // Auth
    suspend fun login(request: LoginRequest): LoginResponse            // POST /auth/login
    suspend fun registerMember(request: RegistrationRequest): LoginResponse // POST /auth/register
    fun logout()                                                        // clears the auth token (local only)
    fun restoreSession(token: String, userId: String) {}               // re-injects a saved token

    // Workouts
    suspend fun getWorkouts(): List<Workout>                            // GET    /workouts
    suspend fun createWorkout(request: WorkoutRequest): Workout         // POST   /workouts
    suspend fun updateWorkout(id: String, request: WorkoutRequest): Workout // PUT /workouts/{id}
    suspend fun deleteWorkout(id: String)                               // DELETE /workouts/{id}

    // Instructors
    suspend fun getInstructors(): List<Instructor>                      // GET    /users  (filtered by role)
    suspend fun createInstructor(request: InstructorRequest): Instructor // POST  /users/instructors
    suspend fun updateInstructor(id: String, request: InstructorRequest): Instructor
    suspend fun deleteInstructor(id: String)

    // Locations
    suspend fun getLocations(): List<Location>

    // Lessons
    suspend fun getLessons(startDate: String, endDate: String): List<Lesson> // GET /lessons?from=...&to=...
    suspend fun createLesson(request: LessonRequest): List<Lesson>     // POST /lessons (or /lessons/recurring)
    suspend fun updateLesson(id: String, request: LessonRequest): Lesson
    suspend fun deleteLesson(id: String)

    // Members
    suspend fun getMembers(): List<Member>                              // GET /users  (filtered by role)
    suspend fun getMember(id: String): Member
    suspend fun updateProfile(username: String?, profileImageUrl: String?): Member // PUT /users/me

    // Memberships
    suspend fun getMembershipPrices(): List<MembershipPrice>            // GET /memberships/prices
    suspend fun subscribeMembership(type: MembershipType, billingCycle: BillingCycle, startDate: String): Membership
    suspend fun upgradeMembership(membershipId: String): Membership
    suspend fun cancelMembership(membershipId: String): Membership      // PUT /memberships/{id}/cancel
    suspend fun getUpcomingMembership(): Membership?                    // GET /memberships/me/upcoming

    // Notifications
    suspend fun getNotifications(): List<AppNotification>               // GET /notifications/me
    suspend fun markNotificationRead(id: String)                        // PUT /notifications/{id}/read

    // Admin stats
    suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy>
    suspend fun getDashboardStats(): DashboardStats

    // Member reservations
    suspend fun getMemberEnrollments(memberId: String): List<Lesson>
    suspend fun getAvailableBikes(lessonId: String): List<Bike>
    suspend fun reserveLesson(lessonId: String, memberId: String, bikeId: String? = null)
    suspend fun cancelReservation(lessonId: String, memberId: String)
    suspend fun joinWaitlist(lessonId: String, memberId: String)
    suspend fun leaveWaitlist(lessonId: String, memberId: String)
    suspend fun getMyWaitlist(memberId: String): List<String>

    // Instructor operations
    suspend fun getMyLessons(): List<Lesson>
    suspend fun getLessonRoster(lessonId: String): List<LessonRosterEntry>
}
