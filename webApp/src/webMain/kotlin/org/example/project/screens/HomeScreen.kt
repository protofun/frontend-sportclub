package org.example.project.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*
import org.example.project.viewmodel.ScheduleViewModel

@Composable
fun HomeScreen(navigator: Navigator, scheduleVm: ScheduleViewModel) {
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) { scheduleVm.load() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll)) {
        HeroSection(navigator)
        FeaturesSection()
        WorkoutTypesSection(navigator)
        UpcomingLessonsSection(navigator, scheduleVm)
        PricingPreviewSection(navigator)
        CtaSection(navigator)
        AppDownloadSection(navigator)
        FooterSection()
    }
}

@Composable
private fun HeroSection(navigator: Navigator) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(Brush.linearGradient(listOf(PrimaryDark, Primary, PrimaryLight)))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Train. Grow. Achieve.",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Join our community and discover a healthier, stronger you.\nProfessional classes for every fitness level.",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navigator.navigate(Route.Register) },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Become a Member", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { navigator.navigate(Route.Schedule) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("View Schedule", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Why Choose SportClub?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FeatureCard("🏋️", "Expert Instructors", "Learn from certified professionals with years of experience", Modifier.weight(1f))
            FeatureCard("📅", "Flexible Schedule", "Over 50 classes per week across all our facilities", Modifier.weight(1f))
            FeatureCard("🎯", "All Levels Welcome", "From beginner to advanced – we have classes for everyone", Modifier.weight(1f))
            FeatureCard("💪", "Modern Facilities", "State-of-the-art equipment and comfortable spaces", Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeatureCard(icon: String, title: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 36.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(desc, fontSize = 14.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun WorkoutTypesSection(navigator: Navigator) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Our Classes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        Text("Something for everyone", fontSize = 16.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val workoutTypes = listOf(
                Triple("🧘", "Yoga", Primary),
                Triple("🥊", "Boxing", Error),
                Triple("🚴", "Spinning", Secondary),
                Triple("🏃", "Bootcamp", Success),
                Triple("💪", "Body Shape", Color(0xFF9C27B0)),
                Triple("⚡", "Club Power", Warning)
            )
            workoutTypes.forEach { (emoji, name, color) ->
                WorkoutTypeChip(emoji, name, color, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = { navigator.navigate(Route.Schedule) }) {
            Text("See full schedule →", color = Primary, fontSize = 16.sp)
        }
    }
}

@Composable
private fun WorkoutTypeChip(emoji: String, name: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun UpcomingLessonsSection(navigator: Navigator, scheduleVm: ScheduleViewModel) {
    val state = scheduleVm.state
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant)
            .padding(horizontal = 48.dp, vertical = 48.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Today's Classes", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            TextButton(onClick = { navigator.navigate(Route.Schedule) }) {
                Text("View all →", color = Primary)
            }
        }
        Spacer(Modifier.height(16.dp))
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val todayLessons = state.lessons.take(4)
            if (todayLessons.isEmpty()) {
                Text("No classes scheduled for today.", color = OnSurfaceVariant)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    todayLessons.forEach { lesson ->
                        MiniLessonCard(lesson, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniLessonCard(lesson: org.example.project.model.Lesson, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(lesson.workoutName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(org.example.project.components.formatTime(lesson.startTime), fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Medium)
            Text(lesson.locationName, fontSize = 13.sp, color = OnSurfaceVariant)
            Text(lesson.instructorName ?: "TBD", fontSize = 12.sp, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            org.example.project.components.OccupancyBadge(lesson.enrolledCount, lesson.maxCapacity)
        }
    }
}

@Composable
private fun PricingPreviewSection(navigator: Navigator) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Simple, Transparent Pricing", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        Text("No hidden fees. Cancel anytime (monthly plan).", fontSize = 16.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PriceTile("2x / week", "€29", "per month", false, Modifier.weight(1f))
            PriceTile("2x / week", "€299", "per year", false, Modifier.weight(1f))
            PriceTile("Unlimited", "€55", "per month", true, Modifier.weight(1f))
            PriceTile("Unlimited", "€549", "per year", true, Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navigator.navigate(Route.Register) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text("Get Started Today", fontSize = 16.sp)
        }
    }
}

@Composable
private fun PriceTile(title: String, price: String, period: String, popular: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (popular) Primary else Surface),
        elevation = CardDefaults.cardElevation(if (popular) 8.dp else 2.dp),
        border = if (popular) null else BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (popular) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Secondary).padding(horizontal = 8.dp, vertical = 2.dp)
                ) { Text("Popular", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
            }
            Text(title, fontSize = 14.sp, color = if (popular) Color.White.copy(alpha = 0.8f) else OnSurfaceVariant)
            Text(price, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (popular) Color.White else Primary)
            Text(period, fontSize = 13.sp, color = if (popular) Color.White.copy(alpha = 0.7f) else OnSurfaceVariant)
        }
    }
}

@Composable
private fun CtaSection(navigator: Navigator) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Secondary, Color(0xFFFF8F00))))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ready to Start Your Journey?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("Join hundreds of members already achieving their fitness goals.", fontSize = 16.sp, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { navigator.navigate(Route.Register) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Secondary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text("Register Now – It's Free", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AppDownloadSection(navigator: Navigator) {
    Column(
        modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(horizontal = 48.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Take SportClub With You", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        Spacer(Modifier.height(8.dp))
        Text("Available on Web, Android and Windows Desktop", fontSize = 16.sp, color = OnSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppPlatformBadge("🌐", "Web browser", Modifier.weight(1f))
            AppPlatformBadge("🤖", "Android APK", Modifier.weight(1f))
            AppPlatformBadge("🪟", "Windows .msi", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navigator.navigate(Route.Downloads) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Text("Download the App", fontSize = 16.sp)
        }
    }
}

@Composable
private fun AppPlatformBadge(icon: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FooterSection() {
    Box(
        modifier = Modifier.fillMaxWidth().background(OnBackground).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SportClub", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("© 2026 SportClub. All rights reserved.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}
