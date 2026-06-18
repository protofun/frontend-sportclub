package org.example.project.member.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import org.example.project.model.Bike

private val ALL_BIKES = (1..4).flatMap { row -> (1..6).map { seat -> Bike("bike-$row-$seat", row, seat) } }

@Composable
fun SpinningBikePickerDialog(
    availableBikes: List<Bike>?,   // null = laden mislukt, lege lijst = alles bezet
    enrolledCount: Int,
    maxCapacity: Int,
    isLoading: Boolean,
    onConfirm: (Bike) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBike by remember { mutableStateOf<Bike?>(null) }

    val totalBikes = 24
    val bikesWithReservation = if (availableBikes != null) totalBikes - availableBikes.size else 0
    // People enrolled without a specific bike (old reservations or API failure)
    val enrolledWithoutBike = enrolledCount - bikesWithReservation
    val loadFailed = availableBikes == null && !isLoading

    // Which bikes to show as clickable:
    // - If API worked: only bikes in availableBikes list
    // - If API failed: all 24 (we can't know which are taken)
    val selectableBikes: List<Bike> = availableBikes ?: ALL_BIKES

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Choose your bike", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Spinning Room · $enrolledCount/$maxCapacity spots taken",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
                if (!isLoading && bikesWithReservation < enrolledCount && enrolledCount > 0) {
                    Text(
                        "$enrolledWithoutBike enrolled member(s) have no assigned bike yet",
                        fontSize = 11.sp,
                        color = Color(0xFFF57C00)
                    )
                }
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (loadFailed) {
                        Text(
                            "Could not load bike occupancy — all bikes shown as selectable.",
                            fontSize = 12.sp,
                            color = Color(0xFFF57C00)
                        )
                    }

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(Color(0xFF1565C0).copy(alpha = 0.25f), "Available")
                        LegendDot(Color(0xFF4CAF50), "Selected")
                        LegendDot(Color(0xFFE0E0E0), "Reserved")
                    }

                    Spacer(Modifier.height(4.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("← Front (instructor)", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        for (row in 1..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (seat in 1..6) {
                                    val bike = selectableBikes.find { it.rowNumber == row && it.seatNumber == seat }
                                    val isAvailable = bike != null
                                    val isSelected = selectedBike != null &&
                                        selectedBike!!.rowNumber == row &&
                                        selectedBike!!.seatNumber == seat

                                    val bgColor = when {
                                        isSelected  -> Color(0xFF4CAF50)
                                        isAvailable -> Color(0xFF1565C0).copy(alpha = 0.15f)
                                        else        -> Color(0xFFE0E0E0)
                                    }
                                    val borderColor = when {
                                        isSelected  -> Color(0xFF2E7D32)
                                        isAvailable -> Color(0xFF1565C0)
                                        else        -> Color(0xFFBDBDBD)
                                    }

                                    val clickMod = if (isAvailable && bike != null) {
                                        val captured = bike
                                        Modifier.clickable { selectedBike = captured }
                                    } else {
                                        Modifier
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bgColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                            .then(clickMod),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$seat",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected  -> Color.White
                                                isAvailable -> Color(0xFF1565C0)
                                                else        -> Color(0xFF9E9E9E)
                                            }
                                        )
                                    }
                                }
                            }
                            Text("Row $row", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                        }
                    }

                    selectedBike?.let { bike ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Selected: Row ${bike.rowNumber}, Bike ${bike.seatNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedBike?.let { onConfirm(it) } },
                enabled = selectedBike != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, fontSize = 11.sp, color = Color(0xFF757575))
    }
}
