package org.example.project.api

import org.example.project.model.*

interface SportClubApiService {
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun registerMember(request: RegistrationRequest): LoginResponse

    suspend fun getWorkouts(): List<Workout>
    suspend fun createWorkout(request: WorkoutRequest): Workout
    suspend fun updateWorkout(id: String, request: WorkoutRequest): Workout
    suspend fun deleteWorkout(id: String)

    suspend fun getInstructors(): List<Instructor>
    suspend fun createInstructor(request: InstructorRequest): Instructor
    suspend fun updateInstructor(id: String, request: InstructorRequest): Instructor
    suspend fun deleteInstructor(id: String)

    suspend fun getLocations(): List<Location>

    suspend fun getLessons(startDate: String, endDate: String): List<Lesson>
    suspend fun createLesson(request: LessonRequest): List<Lesson>
    suspend fun updateLesson(id: String, request: LessonRequest): Lesson
    suspend fun deleteLesson(id: String)

    suspend fun getMembers(): List<Member>
    suspend fun getMember(id: String): Member
    suspend fun updateProfile(username: String?, profileImageUrl: String?): Member

    suspend fun getMembershipPrices(): List<MembershipPrice>
    suspend fun subscribeMembership(type: MembershipType, billingCycle: BillingCycle, startDate: String): Membership
    suspend fun upgradeMembership(membershipId: String): Membership

    suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy>
    suspend fun getDashboardStats(): DashboardStats

    // Member operations
    suspend fun getMemberEnrollments(memberId: String): List<Lesson>
    suspend fun reserveLesson(lessonId: String, memberId: String)
    suspend fun cancelReservation(lessonId: String, memberId: String)
    suspend fun joinWaitlist(lessonId: String, memberId: String)
    suspend fun leaveWaitlist(lessonId: String, memberId: String)
    suspend fun getMyWaitlist(memberId: String): List<String>
}
