package org.example.project.api

import org.example.project.model.*
import kotlin.random.Random

class MockApiService : SportClubApiService {

    private val workouts = mutableListOf(
        Workout("1", "Yoga", "Relaxing yoga sessions for all levels"),
        Workout("2", "Bootcamp", "Intensive outdoor bootcamp training"),
        Workout("3", "Spinning", "Indoor cycling workout"),
        Workout("4", "Body Shape", "Full body toning workout"),
        Workout("5", "Club Power", "Strength training with weights"),
        Workout("6", "Boxing", "Boxing training for fitness"),
        Workout("7", "XCO Training", "Functional training with XCO equipment"),
        Workout("8", "Total Body Workout", "Complete full-body exercise session")
    )

    private val instructors = mutableListOf(
        Instructor("1", "Emma", "de Vries", "emma@sportclub.nl", null, listOf("Yoga", "Body Shape")),
        Instructor("2", "Lars", "van den Berg", "lars@sportclub.nl", null, listOf("Bootcamp", "Boxing")),
        Instructor("3", "Sophie", "Janssen", "sophie@sportclub.nl", null, listOf("Spinning", "Club Power")),
        Instructor("4", "Tim", "Bakker", "tim@sportclub.nl", null, listOf("Total Body Workout", "XCO Training"))
    )

    private val locations = listOf(
        Location("1", "Hall 1", LocationType.GROUP_INDOOR, 42),
        Location("2", "Hall 2", LocationType.GROUP_INDOOR, 32),
        Location("3", "Hall 3", LocationType.GROUP_INDOOR, 24),
        Location("4", "Outdoor Area", LocationType.OUTDOOR, 20),
        Location("5", "Spinning Room", LocationType.SPINNING, 24)
    )

    private val membershipPrices = listOf(
        MembershipPrice(MembershipType.BASIC, BillingCycle.MONTHLY, 2900),
        MembershipPrice(MembershipType.BASIC, BillingCycle.YEARLY, 29900),
        MembershipPrice(MembershipType.UNLIMITED, BillingCycle.MONTHLY, 5500),
        MembershipPrice(MembershipType.UNLIMITED, BillingCycle.YEARLY, 54900)
    )

    private val members = mutableListOf(
        Member("1", "jan@example.com", "Jan Pietersen", null, UserRole.MEMBER,
            Membership("m1", "1", MembershipType.UNLIMITED, BillingCycle.MONTHLY, "2024-01-01", null, MembershipStatus.ACTIVE)),
        Member("2", "maria@example.com", "Maria Smit", null, UserRole.MEMBER,
            Membership("m2", "2", MembershipType.BASIC, BillingCycle.MONTHLY, "2024-02-01", null, MembershipStatus.ACTIVE)),
        Member("3", "peter@example.com", "Peter de Groot", null, UserRole.MEMBER,
            Membership("m3", "3", MembershipType.BASIC, BillingCycle.YEARLY, "2024-01-01", "2025-01-01", MembershipStatus.ACTIVE)),
        Member("4", "anna@example.com", "Anna Visser", null, UserRole.MEMBER,
            Membership("m4", "4", MembershipType.UNLIMITED, BillingCycle.YEARLY, "2023-06-01", "2024-06-01", MembershipStatus.EXPIRED)),
        Member("5", "bas@example.com", "Bas Meijer", null, UserRole.MEMBER,
            Membership("m5", "5", MembershipType.UNLIMITED, BillingCycle.MONTHLY, "2025-03-01", null, MembershipStatus.ACTIVE)),
        Member("6", "lisa@example.com", "Lisa van Dam", null, UserRole.MEMBER,
            Membership("m6", "6", MembershipType.BASIC, BillingCycle.MONTHLY, "2025-11-01", null, MembershipStatus.ACTIVE))
    )

    // Demo member for the member app login (maps to members[0] = Jan Pietersen, id="1")
    private val demoMemberId = "1"

    private var nextId = 200

    private var currentUserId: String? = null

    private val lessons: MutableList<Lesson> = run {
        val list = mutableListOf<Lesson>()
        val dates = listOf(
            "2026-06-05", "2026-06-06", "2026-06-07", "2026-06-08", "2026-06-09",
            "2026-06-10", "2026-06-11", "2026-06-12", "2026-06-13", "2026-06-14"
        )
        val slots = listOf(
            Triple(0, "09:00", 1), Triple(1, "10:30", 4),
            Triple(2, "12:00", 5), Triple(3, "17:30", 2),
            Triple(4, "19:00", 1), Triple(5, "18:00", 3)
        )
        var id = 1
        for (date in dates) {
            for ((workoutIdx, time, locationIdx) in slots) {
                if (id % 3 != 0) {
                    val workout = workouts[workoutIdx % workouts.size]
                    val location = locations[locationIdx - 1]
                    val instructor = instructors[id % instructors.size]
                    val enrolled = Random.nextInt(0, location.capacity)
                    list.add(Lesson(
                        id = id.toString(),
                        workoutId = workout.id,
                        workoutName = workout.name,
                        instructorId = instructor.id,
                        instructorName = instructor.fullName,
                        locationId = location.id,
                        locationName = location.name,
                        locationType = location.type,
                        startTime = "${date}T${time}:00",
                        durationMinutes = 60,
                        maxCapacity = location.capacity,
                        enrolledCount = enrolled
                    ))
                }
                id++
            }
        }
        list
    }

    // Member enrollment state: memberId -> set of lessonIds
    private val memberEnrollments = mutableMapOf<String, MutableSet<String>>()
    private val memberWaitlists = mutableMapOf<String, MutableSet<String>>()

    override fun logout() {
        currentUserId = null
    }

    override suspend fun login(request: LoginRequest): LoginResponse =
        when (request.email) {
            "admin@sportclub.nl" -> LoginResponse("mock-token", "100", UserRole.STAFF, "Admin User", request.email).also { currentUserId = it.userId }
            "instructor@sportclub.nl" -> LoginResponse("mock-token", "1", UserRole.INSTRUCTOR, "Emma de Vries", request.email).also { currentUserId = it.userId }
            "member@sportclub.nl" -> LoginResponse("mock-token", demoMemberId, UserRole.MEMBER, "Jan Pietersen", request.email).also { currentUserId = it.userId }
            else -> throw Exception("Invalid email or password")
        }

    override suspend fun registerMember(request: RegistrationRequest): LoginResponse {
        val id = (nextId++).toString()
        val endDate = if (request.billingCycle == BillingCycle.YEARLY) {
            val year = request.startDate.substring(0, 4).toInt() + 1
            "$year${request.startDate.substring(4)}"
        } else null
        val membership = Membership(
            id = "m${nextId++}", userId = id, type = request.membershipType, billingCycle = request.billingCycle,
            startDate = request.startDate, endDate = endDate, status = MembershipStatus.ACTIVE
        )
        val fullName = "${request.firstName} ${request.lastName}".trim()
        val member = Member(id, request.email, fullName.ifBlank { null }, null, UserRole.MEMBER, membership)
        members.add(member)
        currentUserId = id
        return LoginResponse("mock-token", id, UserRole.MEMBER, member.fullName, member.email)
    }

    override suspend fun getWorkouts() = workouts.toList()

    override suspend fun createWorkout(request: WorkoutRequest): Workout {
        val w = Workout((nextId++).toString(), request.name, request.description)
        workouts.add(w); return w
    }

    override suspend fun updateWorkout(id: String, request: WorkoutRequest): Workout {
        val i = workouts.indexOfFirst { it.id == id }
        val w = Workout(id, request.name, request.description)
        workouts[i] = w; return w
    }

    override suspend fun deleteWorkout(id: String) { workouts.removeAll { it.id == id } }

    override suspend fun getInstructors() = instructors.toList()

    override suspend fun createInstructor(request: InstructorRequest): Instructor {
        val ins = Instructor((nextId++).toString(), request.firstName, request.lastName, request.email, request.photoUrl, request.specialties)
        instructors.add(ins); return ins
    }

    override suspend fun updateInstructor(id: String, request: InstructorRequest): Instructor {
        val i = instructors.indexOfFirst { it.id == id }
        val ins = Instructor(id, request.firstName, request.lastName, request.email, request.photoUrl, request.specialties)
        instructors[i] = ins; return ins
    }

    override suspend fun deleteInstructor(id: String) { instructors.removeAll { it.id == id } }

    override suspend fun getLocations() = locations

    override suspend fun getLessons(startDate: String, endDate: String) =
        lessons.filter { it.startTime >= startDate && it.startTime <= "${endDate}T23:59:59" }

    override suspend fun createLesson(request: LessonRequest): List<Lesson> {
        val workout = workouts.find { it.id == request.workoutId } ?: throw Exception("Workout not found")
        val location = locations.find { it.id == request.locationId } ?: throw Exception("Location not found")
        val instructor = instructors.find { it.id == request.instructorId }
        val lesson = Lesson(
            id = (nextId++).toString(), workoutId = workout.id, workoutName = workout.name,
            instructorId = instructor?.id, instructorName = instructor?.fullName ?: "TBD",
            locationId = location.id, locationName = location.name, locationType = location.type,
            startTime = request.startTime, durationMinutes = request.durationMinutes,
            maxCapacity = location.capacity, enrolledCount = 0
        )
        lessons.add(lesson)
        return listOf(lesson)
    }

    override suspend fun updateLesson(id: String, request: LessonRequest): Lesson {
        val i = lessons.indexOfFirst { it.id == id }
        val workout = workouts.find { it.id == request.workoutId } ?: throw Exception("Workout not found")
        val location = locations.find { it.id == request.locationId } ?: throw Exception("Location not found")
        val instructor = instructors.find { it.id == request.instructorId }
        val updated = lessons[i].copy(
            workoutId = workout.id, workoutName = workout.name,
            instructorId = instructor?.id, instructorName = instructor?.fullName ?: "TBD",
            locationId = location.id, locationName = location.name, locationType = location.type,
            startTime = request.startTime, durationMinutes = request.durationMinutes
        )
        lessons[i] = updated; return updated
    }

    override suspend fun deleteLesson(id: String) { lessons.removeAll { it.id == id } }

    override suspend fun getMembers() = members.toList()

    override suspend fun getMember(id: String) = members.find { it.id == id } ?: throw Exception("Member not found")

    override suspend fun updateProfile(username: String?, profileImageUrl: String?): Member {
        val id = currentUserId ?: throw Exception("Not logged in")
        val idx = members.indexOfFirst { it.id == id }
        if (idx < 0) throw Exception("Member not found")
        val updated = members[idx].copy(
            username = username ?: members[idx].username,
            profileImageUrl = profileImageUrl ?: members[idx].profileImageUrl
        )
        members[idx] = updated
        return updated
    }

    override suspend fun getMembershipPrices() = membershipPrices

    override suspend fun subscribeMembership(type: MembershipType, billingCycle: BillingCycle, startDate: String): Membership {
        val id = currentUserId ?: throw Exception("Not logged in")
        val idx = members.indexOfFirst { it.id == id }
        if (idx < 0) throw Exception("Member not found")
        val membership = Membership("m${nextId++}", id, type, billingCycle, startDate, null, MembershipStatus.ACTIVE)
        members[idx] = members[idx].copy(activeMembership = membership)
        return membership
    }

    override suspend fun upgradeMembership(membershipId: String): Membership {
        val id = currentUserId ?: throw Exception("Not logged in")
        val idx = members.indexOfFirst { it.id == id }
        val current = members.getOrNull(idx)?.activeMembership ?: throw Exception("No active membership")
        val upgraded = current.copy(type = MembershipType.UNLIMITED)
        members[idx] = members[idx].copy(activeMembership = upgraded)
        return upgraded
    }

    override suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy> =
        getLessons(startDate, endDate).map { lesson ->
            LessonOccupancy(lesson, members.take(lesson.enrolledCount).map { m ->
                MemberEnrollment(m.id, m.fullName, EnrollmentStatus.ENROLLED, false)
            })
        }

    override suspend fun getDashboardStats() = DashboardStats(
        totalMembers = members.size,
        activeMembers = members.count { it.activeMembership?.status == MembershipStatus.ACTIVE },
        lessonsToday = lessons.count { it.startTime.startsWith("2026-06-05") },
        enrollmentsToday = lessons.filter { it.startTime.startsWith("2026-06-05") }.sumOf { it.enrolledCount }
    )

    override suspend fun getMemberEnrollments(memberId: String): List<Lesson> {
        val ids = memberEnrollments[memberId] ?: return emptyList()
        return lessons.filter { it.id in ids }
    }

    override suspend fun reserveLesson(lessonId: String, memberId: String) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) throw Exception("Lesson not found")
        val lesson = lessons[idx]
        if (lesson.isFull) throw Exception("Class is full — join the waitlist instead")
        memberEnrollments.getOrPut(memberId) { mutableSetOf() }.add(lessonId)
        lessons[idx] = lesson.copy(enrolledCount = lesson.enrolledCount + 1)
    }

    override suspend fun cancelReservation(lessonId: String, memberId: String) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) return
        val enrolled = memberEnrollments[memberId]
        if (enrolled != null && lessonId in enrolled) {
            enrolled.remove(lessonId)
            val lesson = lessons[idx]
            if (lesson.enrolledCount > 0) lessons[idx] = lesson.copy(enrolledCount = lesson.enrolledCount - 1)
        }
    }

    override suspend fun joinWaitlist(lessonId: String, memberId: String) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) throw Exception("Lesson not found")
        memberWaitlists.getOrPut(memberId) { mutableSetOf() }.add(lessonId)
        val lesson = lessons[idx]
        lessons[idx] = lesson.copy(waitlistCount = lesson.waitlistCount + 1)
    }

    override suspend fun leaveWaitlist(lessonId: String, memberId: String) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) return
        val waitlist = memberWaitlists[memberId]
        if (waitlist != null && lessonId in waitlist) {
            waitlist.remove(lessonId)
            val lesson = lessons[idx]
            if (lesson.waitlistCount > 0) lessons[idx] = lesson.copy(waitlistCount = lesson.waitlistCount - 1)
        }
    }

    override suspend fun getMyWaitlist(memberId: String): List<String> {
        return memberWaitlists[memberId]?.toList() ?: emptyList()
    }
}
