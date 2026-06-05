package org.example.project.api

import org.example.project.model.*

interface SportClubApiService {
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun getWorkouts(): List<Workout>
    suspend fun createWorkout(request: WorkoutRequest): Workout
    suspend fun updateWorkout(id: Int, request: WorkoutRequest): Workout
    suspend fun deleteWorkout(id: Int)
    suspend fun getInstructors(): List<Instructor>
    suspend fun createInstructor(request: InstructorRequest): Instructor
    suspend fun updateInstructor(id: Int, request: InstructorRequest): Instructor
    suspend fun deleteInstructor(id: Int)
    suspend fun getLocations(): List<Location>
    suspend fun getLessons(startDate: String, endDate: String): List<Lesson>
    suspend fun createLesson(request: LessonRequest): List<Lesson>
    suspend fun updateLesson(id: Int, request: LessonRequest): Lesson
    suspend fun deleteLesson(id: Int)
    suspend fun getMembers(): List<Member>
    suspend fun getMember(id: Int): Member
    suspend fun getSubscriptionPlans(): List<SubscriptionPlan>
    suspend fun registerMember(request: RegistrationRequest): Member
    suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy>
    suspend fun getDashboardStats(): DashboardStats
    // Member operations
    suspend fun getMemberEnrollments(memberId: Int): List<Lesson>
    suspend fun reserveLesson(lessonId: Int, memberId: Int): EnrollmentStatus
    suspend fun cancelReservation(lessonId: Int, memberId: Int)
    suspend fun joinWaitlist(lessonId: Int, memberId: Int): EnrollmentStatus
    suspend fun leaveWaitlist(lessonId: Int, memberId: Int)
}
