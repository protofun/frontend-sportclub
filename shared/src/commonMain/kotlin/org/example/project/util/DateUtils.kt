package org.example.project.util

expect fun currentEpochMillis(): Long

/** Returns today's date in the local platform timezone, formatted as "YYYY-MM-DD". */
fun todayDateString(): String {
    val days = currentEpochMillis() / 86_400_000L
    val (year, month, day) = civilFromDays(days)
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

// Howard Hinnant's days_from_civil / civil_from_days algorithm (proleptic Gregorian calendar).
private fun civilFromDays(z: Long): Triple<Int, Int, Int> {
    val zz = z + 719468
    val era = (if (zz >= 0) zz else zz - 146096) / 146097
    val doe = zz - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val year = if (m <= 2) y + 1 else y
    return Triple(year.toInt(), m.toInt(), d.toInt())
}

private fun daysFromCivil(y: Int, m: Int, d: Int): Long {
    val yy = (if (m <= 2) y - 1 else y).toLong()
    val era = (if (yy >= 0) yy else yy - 399) / 400
    val yoe = yy - era * 400
    val mp = (m + 9) % 12
    val doy = (153 * mp + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097L + doe - 719468L
}

/** Converts an ISO date or datetime string to a readable format, e.g. "2026-06-21T10:00:00" → "June 21, 2026" */
fun formatDateFriendly(dateOrDateTime: String): String {
    val datePart = dateOrDateTime.substringBefore("T")
    val parts = datePart.split("-")
    val months = listOf("", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")
    return "${months[parts[1].toInt()]} ${parts[2].toInt()}, ${parts[0]}"
}

/**
 * Returns true when the lesson's startTime has already passed (best-effort UTC comparison).
 * startTime must be "YYYY-MM-DDThh:mm:ss" or similar ISO format.
 */
fun isLessonInPast(startTime: String): Boolean {
    return try {
        val tIdx = startTime.indexOf("T")
        val datePart = startTime.substring(0, tIdx)
        val timePart = startTime.substring(tIdx + 1)
        val today = todayDateString()
        when {
            datePart < today -> true
            datePart > today -> false
            else -> {
                val h = timePart.substring(0, 2).toInt()
                val m = timePart.substring(3, 5).toInt()
                val lessonMinutesUtc = h * 60 + m
                val currentMinutesUtc = ((currentEpochMillis() / 60_000L) % (24 * 60)).toInt()
                currentMinutesUtc >= lessonMinutesUtc
            }
        }
    } catch (e: Exception) { false }
}

/** Adds [days] calendar days to an ISO-8601 date or date-time string, preserving any time component. */
fun addDaysToIsoDateTime(dateTime: String, days: Int): String {
    val tIndex = dateTime.indexOf("T")
    val datePart = if (tIndex >= 0) dateTime.substring(0, tIndex) else dateTime
    val timePart = if (tIndex >= 0) dateTime.substring(tIndex) else ""
    val (year, month, day) = datePart.split("-").map { it.toInt() }
    val totalDays = daysFromCivil(year, month, day) + days
    val (newYear, newMonth, newDay) = civilFromDays(totalDays)
    val newDatePart = "${newYear.toString().padStart(4, '0')}-${newMonth.toString().padStart(2, '0')}-${newDay.toString().padStart(2, '0')}"
    return newDatePart + timePart
}
