package org.example.project.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.example.project.api.SportClubApiService
import org.example.project.components.formatDate
import org.example.project.components.formatTime
import org.example.project.model.LessonOccupancy
import org.example.project.theme.*
import org.example.project.util.todayDateString

@Composable
fun OccupancyScreen(api: SportClubApiService) {
    var startDate by remember { mutableStateOf(todayDateString()) }
    var endDate by remember { mutableStateOf(todayDateString()) }
    var occupancy by remember { mutableStateOf<List<LessonOccupancy>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = remember { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    val scroll = rememberScrollState()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            occupancy = api.getLessonOccupancy(startDate, endDate)
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    fun reload() {
        scope.launch {
            isLoading = true
            error = null
            try {
                occupancy = api.getLessonOccupancy(startDate, endDate)
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Column {
                Text("Class Occupancy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("View enrollment and occupancy rates", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Date range filter
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        startDate, { startDate = it },
                        label = { Text("From (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        endDate, { endDate = it },
                        label = { Text("To (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { reload() }, modifier = Modifier.height(56.dp)) {
                        Text("Apply Filter")
                    }
                }
            }

            error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = Error.copy(0.1f)), shape = RoundedCornerShape(8.dp)) {
                    Text(it, modifier = Modifier.padding(16.dp), color = Error)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (occupancy.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    Text("No classes found for the selected period.", color = OnSurfaceVariant)
                }
            } else {
                // Summary
                val totalCapacity = occupancy.sumOf { it.lesson.maxCapacity }
                val totalEnrolled = occupancy.sumOf { it.lesson.enrolledCount }
                val avgPct = if (totalCapacity > 0) (totalEnrolled * 100 / totalCapacity) else 0

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard("Total Classes", occupancy.size.toString(), Primary, Modifier.weight(1f))
                    SummaryCard("Total Enrolled", totalEnrolled.toString(), Secondary, Modifier.weight(1f))
                    SummaryCard("Total Capacity", totalCapacity.toString(), OnBackground, Modifier.weight(1f))
                    SummaryCard("Avg. Occupancy", "$avgPct%", if (avgPct >= 80) Error else if (avgPct >= 50) Warning else Success, Modifier.weight(1f))
                }

                // Table
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Date", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Time", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.7f))
                            Text("Class", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                            Text("Instructor", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
                            Text("Location", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Enrolled", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                            Text("Capacity", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                            Text("Rate", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider()

                        occupancy.sortedBy { it.lesson.startTime }.forEachIndexed { i, item ->
                            val lesson = item.lesson
                            val pct = lesson.enrolledCount * 100 / lesson.maxCapacity
                            val color = when {
                                pct >= 90 -> Error
                                pct >= 70 -> Warning
                                else -> Success
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lesson.startTime.substring(0, 10), fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(formatTime(lesson.startTime), fontSize = 13.sp, modifier = Modifier.weight(0.7f))
                                Text(lesson.workoutName, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
                                Text(lesson.instructorName ?: "TBD", fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1.5f))
                                Text(lesson.locationName, fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("${lesson.enrolledCount}", fontSize = 13.sp, modifier = Modifier.weight(0.8f))
                                Text("${lesson.maxCapacity}", fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(0.8f))
                                Box(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(color.copy(0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("$pct%", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            if (i < occupancy.size - 1) HorizontalDivider(color = Color.LightGray.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 13.sp, color = OnSurfaceVariant)
        }
    }
}
