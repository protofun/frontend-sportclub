package org.example.project.member.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.member.navigation.MemberNavigator
import org.example.project.model.*
import org.example.project.util.todayDateString
import org.example.project.viewmodel.MemberSessionViewModel

private fun formatPrice(priceInCents: Int): String =
    "${priceInCents / 100}.${(priceInCents % 100).toString().padStart(2, '0')}"

// Membership plans don't change often, so they're hardcoded here rather than fetched.
private val availablePlans = listOf(
    MembershipPrice(MembershipType.BASIC, BillingCycle.MONTHLY, 2900),
    MembershipPrice(MembershipType.BASIC, BillingCycle.YEARLY, 29900),
    MembershipPrice(MembershipType.UNLIMITED, BillingCycle.MONTHLY, 5500),
    MembershipPrice(MembershipType.UNLIMITED, BillingCycle.YEARLY, 54900)
)

@Composable
fun MemberSubscriptionScreen(navigator: MemberNavigator, sessionVm: MemberSessionViewModel) {
    val state = sessionVm.state
    val sub = state.memberInfo?.activeMembership
    val scroll = rememberScrollState()
    var subscribingTo by remember { mutableStateOf<MembershipPrice?>(null) }
    var isUpgrading by remember { mutableStateOf(false) }

    LaunchedEffect(navigator.currentUser?.userId) {
        navigator.currentUser?.let { sessionVm.loadMemberInfo(it.userId) }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Column {
                Text("My Subscription", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Manage your SportClub membership", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (sub == null) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💳", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No active subscription", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Subscribe to start working out!", fontSize = 14.sp, color = Color(0xFF757575))
                    }
                }

                Text("Choose a plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                availablePlans.forEach { plan ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(plan.description, fontSize = 13.sp, color = Color(0xFF757575))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "€${formatPrice(plan.priceInCents)} / ${if (plan.billingCycle == BillingCycle.MONTHLY) "month" else "year"}",
                                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1565C0)
                                )
                            }
                            Button(
                                onClick = {
                                    subscribingTo = plan
                                    navigator.currentUser?.let { user ->
                                        sessionVm.subscribe(user.userId, plan.type, plan.billingCycle, todayDateString()) {
                                            subscribingTo = null
                                        }
                                    }
                                },
                                enabled = subscribingTo == null
                            ) { Text(if (subscribingTo == plan) "..." else "Subscribe") }
                        }
                    }
                }
            } else {
                val isActive = sub.status == MembershipStatus.ACTIVE

                // Current plan card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF1565C0) else Color(0xFF9E9E9E)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Current Plan", fontSize = 13.sp, color = Color.White.copy(0.8f))
                                Text(sub.planName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(0.2f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(sub.status.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Start Date", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                Text(sub.startDate, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            if (sub.endDate != null) {
                                Column {
                                    Text("End Date", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                    Text(sub.endDate, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                            Column {
                                Text("Billing", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                Text(sub.billingCycle.name, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Benefits
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Plan Benefits", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        val perks = when (sub.type) {
                            MembershipType.UNLIMITED -> listOf(
                                "Unlimited class reservations",
                                "Access to all workout types",
                                "Priority booking window",
                                "No weekly class limit"
                            )
                            MembershipType.BASIC -> listOf(
                                "Up to 2 classes per week",
                                "Access to all workout types",
                                "Standard booking window"
                            )
                        }
                        perks.forEach { perk ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(perk, fontSize = 14.sp)
                            }
                        }
                    }
                }

                if (sub.type == MembershipType.BASIC && isActive) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Upgrade to Unlimited", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Unlimited access to all classes from €55/month",
                                fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isUpgrading = true
                                    navigator.currentUser?.let { user ->
                                        sessionVm.upgrade(user.userId, sub.id) { isUpgrading = false }
                                    }
                                },
                                enabled = !isUpgrading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
                            ) { Text(if (isUpgrading) "Upgrading..." else "Upgrade Now") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
