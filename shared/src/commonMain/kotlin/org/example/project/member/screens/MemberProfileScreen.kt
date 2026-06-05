package org.example.project.member.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import org.example.project.member.navigation.MemberNavigator
import org.example.project.viewmodel.MemberSessionViewModel

@Composable
fun MemberProfileScreen(navigator: MemberNavigator, sessionVm: MemberSessionViewModel) {
    val state = sessionVm.state
    val member = state.memberInfo
    val user = navigator.currentUser
    val scroll = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user?.userId) {
        user?.let { sessionVm.loadMemberInfo(it.userId) }
    }

    val initials = user?.fullName?.split(" ")?.take(2)
        ?.mapNotNull { it.firstOrNull()?.toString() }?.joinToString("") ?: "?"

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Text("My Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Avatar + name
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF1565C0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user?.fullName ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(user?.email ?: "", fontSize = 14.sp, color = Color(0xFF757575))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1565C0).copy(0.1f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            user?.role?.name ?: "MEMBER",
                            color = Color(0xFF1565C0), fontSize = 12.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Details
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Account Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    ProfileRow("Full Name", member?.fullName ?: user?.fullName ?: "—")
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    ProfileRow("Email", member?.email ?: user?.email ?: "—")
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    ProfileRow("Phone", member?.phone?.ifBlank { "—" } ?: "—")
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    ProfileRow("Member since", member?.activeSubscription?.startDate ?: "—")
                }
            }

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) { Text("Sign Out") }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; navigator.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Sign Out") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF757575), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
