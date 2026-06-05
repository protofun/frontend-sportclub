package org.example.project.api

import org.example.project.model.*
import kotlin.random.Random

class MockApiService : SportClubApiService {

    private val workouts = mutableListOf(
        Workout(1, "Yoga", "Relaxing yoga sessions for all levels"),
        Workout(2, "Bootcamp", "Intensive outdoor bootcamp training"),
        Workout(3, "Spinning", "Indoor cycling workout"),
        Workout(4, "Body Shape", "Full body toning workout"),
        Workout(5, "Club Power", "Strength training with weights"),
        Workout(6, "Boxing", "Boxing training for fitness"),
        Workout(7, "XCO Training", "Functional training with XCO equipment"),
        Workout(8, "Total Body Workout", "Complete full-body exercise session")
    )

    private val instructors = mutableListOf(
        Instructor(1, "Emma", "de Vries", "emma@sportclub.nl", null, listOf("Yoga", "Body Shape")),
        Instructor(2, "Lars", "van den Berg", "lars@sportclub.nl", null, listOf("Bootcamp", "Boxing")),
        Instructor(3, "Sophie", "Janssen", "sophie@sportclub.nl", null, listOf("Spinning", "Club Power")),
        Instructor(4, "Tim", "Bakker", "tim@sportclub.nl", null, listOf("Total Body Workout", "XCO Training"))
    )

    private val locations = listOf(
        Location(1, "Hall 1", LocationType.GROUP_INDOOR, 42),
        Location(2, "Hall 2", LocationType.GROUP_INDOOR, 32),
        Location(3, "Hall 3", LocationType.GROUP_INDOOR, 24),
        Location(4, "Outdoor Area", LocationType.OUTDOOR, 20),
        Location(5, "Spinning Room", LocationType.SPINNING, 24)
    )

    private val subscriptionPlans = listOf(
        SubscriptionPlan(1, SubscriptionType.TWO_PER_WEEK, BillingCycle.MONTHLY, 29.0,
            "2x per week – Monthly", "Work out up to 2 times per week, billed monthly"),
        SubscriptionPlan(2, SubscriptionType.TWO_PER_WEEK, BillingCycle.YEARLY, 299.0,
            "2x per week – Yearly", "Work out up to 2 times per week, billed yearly (save €50)"),
        SubscriptionPlan(3, SubscriptionType.UNLIMITED, BillingCycle.MONTHLY, 55.0,
            "Unlimited – Monthly", "Unlimited access to all classes, billed monthly"),
        SubscriptionPlan(4, SubscriptionType.UNLIMITED, BillingCycle.YEARLY, 549.0,
            "Unlimited – Yearly", "Unlimited access to all classes, billed yearly (save €111)")
    )

    private val members = mutableListOf(
        Member(1, "Jan", "Pietersen", "jan@example.com", "06-12345678", null,
            MemberSubscription(1, 3, "Unlimited – Monthly", SubscriptionType.UNLIMITED, BillingCycle.MONTHLY, "2024-01-01", null, SubscriptionStatus.ACTIVE)),
        Member(2, "Maria", "Smit", "maria@example.com", "06-87654321", null,
            MemberSubscription(2, 1, "2x per week – Monthly", SubscriptionType.TWO_PER_WEEK, BillingCycle.MONTHLY, "2024-02-01", null, SubscriptionStatus.ACTIVE)),
        Member(3, "Peter", "de Groot", "peter@example.com", "06-11223344", null,
            MemberSubscription(3, 2, "2x per week – Yearly", SubscriptionType.TWO_PER_WEEK, BillingCycle.YEARLY, "2024-01-01", "2025-01-01", SubscriptionStatus.ACTIVE)),
        Member(4, "Anna", "Visser", "anna@example.com", "06-44556677", null,
            MemberSubscription(4, 4, "Unlimited – Yearly", SubscriptionType.UNLIMITED, BillingCycle.YEARLY, "2023-06-01", "2024-06-01", SubscriptionStatus.EXPIRED)),
        Member(5, "Bas", "Meijer", "bas@example.com", "06-55667788", null,
            MemberSubscription(5, 3, "Unlimited – Monthly", SubscriptionType.UNLIMITED, BillingCycle.MONTHLY, "2025-03-01", null, SubscriptionStatus.ACTIVE)),
        Member(6, "Lisa", "van Dam", "lisa@example.com", "06-99887766", null,
            MemberSubscription(6, 1, "2x per week – Monthly", SubscriptionType.TWO_PER_WEEK, BillingCycle.MONTHLY, "2025-11-01", null, SubscriptionStatus.ACTIVE))
    )

    // Demo member for the member app login (maps to members[0] = Jan Pietersen, id=1)
    private val demoMemberId = 1

    private var nextId = 200

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
                        id = id,
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
    private val memberEnrollments = mutableMapOf<Int, MutableSet<Int>>()
    private val memberWaitlists = mutableMapOf<Int, MutableSet<Int>>()

    override suspend fun login(request: LoginRequest): LoginResponse =
        when (request.email) {
            "admin@sportclub.nl" -> LoginResponse("mock-token", 0, UserRole.STAFF, "Admin User", request.email)
            "instructor@sportclub.nl" -> LoginResponse("mock-token", 1, UserRole.INSTRUCTOR, "Emma de Vries", request.email)
            "member@sportclub.nl" -> LoginResponse("mock-token", demoMemberId, UserRole.MEMBER, "Jan Pietersen", request.email)
            else -> throw Exception("Invalid email or password")
        }

    override suspend fun getWorkouts() = workouts.toList()

    override suspend fun createWorkout(request: WorkoutRequest): Workout {
        val w = Workout(nextId++, request.name, request.description, request.imageUrl)
        workouts.add(w); return w
    }

    override suspend fun updateWorkout(id: Int, request: WorkoutRequest): Workout {
        val i = workouts.indexOfFirst { it.id == id }
        val w = Workout(id, request.name, request.description, request.imageUrl)
        workouts[i] = w; return w
    }

    override suspend fun deleteWorkout(id: Int) { workouts.removeAll { it.id == id } }

    override suspend fun getInstructors() = instructors.toList()

    override suspend fun createInstructor(request: InstructorRequest): Instructor {
        val ins = Instructor(nextId++, request.firstName, request.lastName, request.email, request.photoUrl, request.specialties)
        instructors.add(ins); return ins
    }

    override suspend fun updateInstructor(id: Int, request: InstructorRequest): Instructor {
        val i = instructors.indexOfFirst { it.id == id }
        val ins = Instructor(id, request.firstName, request.lastName, request.email, request.photoUrl, request.specialties)
        instructors[i] = ins; return ins
    }

    override suspend fun deleteInstructor(id: Int) { instructors.removeAll { it.id == id } }

    override suspend fun getLocations() = locations

    override suspend fun getLessons(startDate: String, endDate: String) =
        lessons.filter { it.startTime >= startDate && it.startTime <= "${endDate}T23:59:59" }

    override suspend fun createLesson(request: LessonRequest): List<Lesson> {
        val workout = workouts.find { it.id == request.workoutId } ?: throw Exception("Workout not found")
        val location = locations.find { it.id == request.locationId } ?: throw Exception("Location not found")
        val instructor = instructors.find { it.id == request.instructorId }
        val lesson = Lesson(
            id = nextId++, workoutId = workout.id, workoutName = workout.name,
            instructorId = instructor?.id, instructorName = instructor?.fullName ?: "TBD",
            locationId = location.id, locationName = location.name, locationType = location.type,
            startTime = request.startTime, durationMinutes = request.durationMinutes,
            maxCapacity = location.capacity, enrolledCount = 0
        )
        lessons.add(lesson)
        return listOf(lesson)
    }

    override suspend fun updateLesson(id: Int, request: LessonRequest): Lesson {
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

    override suspend fun deleteLesson(id: Int) { lessons.removeAll { it.id == id } }

    override suspend fun getMembers() = members.toList()

    override suspend fun getMember(id: Int) = members.find { it.id == id } ?: throw Exception("Member not found")

    override suspend fun getSubscriptionPlans() = subscriptionPlans

    override suspend fun registerMember(request: RegistrationRequest): Member {
        val plan = subscriptionPlans.find { it.id == request.subscriptionPlanId } ?: throw Exception("Plan not found")
        val endDate = if (plan.billingCycle == BillingCycle.YEARLY) {
            val year = request.startDate.substring(0, 4).toInt() + 1
            "$year${request.startDate.substring(4)}"
        } else null
        val sub = MemberSubscription(nextId++, plan.id, plan.name, plan.type, plan.billingCycle, request.startDate, endDate, SubscriptionStatus.ACTIVE)
        val member = Member(nextId++, request.firstName, request.lastName, request.email, request.phone, null, sub)
        members.add(member); return member
    }

    override suspend fun getLessonOccupancy(startDate: String, endDate: String): List<LessonOccupancy> =
        getLessons(startDate, endDate).map { lesson ->
            LessonOccupancy(lesson, members.take(lesson.enrolledCount).map { m ->
                MemberEnrollment(m.id, m.fullName, EnrollmentStatus.ENROLLED, false)
            })
        }

    override suspend fun getDashboardStats() = DashboardStats(
        totalMembers = members.size,
        activeMembers = members.count { it.activeSubscription?.status == SubscriptionStatus.ACTIVE },
        lessonsToday = lessons.count { it.startTime.startsWith("2026-06-05") },
        enrollmentsToday = lessons.filter { it.startTime.startsWith("2026-06-05") }.sumOf { it.enrolledCount }
    )

    override suspend fun getMemberEnrollments(memberId: Int): List<Lesson> {
        val ids = memberEnrollments[memberId] ?: return emptyList()
        return lessons.filter { it.id in ids }
    }

    override suspend fun reserveLesson(lessonId: Int, memberId: Int): EnrollmentStatus {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) throw Exception("Lesson not found")
        val lesson = lessons[idx]
        if (lesson.isFull) throw Exception("Class is full — join the waitlist instead")
        memberEnrollments.getOrPut(memberId) { mutableSetOf() }.add(lessonId)
        lessons[idx] = lesson.copy(enrolledCount = lesson.enrolledCount + 1)
        return EnrollmentStatus.ENROLLED
    }

    override suspend fun cancelReservation(lessonId: Int, memberId: Int) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) return
        val enrolled = memberEnrollments[memberId]
        if (enrolled != null && lessonId in enrolled) {
            enrolled.remove(lessonId)
            val lesson = lessons[idx]
            if (lesson.enrolledCount > 0) lessons[idx] = lesson.copy(enrolledCount = lesson.enrolledCount - 1)
        }
    }

    override suspend fun joinWaitlist(lessonId: Int, memberId: Int): EnrollmentStatus {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) throw Exception("Lesson not found")
        memberWaitlists.getOrPut(memberId) { mutableSetOf() }.add(lessonId)
        val lesson = lessons[idx]
        lessons[idx] = lesson.copy(waitlistCount = lesson.waitlistCount + 1)
        return EnrollmentStatus.WAITLISTED
    }

    override suspend fun leaveWaitlist(lessonId: Int, memberId: Int) {
        val idx = lessons.indexOfFirst { it.id == lessonId }
        if (idx < 0) return
        val waitlist = memberWaitlists[memberId]
        if (waitlist != null && lessonId in waitlist) {
            waitlist.remove(lessonId)
            val lesson = lessons[idx]
            if (lesson.waitlistCount > 0) lessons[idx] = lesson.copy(waitlistCount = lesson.waitlistCount - 1)
        }
    }
}
