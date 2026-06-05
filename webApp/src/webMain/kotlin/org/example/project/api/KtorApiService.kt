package org.example.project.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.model.*

// TODO: Set the actual backend base URL when API routes are available
const val BASE_URL = "http://localhost:8080/api"

class KtorApiService : SportClubApiService {

    private var authToken: String? = null

    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    fun setToken(token: String?) { authToken = token }

    private fun HttpRequestBuilder.applyAuth() {
        authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        contentType(ContentType.Application.Json)
    }

    // TODO: Replace endpoint paths with actual routes from backend
    override suspend fun login(request: LoginRequest): LoginResponse =
        client.post("$BASE_URL/auth/login") { applyAuth(); setBody(request) }.body<LoginResponse>()
            .also { setToken(it.token) }

    override suspend fun getWorkouts(): List<Workout> =
        client.get("$BASE_URL/workouts") { applyAuth() }.body()

    override suspend fun createWorkout(request: WorkoutRequest): Workout =
        client.post("$BASE_URL/workouts") { applyAuth(); setBody(request) }.body()

    override suspend fun updateWorkout(id: Int, request: WorkoutRequest): Workout =
        client.put("$BASE_URL/workouts/$id") { applyAuth(); setBody(request) }.body()

    override suspend fun deleteWorkout(id: Int) {
        client.delete("$BASE_URL/workouts/$id") { applyAuth() }
    }

    override suspend fun getInstructors(): List<Instructor> =
        client.get("$BASE_URL/instructors") { applyAuth() }.body()

    override suspend fun createInstructor(request: InstructorRequest): Instructor =
        client.post("$BASE_URL/instructors") { applyAuth(); setBody(request) }.body()

    override suspend fun updateInstructor(id: Int, request: InstructorRequest): Instructor =
        client.put("$BASE_URL/instructors/$id") { applyAuth(); setBody(request) }.body()

    override suspend fun deleteInstructor(id: Int) {
        client.delete("$BASE_URL/instructors/$id") { applyAuth() }
    }

    override suspend fun getLocations(): List<Location> =
        client.get("$BASE_URL/locations") { applyAuth() }.body()

    override suspend fun getLessons(startDate: String, endDate: String): List<Lesson> =
        client.get("$BASE_URL/lessons") {
            applyAuth()
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()

    override suspend fun createLesson(request: LessonRequest): List<Lesson> =
        client.post("$BASE_URL/lessons") { applyAuth(); setBody(request) }.body()

    override suspend fun updateLesson(id: Int, request: LessonRequest): Lesson =
        client.put("$BASE_URL/lessons/$id") { applyAuth(); setBody(request) }.body()

    override suspend fun deleteLesson(id: Int) {
        client.delete("$BASE_URL/lessons/$id") { applyAuth() }
    }

    override suspend fun getMembers(): List<Member> =
        client.get("$BASE_URL/members") { applyAuth() }.body()

    override suspend fun getMember(id: Int): Member =
        client.get("$BASE_URL/members/$id") { applyAuth() }.body()

    override suspend fun getSubscriptionPlans(): List<SubscriptionPlan> =
        client.get("$BASE_URL/subscriptions/plans").body()

    override suspend fun registerMember(request: RegistrationRequest): Member =
        client.post("$BASE_URL/members/register") { applyAuth(); setBody(request) }.body()

    override suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy> =
        client.get("$BASE_URL/lessons/occupancy") {
            applyAuth()
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()

    override suspend fun getDashboardStats(): DashboardStats =
        client.get("$BASE_URL/admin/stats") { applyAuth() }.body()

    // TODO: Replace with actual backend routes
    override suspend fun getMemberEnrollments(memberId: Int): List<Lesson> =
        client.get("$BASE_URL/members/$memberId/enrollments") { applyAuth() }.body()

    override suspend fun reserveLesson(lessonId: Int, memberId: Int): EnrollmentStatus =
        client.post("$BASE_URL/lessons/$lessonId/reserve") {
            applyAuth(); parameter("memberId", memberId)
        }.body()

    override suspend fun cancelReservation(lessonId: Int, memberId: Int) {
        client.delete("$BASE_URL/lessons/$lessonId/reserve") {
            applyAuth(); parameter("memberId", memberId)
        }
    }

    override suspend fun joinWaitlist(lessonId: Int, memberId: Int): EnrollmentStatus =
        client.post("$BASE_URL/lessons/$lessonId/waitlist") {
            applyAuth(); parameter("memberId", memberId)
        }.body()

    override suspend fun leaveWaitlist(lessonId: Int, memberId: Int) {
        client.delete("$BASE_URL/lessons/$lessonId/waitlist") {
            applyAuth(); parameter("memberId", memberId)
        }
    }
}
