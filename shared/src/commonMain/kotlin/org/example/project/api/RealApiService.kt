package org.example.project.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.project.model.*
import org.example.project.util.addDaysToIsoDateTime
import org.example.project.util.todayDateString

/**
 * Talks to the real Ktor backend (org.example.project.routes.*).
 *
 * Several frontend concepts have no backend equivalent yet (locations, instructor
 * management, admin membership lookups, admin occupancy member lists, admin stats).
 * These are degraded gracefully here - see the gap report for details.
 */
class RealApiService(private val baseUrl: String = defaultApiBaseUrl()) : SportClubApiService {

    private val client: HttpClient = createHttpClient()
    private var authToken: String? = null
    private var currentUserId: String? = null

    // ---- wire DTOs (match org.example.project.dto / domain on the backend) -----------------

    @Serializable
    private data class AuthResponseDto(val token: String = "", val userId: String = "", val role: String = "", val email: String = "")

    @Serializable
    private data class LoginRequestDto(val email: String, val password: String)

    @Serializable
    private data class RegisterRequestDto(
        val email: String,
        val password: String,
        val username: String? = null,
        val membershipType: String,
        val billingCycle: String,
        val membershipStartDate: String
    )

    @Serializable
    private data class UserDto(
        val id: String,
        val email: String,
        val role: String,
        val username: String? = null,
        val profileImageUrl: String? = null,
        val specialties: List<String> = emptyList()
    )

    @Serializable
    private data class InstructorRequestDto(
        val firstName: String,
        val lastName: String,
        val email: String,
        val photoUrl: String? = null,
        val specialties: List<String> = emptyList()
    )

    @Serializable
    private data class WorkoutDto(val id: String, val name: String, val description: String? = null)

    @Serializable
    private data class WorkoutRequestDto(val name: String, val description: String? = null)

    @Serializable
    private data class LessonDto(
        val id: String,
        val workoutId: String,
        val instructorId: String? = null,
        val locationId: String,
        val startTime: String,
        val durationMinutes: Int,
        val capacity: Int
    )

    @Serializable
    private data class LessonRequestDto(
        val workoutId: String,
        val instructorId: String? = null,
        val locationId: String,
        val startTime: String,
        val durationMinutes: Int,
        val capacity: Int
    )

    @Serializable
    private data class OccupancyDto(val capacity: Int = 0, val reservationCount: Int = 0)

    @Serializable
    private data class MembershipDto(
        val id: String,
        val userId: String,
        val type: String,
        val billingCycle: String,
        val startDate: String,
        val endDate: String? = null,
        val status: String
    )

    @Serializable
    private data class CreateMembershipRequestDto(val type: String, val billingCycle: String, val startDate: String)

    @Serializable
    private data class MembershipPriceDto(val type: String, val billingCycle: String, val priceInCents: Int)

    @Serializable
    private data class ReservationDto(
        val id: String,
        val lessonId: String,
        val userId: String? = null,
        val bikeId: String? = null,
        val status: String? = null
    )

    @Serializable
    private data class ReservationRequestDto(val lessonId: String, val bikeId: String? = null)

    @Serializable
    private data class WaitlistEntryDto(val position: Int, val lessonId: String)

    @Serializable
    private data class LessonRosterEntryDto(
        val reservationId: String,
        val userId: String,
        val name: String,
        val email: String,
        val status: String
    )

    @Serializable
    private data class ErrorResponseDto(val error: String? = null)

    private val errorJson = Json { ignoreUnknownKeys = true }

    // ---- low-level HTTP helpers --------------------------------------------------------------

    private fun HttpRequestBuilder.authorize() {
        authToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            val message = try {
                errorJson.decodeFromString<ErrorResponseDto>(body).error
            } catch (e: Exception) {
                null
            }
            throw Exception(message ?: body.ifBlank { "Something went wrong (${response.status.value})" })
        }
    }

    private suspend inline fun <reified T> apiGet(path: String): T {
        val response = client.get("$baseUrl$path") { authorize() }
        ensureSuccess(response)
        return response.body()
    }

    private suspend inline fun <reified TReq, reified TRes> apiPost(path: String, requestBody: TReq): TRes {
        val response = client.post("$baseUrl$path") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        ensureSuccess(response)
        return response.body()
    }

    private suspend inline fun <reified TReq> apiPostNoResponse(path: String, requestBody: TReq) {
        val response = client.post("$baseUrl$path") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        ensureSuccess(response)
    }

    private suspend inline fun <reified TReq, reified TRes> apiPut(path: String, requestBody: TReq): TRes {
        val response = client.put("$baseUrl$path") {
            authorize()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        ensureSuccess(response)
        return response.body()
    }

    private suspend inline fun <reified TRes> apiPutNoBody(path: String): TRes {
        val response = client.put("$baseUrl$path") { authorize() }
        ensureSuccess(response)
        return response.body()
    }

    private suspend fun apiDelete(path: String) {
        val response = client.delete("$baseUrl$path") { authorize() }
        ensureSuccess(response)
    }

    // ---- auth ---------------------------------------------------------------------------------

    override suspend fun login(request: LoginRequest): LoginResponse {
        val dto: AuthResponseDto = apiPost("/auth/login", LoginRequestDto(request.email, request.password))
        authToken = dto.token
        currentUserId = dto.userId
        val username = try { apiGet<UserDto>("/users/me").username } catch (e: Exception) { null }
        return LoginResponse(
            token = dto.token,
            userId = dto.userId,
            role = userRoleFromBackend(dto.role),
            fullName = username?.takeIf { it.isNotBlank() } ?: dto.email,
            email = dto.email
        )
    }

    override fun logout() {
        authToken = null
        currentUserId = null
    }

    override fun restoreSession(token: String, userId: String) {
        authToken = token
        currentUserId = userId
    }

    override suspend fun registerMember(request: RegistrationRequest): LoginResponse {
        val username = "${request.firstName} ${request.lastName}".trim().ifBlank { null }
        val body = RegisterRequestDto(
            email = request.email,
            password = request.password,
            username = username,
            membershipType = request.membershipType.toBackend(),
            billingCycle = request.billingCycle.toBackend(),
            membershipStartDate = request.startDate
        )
        val dto: AuthResponseDto = apiPost("/auth/register", body)
        authToken = dto.token
        currentUserId = dto.userId

        // The backend's /auth/register doesn't create a membership itself, so do it explicitly
        // with the plan the user picked during registration.
        try {
            apiPost<CreateMembershipRequestDto, MembershipDto>(
                "/memberships",
                CreateMembershipRequestDto(
                    type = request.membershipType.toBackend(),
                    billingCycle = request.billingCycle.toBackend(),
                    startDate = request.startDate.toStartOfDayDateTimeString()
                )
            )
        } catch (e: Exception) {
            // Non-fatal: the account was created successfully even if the membership couldn't be.
        }

        return LoginResponse(
            token = dto.token,
            userId = dto.userId,
            role = userRoleFromBackend(dto.role),
            fullName = username ?: dto.email,
            email = dto.email
        )
    }

    // ---- workouts -------------------------------------------------------------------------------

    override suspend fun getWorkouts(): List<Workout> =
        apiGet<List<WorkoutDto>>("/workouts").map { it.toWorkout() }

    override suspend fun createWorkout(request: WorkoutRequest): Workout =
        apiPost<WorkoutRequestDto, WorkoutDto>("/workouts", WorkoutRequestDto(request.name, request.description)).toWorkout()

    override suspend fun updateWorkout(id: String, request: WorkoutRequest): Workout =
        apiPut<WorkoutRequestDto, WorkoutDto>("/workouts/$id", WorkoutRequestDto(request.name, request.description)).toWorkout()

    override suspend fun deleteWorkout(id: String) = apiDelete("/workouts/$id")

    private fun WorkoutDto.toWorkout() = Workout(id, name, description ?: "")

    // ---- instructors --------------------------------------------------------------------------

    override suspend fun getInstructors(): List<Instructor> =
        apiGet<List<UserDto>>("/users")
            .filter { it.role.equals("Instructor", ignoreCase = true) }
            .map { it.toInstructor() }

    override suspend fun createInstructor(request: InstructorRequest): Instructor =
        apiPost<InstructorRequestDto, UserDto>("/users/instructors", request.toDto()).toInstructor()

    override suspend fun updateInstructor(id: String, request: InstructorRequest): Instructor =
        apiPut<InstructorRequestDto, UserDto>("/users/instructors/$id", request.toDto()).toInstructor()

    override suspend fun deleteInstructor(id: String): Unit = apiDelete("/users/$id")

    private fun InstructorRequest.toDto() = InstructorRequestDto(
        firstName   = firstName,
        lastName    = lastName,
        email       = email,
        photoUrl    = photoUrl,
        specialties = specialties
    )

    private fun UserDto.toInstructor(): Instructor {
        val name = username?.takeIf { it.isNotBlank() } ?: email
        val parts = name.trim().split(" ")
        val first = parts.first()
        val last = parts.drop(1).joinToString(" ")
        return Instructor(id, first, last, email, profileImageUrl, specialties)
    }

    // ---- locations (no backend endpoint -- static reference data) --------------------------------

    override suspend fun getLocations(): List<Location> = ReferenceLocations.all

    // ---- lessons --------------------------------------------------------------------------------

    override suspend fun getLessons(startDate: String, endDate: String): List<Lesson> =
        enrichLessons(apiGet("/lessons?from=${startDate}T00:00:00&to=${endDate}T23:59:59"))

    override suspend fun createLesson(request: LessonRequest): List<Lesson> {
        val body = request.toDto()
        val dtos: List<LessonDto> = when (request.recurrence) {
            RecurrenceType.NONE -> listOf(apiPost<LessonRequestDto, LessonDto>("/lessons", body))
            RecurrenceType.DAILY, RecurrenceType.WEEKLY -> {
                val until = request.recurrenceEndDate
                    ?: throw IllegalArgumentException("An end date is required for recurring lessons")
                val repeatEveryDays = if (request.recurrence == RecurrenceType.WEEKLY) 7 else 1
                apiPost("/lessons/recurring?repeatEveryDays=$repeatEveryDays&until=${until.toEndOfDayDateTimeString()}", body)
            }
        }
        return enrichLessons(dtos)
    }

    override suspend fun updateLesson(id: String, request: LessonRequest): Lesson {
        val updated = apiPut<LessonRequestDto, LessonDto>("/lessons/$id", request.toDto())
        when (request.recurrence) {
            RecurrenceType.NONE -> {}
            RecurrenceType.DAILY, RecurrenceType.WEEKLY -> {
                val until = request.recurrenceEndDate
                    ?: throw IllegalArgumentException("An end date is required for recurring lessons")
                val repeatEveryDays = if (request.recurrence == RecurrenceType.WEEKLY) 7 else 1
                val recurringBody = request.toDto().copy(
                    startTime = addDaysToIsoDateTime(request.startTime, repeatEveryDays)
                )
                apiPost<LessonRequestDto, List<LessonDto>>(
                    "/lessons/recurring?repeatEveryDays=$repeatEveryDays&until=${until.toEndOfDayDateTimeString()}",
                    recurringBody
                )
            }
        }
        return enrichLessons(listOf(updated)).first()
    }

    override suspend fun deleteLesson(id: String) = apiDelete("/lessons/$id")

    private fun LessonRequest.toDto() = LessonRequestDto(workoutId, instructorId, locationId, startTime, durationMinutes, capacity)

    /** Backend expects java.time.LocalDateTime.parse-able strings (no timezone). Plain dates get a time appended. */
    private fun String.toStartOfDayDateTimeString(): String = if (contains("T")) this else "${this}T00:00:00"

    private fun String.toEndOfDayDateTimeString(): String = if (contains("T")) this else "${this}T23:59:59"

    private suspend fun enrichLessons(dtos: List<LessonDto>): List<Lesson> {
        if (dtos.isEmpty()) return emptyList()
        val workoutMap = getWorkouts().associate { it.id to it.name }
        val instructorMap = try {
            getInstructors().associate { it.id to it.fullName }
        } catch (e: Exception) {
            emptyMap()
        }
        return coroutineScope {
            dtos.map { dto -> async { enrichLesson(dto, workoutMap, instructorMap) } }.awaitAll()
        }
    }

    private suspend fun enrichLesson(dto: LessonDto, workoutMap: Map<String, String>, instructorMap: Map<String, String>): Lesson {
        val occupancy = try { apiGet<OccupancyDto>("/lessons/${dto.id}/occupancy") } catch (e: Exception) { null }
        val location = ReferenceLocations.all.find { it.id == dto.locationId }
        return Lesson(
            id = dto.id,
            workoutId = dto.workoutId,
            workoutName = workoutMap[dto.workoutId] ?: "",
            instructorId = dto.instructorId,
            instructorName = dto.instructorId?.let { instructorMap[it] },
            locationId = dto.locationId,
            locationName = location?.name ?: dto.locationId.take(8),
            locationType = location?.type,
            startTime = dto.startTime,
            durationMinutes = dto.durationMinutes,
            maxCapacity = occupancy?.capacity?.takeIf { it > 0 } ?: dto.capacity,
            enrolledCount = occupancy?.reservationCount ?: 0
        )
    }

    // ---- members / users ------------------------------------------------------------------------

    override suspend fun getMembers(): List<Member> =
        apiGet<List<UserDto>>("/users")
            .filter { it.role.equals("Member", ignoreCase = true) }
            .map { it.toMember(null) }

    override suspend fun getMember(id: String): Member {
        val dto: UserDto = if (id == currentUserId) apiGet("/users/me") else apiGet("/users/$id")
        val membership = if (id == currentUserId) {
            try { apiGet<MembershipDto>("/memberships/me").toMembership() } catch (e: Exception) { null }
        } else null
        return dto.toMember(membership)
    }

    override suspend fun updateProfile(username: String?, profileImageUrl: String?): Member {
        val dto: UserDto = apiPut("/users/me", UpdateProfileRequest(username, profileImageUrl))
        val membership = try { apiGet<MembershipDto>("/memberships/me").toMembership() } catch (e: Exception) { null }
        return dto.toMember(membership)
    }

    private fun UserDto.toMember(membership: Membership?) =
        Member(id, email, username, profileImageUrl, userRoleFromBackend(role), membership)

    private fun MembershipDto.toMembership() = Membership(
        id = id,
        userId = userId,
        type = membershipTypeFromBackend(type),
        billingCycle = billingCycleFromBackend(billingCycle),
        startDate = startDate,
        endDate = endDate,
        status = membershipStatusFromBackend(status)
    )

    // ---- memberships -----------------------------------------------------------------------------

    override suspend fun getMembershipPrices(): List<MembershipPrice> =
        apiGet<List<MembershipPriceDto>>("/memberships/prices").map {
            MembershipPrice(membershipTypeFromBackend(it.type), billingCycleFromBackend(it.billingCycle), it.priceInCents)
        }

    override suspend fun subscribeMembership(type: MembershipType, billingCycle: BillingCycle, startDate: String): Membership =
        apiPost<CreateMembershipRequestDto, MembershipDto>(
            "/memberships",
            CreateMembershipRequestDto(type.toBackend(), billingCycle.toBackend(), startDate.toStartOfDayDateTimeString())
        ).toMembership()

    override suspend fun upgradeMembership(membershipId: String): Membership =
        apiPutNoBody<MembershipDto>("/memberships/$membershipId/upgrade").toMembership()

    // ---- occupancy / dashboard stats --------------------------------------------------------------

    override suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy> =
        getLessons(startDate, endDate).map { LessonOccupancy(it, emptyList()) }

    override suspend fun getDashboardStats(): DashboardStats {
        val users = try { apiGet<List<UserDto>>("/users") } catch (e: Exception) { emptyList() }
        val totalMembers = users.count { it.role.equals("Member", ignoreCase = true) }
        val today = todayDateString()
        val todaysLessons = try { getLessons(today, today) } catch (e: Exception) { emptyList() }
        return DashboardStats(
            totalMembers = totalMembers,
            activeMembers = 0,
            lessonsToday = todaysLessons.size,
            enrollmentsToday = todaysLessons.sumOf { it.enrolledCount }
        )
    }

    // ---- member reservations -------------------------------------------------------------------------

    private fun ReservationDto.isActive() = status?.contains("cancel", ignoreCase = true) != true

    override suspend fun getMemberEnrollments(memberId: String): List<Lesson> {
        val reservations = apiGet<List<ReservationDto>>("/reservations/me").filter { it.isActive() }
        if (reservations.isEmpty()) return emptyList()
        val workoutMap = getWorkouts().associate { it.id to it.name }
        val instructorMap = try { getInstructors().associate { it.id to it.fullName } } catch (e: Exception) { emptyMap() }
        return coroutineScope {
            reservations.map { r ->
                async { enrichLesson(apiGet("/lessons/${r.lessonId}"), workoutMap, instructorMap) }
            }.awaitAll()
        }
    }

    @Serializable
    private data class BikeDto(val id: String, val rowNumber: Int, val seatNumber: Int)

    override suspend fun getAvailableBikes(lessonId: String): List<Bike> =
        apiGet<List<BikeDto>>("/bikes/available/$lessonId").map { Bike(it.id, it.rowNumber, it.seatNumber) }

    override suspend fun reserveLesson(lessonId: String, memberId: String, bikeId: String?) {
        apiPostNoResponse("/reservations", ReservationRequestDto(lessonId, bikeId))
    }

    override suspend fun cancelReservation(lessonId: String, memberId: String) {
        val reservation = apiGet<List<ReservationDto>>("/reservations/me")
            .firstOrNull { it.lessonId == lessonId && it.isActive() }
            ?: throw Exception("Reservation not found")
        apiDelete("/reservations/${reservation.id}")
    }

    override suspend fun joinWaitlist(lessonId: String, memberId: String) {
        apiPostNoResponse("/reservations/waitlist", ReservationRequestDto(lessonId))
    }

    override suspend fun leaveWaitlist(lessonId: String, memberId: String) {
        apiDelete("/reservations/waitlist/$lessonId")
    }

    override suspend fun getMyWaitlist(memberId: String): List<String> {
        return apiGet<List<WaitlistEntryDto>>("/reservations/waitlist/me").map { it.lessonId }
    }

    // ---- instructor operations ---------------------------------------------------------------

    override suspend fun getMyLessons(): List<Lesson> = enrichLessons(apiGet("/lessons/my-lessons"))

    override suspend fun getLessonRoster(lessonId: String): List<LessonRosterEntry> =
        apiGet<List<LessonRosterEntryDto>>("/reservations/lesson/$lessonId").map {
            LessonRosterEntry(it.reservationId, it.userId, it.name, it.email, it.status)
        }
}
