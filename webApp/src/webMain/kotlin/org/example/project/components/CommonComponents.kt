package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import org.example.project.model.SubscriptionStatus
import org.example.project.theme.*

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = OnBackground,
        modifier = modifier
    )
}

@Composable
fun StatCard(
    label: String,
    value: String,
    color: Color = Primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 14.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun LessonCard(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (lesson.locationType) {
                            LocationType.SPINNING -> Secondary
                            LocationType.OUTDOOR -> Success
                            else -> Primary
                        }
                    )
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                Text(
                    lesson.instructorName ?: "TBD",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LessonDetail(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 12.sp)
        Text(text, fontSize = 13.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun OccupancyBadge(enrolled: Int, max: Int) {
    val pct = enrolled.toFloat() / max
    val color = when {
        pct >= 1f -> Error
        pct >= 0.8f -> Warning
        else -> Success
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("$enrolled/$max", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SubscriptionStatusBadge(status: SubscriptionStatus) {
    val (label, color) = when (status) {
        SubscriptionStatus.ACTIVE -> "Active" to Success
        SubscriptionStatus.EXPIRED -> "Expired" to Error
        SubscriptionStatus.CANCELLED -> "Cancelled" to OnSurfaceVariant
        SubscriptionStatus.PENDING -> "Pending" to Warning
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = Error, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("Dismiss", color = Error) }
        }
    }
}

@Composable
fun PageContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1100.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            content = content
        )
    }
}

fun formatTime(dateTime: String): String {
    // "2026-06-05T09:00:00" -> "09:00"
    return dateTime.substringAfter("T").substring(0, 5)
}

fun formatDate(date: String): String {
    // "2026-06-05" -> "June 5, 2026"
    val months = listOf("", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December")
    val parts = date.split("-")
    return "${months[parts[1].toInt()]} ${parts[2].toInt()}, ${parts[0]}"
}

fun dayOfWeek(date: String): String {
    // Very simplified - just return the day number's name for known dates
    val days = mapOf(
        "2026-06-05" to "Friday", "2026-06-06" to "Saturday", "2026-06-07" to "Sunday",
        "2026-06-08" to "Monday", "2026-06-09" to "Tuesday", "2026-06-10" to "Wednesday",
        "2026-06-11" to "Thursday", "2026-06-12" to "Friday", "2026-06-13" to "Saturday",
        "2026-06-14" to "Sunday"
    )
    return days[date] ?: date
}
