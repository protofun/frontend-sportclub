package org.example.project.member.screens

import androidx.compose.foundation.BorderStroke
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

private val availablePlans = listOf(
    MembershipPrice(MembershipType.BASIC,     BillingCycle.MONTHLY, 2900),
    MembershipPrice(MembershipType.BASIC,     BillingCycle.YEARLY,  29900),
    MembershipPrice(MembershipType.UNLIMITED, BillingCycle.MONTHLY, 5500),
    MembershipPrice(MembershipType.UNLIMITED, BillingCycle.YEARLY,  54900)
)

@Composable
fun MemberSubscriptionScreen(navigator: MemberNavigator, sessionVm: MemberSessionViewModel) {
    val state    = sessionVm.state
    val sub      = state.memberInfo?.activeMembership
    val upcoming = state.upcomingMembership
    val userId   = navigator.currentUser?.userId
    val scroll = rememberScrollState()

    var pendingPlan       by remember { mutableStateOf<MembershipPrice?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showCancelDialog  by remember { mutableStateOf(false) }
    var isBusy            by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let {
            sessionVm.loadMemberInfo(it)
            sessionVm.loadNotifications()
        }
    }

    // Neppe betaling: nieuw abonnement
    pendingPlan?.let { plan ->
        val startDate = if (sub != null && sub.billingCycle == BillingCycle.YEARLY
            && sub.status == MembershipStatus.ACTIVE && sub.endDate != null)
            sub.endDate.take(10)
        else
            todayDateString()

        FakePaymentDialog(
            title       = "Subscribe to ${plan.name}",
            description = "You will be charged €${formatPrice(plan.priceInCents)} " +
                    if (plan.billingCycle == BillingCycle.MONTHLY) "per month."
                    else "per year. Starts on $startDate.",
            amount      = plan.priceInCents,
            isBusy      = isBusy,
            onConfirm   = {
                isBusy = true
                userId?.let { uid ->
                    sessionVm.subscribe(uid, plan.type, plan.billingCycle, startDate) {
                        isBusy = false
                        pendingPlan = null
                    }
                }
            },
            onDismiss   = { if (!isBusy) pendingPlan = null }
        )
    }

    // Neppe betaling: upgrade
    if (showUpgradeDialog && sub != null) {
        val upgradeAmount = when (sub.billingCycle) {
            BillingCycle.MONTHLY -> 5500 - 2900
            BillingCycle.YEARLY  -> {
                // Pro-rata: verschil * resterende maanden / 12 (vereenvoudigd op 6 resterende maanden)
                val diff = 54900 - 29900
                diff / 2
            }
        }
        val upgradeDesc = when (sub.billingCycle) {
            BillingCycle.MONTHLY -> "Your monthly fee increases from €29.00 to €55.00."
            BillingCycle.YEARLY  -> "Pro-rata amount for the remaining period of your yearly plan (approx. €${formatPrice(upgradeAmount)})."
        }
        FakePaymentDialog(
            title       = "Upgrade to Unlimited",
            description = upgradeDesc,
            amount      = upgradeAmount,
            isBusy      = isBusy,
            onConfirm   = {
                isBusy = true
                userId?.let { uid ->
                    sessionVm.upgrade(uid, sub.id) {
                        isBusy = false
                        showUpgradeDialog = false
                    }
                }
            },
            onDismiss   = { if (!isBusy) showUpgradeDialog = false }
        )
    }

    // Opzeg-bevestiging
    if (showCancelDialog && sub != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Subscription") },
            text  = {
                Text(
                    "Are you sure you want to cancel your ${sub.planName} subscription? " +
                    "It remains active until ${sub.endDate?.take(10) ?: "end of period"}."
                )
            },
            confirmButton = {
                Button(
                    onClick  = {
                        isBusy = true
                        userId?.let { uid ->
                            sessionVm.cancelMembership(sub.id, uid) {
                                isBusy = false
                                showCancelDialog = false
                            }
                        }
                    },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = !isBusy
                ) { Text(if (isBusy) "Cancelling..." else "Yes, cancel") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep subscription") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Color(0xFFF8F9FA))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1565C0)).padding(20.dp)) {
            Column {
                Text("My Subscription", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Manage your SportClub membership", fontSize = 13.sp, color = Color.White.copy(0.8f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── In-app notificaties (verloopalerts) ───────────────────────────────
            state.notifications.filter { !it.isRead }.forEach { notif ->
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔔", fontSize = 20.sp)
                        Text(notif.message, fontSize = 13.sp, color = Color(0xFF5D4037), modifier = Modifier.weight(1f))
                        TextButton(onClick = { sessionVm.markNotificationRead(notif.id) }) {
                            Text("Dismiss", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ── Geen abonnement ───────────────────────────────────────────────────
            if (sub == null) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💳", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No active subscription", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Subscribe to start working out!", fontSize = 14.sp, color = Color(0xFF757575))
                    }
                }
                Text("Choose a plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                availablePlans.forEach { plan ->
                    PlanCard(plan = plan, isBusy = isBusy, pendingPlan = pendingPlan) { pendingPlan = plan }
                }

            } else {
                val isActive    = sub.status == MembershipStatus.ACTIVE
                val isCancelled = sub.status == MembershipStatus.CANCELLED

                // ── Huidig abonnement ──────────────────────────────────────────
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF1565C0) else Color(0xFF9E9E9E)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text("Current Plan", fontSize = 13.sp, color = Color.White.copy(0.8f))
                                Text(sub.planName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.2f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(sub.status.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Start Date", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                Text(sub.startDate.take(10), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            sub.endDate?.let {
                                Column {
                                    Text("End Date", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                    Text(it.take(10), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                            Column {
                                Text("Billing", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                Text(sub.billingCycle.name, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // ── Opzeggen / Geannuleerd ────────────────────────────────────
                when {
                    isCancelled -> {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✅", fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Subscription cancelled", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                                    Text(
                                        "Your access remains active until ${sub.endDate?.take(10) ?: "end of period"}. " +
                                        "You can buy a new subscription after that date.",
                                        fontSize = 12.sp, color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                    isActive && sub.billingCycle == BillingCycle.MONTHLY -> {
                        OutlinedButton(
                            onClick   = { showCancelDialog = true },
                            modifier  = Modifier.fillMaxWidth(),
                            enabled   = !isBusy,
                            border    = BorderStroke(1.dp, Color(0xFFF44336))
                        ) {
                            Text("Cancel subscription", color = Color(0xFFF44336), fontSize = 14.sp)
                        }
                    }
                    isActive && sub.billingCycle == BillingCycle.YEARLY -> {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔒", fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cannot cancel yearly plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB71C1C))
                                    Text(
                                        "Yearly plans run until ${sub.endDate?.take(10) ?: "end of term"} and cannot be cancelled early.",
                                        fontSize = 12.sp, color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Voordelen ──────────────────────────────────────────────────
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Plan Benefits", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        val perks = when (sub.type) {
                            MembershipType.UNLIMITED -> listOf("Unlimited class reservations", "Access to all workout types", "Priority booking window", "No weekly class limit")
                            MembershipType.BASIC     -> listOf("Up to 2 classes per week", "Access to all workout types", "Standard booking window")
                        }
                        perks.forEach { perk ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(perk, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // ── Upgrade (Basic + Active) ───────────────────────────────────
                if (sub.type == MembershipType.BASIC && isActive) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Upgrade to Unlimited", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                when (sub.billingCycle) {
                                    BillingCycle.MONTHLY -> "Monthly fee increases from €29.00 to €55.00/month."
                                    BillingCycle.YEARLY  -> "Pay the pro-rata difference for the remaining period."
                                },
                                fontSize = 13.sp, color = Color(0xFF757575), textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { showUpgradeDialog = true },
                                enabled = !isBusy,
                                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
                            ) { Text("Upgrade Now") }
                        }
                    }
                }

                // ── Toekomstig abonnement (Pending) ───────────────────────────
                if (upcoming != null) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Upcoming subscription", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                            Text(
                                "${upcoming.planName} — starts on ${upcoming.startDate.take(10)}",
                                fontSize = 14.sp, color = Color(0xFF388E3C)
                            )
                            Text(
                                "This subscription activates automatically when your current plan ends.",
                                fontSize = 12.sp, color = Color(0xFF757575)
                            )
                        }
                    }
                }

                // ── Nieuw abonnement kopen (yearly, geen pending, niet geannuleerd) ──
                if (sub.billingCycle == BillingCycle.YEARLY && !isCancelled && upcoming == null) {
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Buy next subscription", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Your plan expires on ${sub.endDate?.take(10) ?: "—"}. " +
                                "Buy a new plan now and it starts automatically afterwards.",
                                fontSize = 13.sp, color = Color(0xFF757575)
                            )
                            availablePlans.forEach { plan ->
                                PlanCard(plan = plan, isBusy = isBusy, pendingPlan = pendingPlan) { pendingPlan = plan }
                            }
                        }
                    }
                }
            }

            // ── Foutmelding ───────────────────────────────────────────────────────
            state.error?.let { err ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(err, color = Color(0xFFC62828), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { sessionVm.dismissError() }) { Text("OK") }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlanCard(plan: MembershipPrice, isBusy: Boolean, pendingPlan: MembershipPrice?, onSelect: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(plan.description, fontSize = 13.sp, color = Color(0xFF757575))
                Spacer(Modifier.height(4.dp))
                Text(
                    "€${formatPrice(plan.priceInCents)} / ${if (plan.billingCycle == BillingCycle.MONTHLY) "month" else "year"}",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1565C0)
                )
            }
            Button(onClick = onSelect, enabled = pendingPlan == null && !isBusy) {
                Text(if (pendingPlan == plan) "..." else "Select")
            }
        }
    }
}

@Composable
fun FakePaymentDialog(
    title: String,
    description: String,
    amount: Int,
    isBusy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        shape = RoundedCornerShape(16.dp),
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(description, fontSize = 14.sp, color = Color(0xFF757575))

                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("SPORTCLUB CARD", fontSize = 11.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("•••• •••• •••• 4242", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CARD HOLDER", fontSize = 9.sp, color = Color.White.copy(0.6f))
                                Text("SportClub Member", fontSize = 12.sp, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("EXPIRES", fontSize = 9.sp, color = Color.White.copy(0.6f))
                                Text("12/28", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total due", fontSize = 14.sp, color = Color(0xFF757575))
                    Text("€${formatPrice(amount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }

                Text(
                    "⚠ This is a simulated payment. No real transaction takes place.",
                    fontSize = 11.sp, color = Color(0xFF9E9E9E)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isBusy,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) { Text(if (isBusy) "Processing..." else "Pay €${formatPrice(amount)}") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Cancel") }
        }
    )
}
