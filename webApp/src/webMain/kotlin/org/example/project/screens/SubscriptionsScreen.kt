package org.example.project.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import org.example.project.model.BillingCycle
import org.example.project.model.MembershipPrice
import org.example.project.model.MembershipType
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*

// Public marketing page — shows pricing info, perks, and an FAQ.
// Prices are hardcoded here (no backend call) because this is a read-only info page.
// The real prices at registration time come from GET /api/v1/memberships/prices (RegisterScreen).
// Every "Choose Plan" button just navigates to Route.Register.
@Composable
fun SubscriptionsScreen(navigator: Navigator) {
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(horizontal = 48.dp, vertical = 40.dp)) {
            Column {
                Text("Membership Plans", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("Choose the plan that fits your lifestyle. No commitment required for monthly plans.", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 40.dp)) {
            // Plan cards hardcoded prices
            val plans = listOf(
                MembershipPrice(MembershipType.BASIC,     BillingCycle.MONTHLY, 2900),
                MembershipPrice(MembershipType.BASIC,     BillingCycle.YEARLY,  29900),
                MembershipPrice(MembershipType.UNLIMITED, BillingCycle.MONTHLY, 5500),
                MembershipPrice(MembershipType.UNLIMITED, BillingCycle.YEARLY,  54900)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                // most populair mark
                plans.forEachIndexed { i, plan ->
                    SubscriptionCard(plan = plan, isPopular = i == 2, onSelect = { navigator.navigate(Route.Register) }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(48.dp))

            // perks section
            Text("Included in All Plans", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Access to all workout types", "Certified instructors", "Locker rooms & showers", "Mobile app access")
                        .forEach { PerkItem(it) }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Reserve classes in advance", "Join waiting lists", "View class history", "Profile management")
                        .forEach { PerkItem(it) }
                }
            }

            Spacer(Modifier.height(48.dp))

            // FAQ section with expandable answers
            Text("Frequently Asked Questions", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(Modifier.height(16.dp))
            val faqs = listOf(
                "Can I cancel my monthly plan?"            to "Yes, monthly plans can be cancelled at the end of any billing period.",
                "What happens when my yearly plan expires?" to "You'll receive a notification 6 weeks before expiry. You can renew via the app.",
                "Can I upgrade my plan?"                   to "Yes, you can upgrade from 2x/week to Unlimited at any time. The price difference is prorated.",
                "How do I book a class?"                   to "Download the SportClub app, log in, and reserve a spot in any available class up to 1 week in advance."
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                faqs.forEach { (question, answer) -> FaqItem(question, answer) }
            }
        }

        // Bottom CTA
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Start Your Journey Today", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navigator.navigate(Route.Register) }, colors = ButtonDefaults.buttonColors(containerColor = Secondary), shape = RoundedCornerShape(24.dp), modifier = Modifier.height(52.dp)) {
                    Text("Sign Up Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(plan: MembershipPrice, isPopular: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    // Calculate yearly savings vs. paying monthly (only relevant for yearly plans).
    val savings = if (plan.billingCycle == BillingCycle.YEARLY) {
        val monthlyPrice = if (plan.type == MembershipType.BASIC) 29.0 else 55.0
        (monthlyPrice * 12 - plan.priceEuros).toInt()
    } else 0

    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isPopular) Primary else Surface), elevation = CardDefaults.cardElevation(if (isPopular) 12.dp else 2.dp), border = if (!isPopular) BorderStroke(1.dp, Color.LightGray) else null) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isPopular) {
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Secondary).padding(horizontal = 10.dp, vertical = 3.dp)) {
                    Text("Most Popular", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
            // plan.name and plan.description are computed properties on MembershipPrice
            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isPopular) Color.White else OnBackground, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(if (plan.billingCycle == BillingCycle.MONTHLY) "Monthly" else "Yearly", fontSize = 13.sp, color = if (isPopular) Color.White.copy(0.7f) else OnSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("€${plan.priceEuros.toInt()}", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = if (isPopular) Color.White else Primary)
                Spacer(Modifier.width(4.dp))
                Text(if (plan.billingCycle == BillingCycle.MONTHLY) "/mo" else "/yr", fontSize = 16.sp, color = if (isPopular) Color.White.copy(0.7f) else OnSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            }
            if (savings > 0) {
                Text("Save €$savings per year", fontSize = 13.sp, color = if (isPopular) Secondary else Success)
            }
            Spacer(Modifier.height(16.dp))
            Text(plan.description, fontSize = 13.sp, color = if (isPopular) Color.White.copy(0.8f) else OnSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 18.sp)
            Spacer(Modifier.height(20.dp))
            Button(onClick = onSelect, colors = ButtonDefaults.buttonColors(containerColor = if (isPopular) Secondary else Primary, contentColor = Color.White), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Choose Plan", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Single perk row with a green checkmark icon.
@Composable
private fun PerkItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp), tint = Success)
        Text(text, fontSize = 15.sp)
    }
}

// Expandable FAQ item. Tapping toggles visibility of the answer.
@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) } // local toggle state

    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(question, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(if (expanded) "▲" else "▼", color = OnSurfaceVariant)
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(answer, fontSize = 14.sp, color = OnSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}
