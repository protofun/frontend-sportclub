package org.example.project.screens.admin

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
import org.example.project.components.ErrorBanner
import org.example.project.components.SubscriptionStatusBadge
import org.example.project.model.BillingCycle
import org.example.project.model.Member
import org.example.project.model.MembershipType
import org.example.project.theme.*
import org.example.project.viewmodel.MemberViewModel

@Composable
fun MembersScreen(vm: MemberViewModel) {
    LaunchedEffect(Unit) { vm.load() }
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        Box(modifier = Modifier.fillMaxWidth().background(PrimaryDark).padding(horizontal = 32.dp, vertical = 24.dp)) {
            Column {
                Text("Members", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Overview of all club members", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            state.error?.let {
                ErrorBanner(it) { vm.clearError() }
            }

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.setSearch(it) },
                label = { Text("Search members by name or email") },
                modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
                singleLine = true
            )

            // Summary stats
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val active = state.members.count { it.activeMembership?.status?.name == "ACTIVE" }
                val expired = state.members.count { it.activeMembership?.status?.name == "EXPIRED" }
                InfoChip("Total: ${state.members.size}", OnBackground)
                InfoChip("Active: $active", Success)
                InfoChip("Expired: $expired", Error)
            }

            if (state.isLoading && state.members.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Member", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                            Text("Email", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                            Text("Phone", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Subscription", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                            Text("Status", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("Since", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider()

                        val filtered = state.filteredMembers
                        if (filtered.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No members found.", color = OnSurfaceVariant)
                            }
                        } else {
                            filtered.forEachIndexed { i, member ->
                                MemberRow(member, onClick = { vm.selectMember(member) })
                                if (i < filtered.size - 1) HorizontalDivider(color = Color.LightGray.copy(0.4f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Member detail dialog
    state.selectedMember?.let { member ->
        MemberDetailDialog(member = member, onDismiss = { vm.selectMember(null) })
    }
}

@Composable
private fun MemberRow(member: Member, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name with avatar
        Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text("${member.firstName.first()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(member.fullName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        Text(member.email, fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(2f))
        Text(member.phone.ifBlank { "–" }, fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
        val sub = member.activeMembership
        Text(
            sub?.planName ?: "No subscription",
            fontSize = 13.sp,
            color = if (sub != null) OnSurface else OnSurfaceVariant,
            modifier = Modifier.weight(2f)
        )
        Box(modifier = Modifier.weight(1f)) {
            if (sub != null) SubscriptionStatusBadge(sub.status)
            else Text("–", color = OnSurfaceVariant, fontSize = 13.sp)
        }
        Text(sub?.startDate ?: "–", fontSize = 13.sp, color = OnSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MemberDetailDialog(member: Member, onDismiss: () -> Unit) {
    val sub = member.activeMembership

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Primary), contentAlignment = Alignment.Center) {
                    Text("${member.firstName.first()}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(member.fullName)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Email", member.email)
                DetailRow("Phone", member.phone.ifBlank { "Not provided" })
                HorizontalDivider()
                Text("Subscription", fontWeight = FontWeight.SemiBold)
                if (sub != null) {
                    DetailRow("Plan", sub.planName)
                    DetailRow("Type", if (sub.type == MembershipType.BASIC) "Basic" else "Unlimited")
                    DetailRow("Billing", if (sub.billingCycle == BillingCycle.MONTHLY) "Monthly" else "Yearly")
                    DetailRow("Start Date", sub.startDate)
                    DetailRow("End Date", sub.endDate ?: "Ongoing")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Status:", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.width(100.dp))
                        SubscriptionStatusBadge(sub.status)
                    }
                } else {
                    Text("No active subscription", color = OnSurfaceVariant)
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label:", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 14.sp)
    }
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
