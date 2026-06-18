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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ErrorBanner
import org.example.project.model.BillingCycle
import org.example.project.model.MembershipPrice
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*
import org.example.project.viewmodel.RegistrationStep
import org.example.project.viewmodel.RegistrationViewModel

@Composable
fun RegisterScreen(navigator: Navigator, vm: RegistrationViewModel) {
    LaunchedEffect(Unit) { vm.loadPlans() }
    val state = vm.state
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Primary).padding(horizontal = 48.dp, vertical = 32.dp)) {
            Column {
                Text("Create Your Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Join SportClub and start your fitness journey", fontSize = 15.sp, color = Color.White.copy(0.8f))
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.widthIn(max = 640.dp).padding(32.dp)) {
                // Step indicator
                StepIndicator(state.step)
                Spacer(Modifier.height(24.dp))

                state.error?.let {
                    ErrorBanner(it) { vm.clearError() }
                    Spacer(Modifier.height(12.dp))
                }

                when (state.step) {
                    RegistrationStep.PERSONAL_INFO -> PersonalInfoStep(vm)
                    RegistrationStep.SUBSCRIPTION -> SubscriptionStep(vm)
                    RegistrationStep.PAYMENT -> PaymentStep(vm)
                    RegistrationStep.CONFIRMATION -> ConfirmationStep(navigator, vm)
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: RegistrationStep) {
    val steps = listOf("Personal Info", "Subscription", "Payment", "Confirmation")
    val currentIdx = current.ordinal

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        steps.forEachIndexed { i, label ->
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(when {
                                i < currentIdx -> Success
                                i == currentIdx -> Primary
                                else -> Color.LightGray
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (i < currentIdx) "✓" else "${i + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 11.sp, color = if (i <= currentIdx) Primary else OnSurfaceVariant)
                }
                if (i < steps.size - 1) {
                    Divider(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        color = if (i < currentIdx) Success else Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalInfoStep(vm: RegistrationViewModel) {
    val s = vm.state
    var firstName by remember { mutableStateOf(s.firstName) }
    var lastName by remember { mutableStateOf(s.lastName) }
    var email by remember { mutableStateOf(s.email) }
    var password by remember { mutableStateOf(s.password) }
    var confirmPassword by remember { mutableStateOf(s.confirmPassword) }

    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(firstName, { firstName = it }, label = { Text("First Name") }, modifier = Modifier.weight(1f))
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(email, { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
            Text("Create Password", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            OutlinedTextField(
                password, { password = it },
                label = { Text("Password (min. 8 characters)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                confirmPassword, { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    vm.updatePersonalInfo(firstName, lastName, email, password, confirmPassword)
                    vm.goToSubscription()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continue to Subscription →")
            }
        }
    }
}

@Composable
private fun SubscriptionStep(vm: RegistrationViewModel) {
    val plans = vm.state.plans
    val selected = vm.state.selectedPlan

    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Choose Your Plan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("Select the membership that works best for you", fontSize = 14.sp, color = OnSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            if (plans.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    plans.forEach { plan ->
                        PlanSelectCard(
                            plan = plan,
                            isSelected = selected == plan,
                            onClick = { vm.selectPlan(plan) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { vm.back() }) {
                Text("← Back")
            }
        }
    }
}

@Composable
private fun PlanSelectCard(plan: MembershipPrice, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryContainer else Surface),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Primary else Color.LightGray)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.name, fontWeight = FontWeight.SemiBold)
                Text(plan.description, fontSize = 13.sp, color = OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("€${plan.priceEuros.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                Text(if (plan.billingCycle == BillingCycle.MONTHLY) "/month" else "/year", fontSize = 12.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PaymentStep(vm: RegistrationViewModel) {
    val s = vm.state
    val plan = s.selectedPlan ?: return
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(s.startDate) }
    var paymentMethod by remember { mutableStateOf("ideal") }

    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Order summary
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Order Summary", fontWeight = FontWeight.SemiBold)
                        Text(plan.name, fontSize = 14.sp, color = OnSurfaceVariant)
                    }
                    Text("€${plan.priceEuros.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Primary)
                }
            }

            // Start date
            OutlinedTextField(
                startDate, { startDate = it; vm.setStartDate(it) },
                label = { Text("Start Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Payment method selector
            Text("Payment Method", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ideal" to "iDEAL", "wero" to "Wero", "card" to "Credit Card").forEach { (id, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (paymentMethod == id) PrimaryContainer else SurfaceVariant)
                            .border(1.dp, if (paymentMethod == id) Primary else Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { paymentMethod = id }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(label, fontSize = 13.sp, color = if (paymentMethod == id) Primary else OnSurface, fontWeight = if (paymentMethod == id) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }

            // Card fields (simulated)
            if (paymentMethod == "card") {
                OutlinedTextField(cardNumber, { cardNumber = it }, label = { Text("Card Number") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(cardHolder, { cardHolder = it }, label = { Text("Card Holder") }, modifier = Modifier.weight(2f))
                    OutlinedTextField(expiry, { expiry = it }, label = { Text("MM/YY") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(cvv, { cvv = it }, label = { Text("CVV") }, modifier = Modifier.weight(1f))
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), shape = RoundedCornerShape(8.dp)) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "You will be redirected to ${if (paymentMethod == "ideal") "iDEAL" else "Wero"} to complete payment.\n(Simulated – no actual payment will be made)",
                            fontSize = 13.sp, color = OnSurfaceVariant
                        )
                    }
                }
            }

            if (s.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Button(
                    onClick = { vm.submitRegistration() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Text("Complete Registration – €${plan.priceEuros.toInt()}", fontWeight = FontWeight.Bold)
                }
            }
            TextButton(onClick = { vm.back() }) { Text("← Back") }
        }
    }
}

@Composable
private fun ConfirmationStep(navigator: Navigator, vm: RegistrationViewModel) {
    val user = vm.state.registeredUser

    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Success)
            Spacer(Modifier.height(16.dp))
            Text("Welcome to SportClub!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Your registration was successful${if (user != null) ", ${user.fullName}" else ""}!\nA confirmation email has been sent to ${user?.email ?: "your email address"}.",
                fontSize = 15.sp, color = OnSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Next Steps:", fontWeight = FontWeight.SemiBold)
                    Text("1. Download the SportClub app on your phone")
                    Text("2. Log in with your email and password")
                    Text("3. Browse classes and make your first reservation!")
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navigator.navigate(Route.Schedule) }) {
                    Text("View Schedule")
                }
                Button(onClick = { navigator.navigate(Route.Home) }) {
                    Text("Back to Home")
                }
            }
        }
    }
}
