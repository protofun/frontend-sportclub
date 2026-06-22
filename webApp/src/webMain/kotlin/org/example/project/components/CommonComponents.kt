package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.model.Lesson
import org.example.project.model.LocationType
import org.example.project.model.MembershipStatus
import org.example.project.theme.*

// Shared UI components used across multiple screens.

// Bold section heading used at the top of content areas.
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = OnBackground, modifier = modifier)
}

// Stat box used on the admin dashboard (e.g. "Total Members / 142").
@Composable
fun StatCard(label: String, value: String, color: Color = Primary, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 14.sp, color = OnSurfaceVariant)
        }
    }
}

// Full lesson card used on ScheduleScreen and AdminDashboardScreen.
@Composable
fun LessonCard(lesson: Lesson, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), onClick = onClick ?: {}) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Thin colored vertical bar on the left
            Box(
                modifier = Modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp))
                    .background(when (lesson.locationType) { LocationType.SPINNING -> Secondary; LocationType.OUTDOOR -> Success; else -> Primary })
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(lesson.workoutName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OccupancyBadge(lesson.enrolledCount, lesson.maxCapacity)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LessonDetail("⏰", formatTime(lesson.startTime))
                    LessonDetail("⏱", "${lesson.durationMinutes} min")
                    LessonDetail("📍", lesson.locationName)
                }
                Spacer(Modifier.height(4.dp))
                Text(lesson.instructorName ?: "TBD", fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LessonDetail(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 12.sp)
        Text(text, fontSize = 13.sp, color = OnSurfaceVariant)
    }
}

// Pill badge showing enrolled/max. Color: green < 80%, orange >= 80%, red = full.
@Composable
fun OccupancyBadge(enrolled: Int, max: Int) {
    val pct = enrolled.toFloat() / max
    val color = when { pct >= 1f -> Error; pct >= 0.8f -> Warning; else -> Success }
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text("$enrolled/$max", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// Pill badge for membership status (Active=green, Expired=red, Cancelled=grey, Pending=orange).
@Composable
fun SubscriptionStatusBadge(status: MembershipStatus) {
    val (label, color) = when (status) {
        MembershipStatus.ACTIVE    -> "Active"    to Success
        MembershipStatus.EXPIRED   -> "Expired"   to Error
        MembershipStatus.CANCELLED -> "Cancelled" to OnSurfaceVariant
        MembershipStatus.PENDING   -> "Pending"   to Warning
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// Red error banner with a dismiss button used by LoginScreen and RegisterScreen.
@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(message, color = Error, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("Dismiss", color = Error) }
        }
    }
}

// Centered scrollable container with a max width — keeps content readable on wide screens.
@Composable
fun PageContainer(modifier: Modifier = Modifier, maxWidth: Dp = 1100.dp, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.widthIn(max = maxWidth).align(Alignment.TopCenter).padding(horizontal = 24.dp, vertical = 24.dp), content = content)
    }
}

// Extracts the time portion from a backend datetime string.
// Example: "2026-06-05T09:00:00" → "09:00"
fun formatTime(dateTime: String): String = dateTime.substringAfter("T").substring(0, 5)

// Converts an ISO date string to a human-readable format.
// Example: "2026-06-05" → "June 5, 2026"
fun formatDate(date: String): String {
    val months = listOf("", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val parts = date.split("-")
    return "${months[parts[1].toInt()]} ${parts[2].toInt()}, ${parts[0]}"
}

// Returns the day-of-week name for an ISO date string
// Example: "2026-06-05" → "Friday"
fun dayOfWeek(date: String): String {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    return try {
        val parts = date.split("-")
        val y = parts[0].toInt(); val m = parts[1].toInt(); val d = parts[2].toInt()
        val t = (14 - m) / 12; val yr = y - t; val mr = m + 12 * t - 2
        val dow = (d + yr + yr / 4 - yr / 100 + yr / 400 + (31 * mr) / 12) % 7
        days[if (dow == 0) 6 else dow - 1]
    } catch (e: Exception) { date }
}
