package org.example.project.components

import androidx.compose.foundation.background
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
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.navigation.isAdminRoute
import org.example.project.theme.*

@Composable
fun TopNavBar(navigator: Navigator, onLogout: (() -> Unit)? = null) {
    val isAdmin = navigator.currentUser?.role?.name == "STAFF" || navigator.currentUser?.role?.name == "INSTRUCTOR"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Brand
            Row(
                modifier = Modifier.clickable { navigator.navigate(Route.Home) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(
                    "SportClub",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.weight(1f))

            if (navigator.currentRoute.isAdminRoute()) {
                // Admin navigation
                AdminNavItem("Dashboard", navigator.currentRoute is Route.AdminDashboard) { navigator.navigate(Route.AdminDashboard) }
                AdminNavItem("Workouts", navigator.currentRoute is Route.AdminWorkouts) { navigator.navigate(Route.AdminWorkouts) }
                AdminNavItem("Instructors", navigator.currentRoute is Route.AdminInstructors) { navigator.navigate(Route.AdminInstructors) }
                AdminNavItem("Schedule", navigator.currentRoute is Route.AdminSchedule) { navigator.navigate(Route.AdminSchedule) }
                AdminNavItem("Members", navigator.currentRoute is Route.AdminMembers) { navigator.navigate(Route.AdminMembers) }
                AdminNavItem("Occupancy", navigator.currentRoute is Route.AdminOccupancy) { navigator.navigate(Route.AdminOccupancy) }
            } else {
                // Public navigation
                PublicNavItem("Home", navigator.currentRoute is Route.Home) { navigator.navigate(Route.Home) }
                PublicNavItem("Schedule", navigator.currentRoute is Route.Schedule) { navigator.navigate(Route.Schedule) }
                PublicNavItem("Subscriptions", navigator.currentRoute is Route.Subscriptions) { navigator.navigate(Route.Subscriptions) }
                PublicNavItem("Downloads", navigator.currentRoute is Route.Downloads) { navigator.navigate(Route.Downloads) }
            }

            Spacer(Modifier.width(16.dp))

            if (navigator.isAuthenticated) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        navigator.currentUser?.fullName ?: "",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { onLogout?.invoke() ?: navigator.logout() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("Logout")
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { navigator.navigate(Route.Login) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) { Text("Login") }
                    Button(
                        onClick = { navigator.navigate(Route.Register) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary,
                            contentColor = Color.White
                        )
                    ) { Text("Join Now") }
                }
            }
        }
    }
}

@Composable
private fun PublicNavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = if (selected) Secondary else Color.White.copy(alpha = 0.85f))
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AdminNavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.75f),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
