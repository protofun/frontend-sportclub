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
