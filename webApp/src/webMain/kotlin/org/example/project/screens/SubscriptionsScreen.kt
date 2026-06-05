package org.example.project.screens

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
import org.example.project.model.BillingCycle
import org.example.project.model.SubscriptionPlan
import org.example.project.model.SubscriptionType
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*

@Composable
fun SubscriptionsScreen(navigator: Navigator) {
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(horizontal = 48.dp, vertical = 40.dp)) {
            Column {
                Text("Membership Plans", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Choose the plan that fits your lifestyle. No commitment required for monthly plans.",
                    fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 40.dp)) {
            // Plans
            val plans = listOf(
                SubscriptionPlan(1, SubscriptionType.TWO_PER_WEEK, BillingCycle.MONTHLY, 29.0, "2x per week", "Work out up to 2 times per week"),
                SubscriptionPlan(2, SubscriptionType.TWO_PER_WEEK, BillingCycle.YEARLY, 299.0, "2x per week", "Work out up to 2 times per week"),
                SubscriptionPlan(3, SubscriptionType.UNLIMITED, BillingCycle.MONTHLY, 55.0, "Unlimited", "Unlimited access to all classes"),
                SubscriptionPlan(4, SubscriptionType.UNLIMITED, BillingCycle.YEARLY, 549.0, "Unlimited", "Unlimited access to all classes")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                plans.forEachIndexed { i, plan ->
                    SubscriptionCard(
                        plan = plan,
                        isPopular = i == 2,
                        onSelect = { navigator.navigate(Route.Register) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Included in all plans
            Text("Included in All Plans", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                val perks = listOf(
                    "✅ Access to all workout types",
                    "✅ Certified instructors",
                    "✅ Locker rooms & showers",
                    "✅ Mobile app access"
                )
                val perks2 = listOf(
                    "✅ Reserve classes in advance",
                    "✅ Join waiting lists",
                    "✅ View class history",
                    "✅ Profile management"
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    perks.forEach { Text(it, fontSize = 15.sp) }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    perks2.forEach { Text(it, fontSize = 15.sp) }
                }
            }

            Spacer(Modifier.height(48.dp))

            // FAQ
            Text("Frequently Asked Questions", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(Modifier.height(16.dp))
            val faqs = listOf(
                "Can I cancel my monthly plan?" to "Yes, monthly plans can be cancelled at the end of any billing period.",
                "What happens when my yearly plan expires?" to "You'll receive a notification 6 weeks before expiry. You can renew via the app.",
                "Can I upgrade my plan?" to "Yes, you can upgrade from 2x/week to Unlimited at any time. The price difference is prorated.",
                "How do I book a class?" to "Download the SportClub app, log in, and reserve a spot in any available class up to 1 week in advance."
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                faqs.forEach { (q, a) -> FaqItem(q, a) }
            }
        }

        // CTA
        Box(
            modifier = Modifier.fillMaxWidth().background(Primary).padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Start Your Journey Today", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { navigator.navigate(Route.Register) },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Sign Up Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    plan: SubscriptionPlan,
    isPopular: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savings = if (plan.billingCycle == BillingCycle.YEARLY) {
        val monthly = if (plan.type == SubscriptionType.TWO_PER_WEEK) 29.0 else 55.0
        val yearlyIfMonthly = monthly * 12
        (yearlyIfMonthly - plan.priceEuros).toInt()
    } else 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPopular) Primary else Surface),
        elevation = CardDefaults.cardElevation(if (isPopular) 12.dp else 2.dp),
        border = if (!isPopular) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPopular) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Secondary).padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("Most Popular", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(
                plan.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isPopular) Color.White else OnBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (plan.billingCycle == BillingCycle.MONTHLY) "Monthly" else "Yearly",
                fontSize = 13.sp,
                color = if (isPopular) Color.White.copy(0.7f) else OnSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "€${plan.priceEuros.toInt()}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPopular) Color.White else Primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (plan.billingCycle == BillingCycle.MONTHLY) "/mo" else "/yr",
                    fontSize = 16.sp,
                    color = if (isPopular) Color.White.copy(0.7f) else OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            if (savings > 0) {
                Text("Save €$savings per year", fontSize = 13.sp, color = if (isPopular) Secondary else Success)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                plan.description,
                fontSize = 13.sp,
                color = if (isPopular) Color.White.copy(0.8f) else OnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) Secondary else Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Plan", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
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
