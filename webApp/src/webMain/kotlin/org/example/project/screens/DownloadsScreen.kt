package org.example.project.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.browser.document
import kotlinx.browser.window
import org.example.project.theme.*
import org.w3c.dom.HTMLAnchorElement

private fun triggerDownload(filename: String, url: String) {
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = url
    a.download = filename
    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
}

@Composable
fun DownloadsScreen() {
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).background(Background)) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(PrimaryDark, Primary)))
                .padding(horizontal = 48.dp, vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Get the SportClub App",
                    fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Reserve classes, track your schedule and manage your membership — anywhere, anytime.",
                    fontSize = 16.sp, color = Color.White.copy(0.9f), textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text("Choose Your Platform", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = OnBackground)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PlatformCard(
                    icon = Icons.Default.Language,
                    platform = "Web",
                    description = "No download needed. Use SportClub directly in your browser — fully featured.",
                    buttonLabel = "Open Web App",
                    badge = "Any browser",
                    badgeColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    onClick = { window.location.hash = "#/app" }
                )

                PlatformCard(
                    icon = Icons.Default.Android,
                    platform = "Android",
                    description = "Install the SportClub app on your Android phone or tablet. Enable 'Install from unknown sources' in settings.",
                    buttonLabel = "Download APK (13 MB)",
                    badge = "Android 7.0+",
                    badgeColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { triggerDownload("SportClub-android.apk", "/downloads/SportClub-android.apk") }
                )

                PlatformCard(
                    icon = Icons.Default.Computer,
                    platform = "Windows",
                    description = "Install the SportClub desktop app on Windows using the MSI installer. Run the installer and follow the steps.",
                    buttonLabel = "Download MSI (61 MB)",
                    badge = "Windows 10/11",
                    badgeColor = Color(0xFF0078D4),
                    modifier = Modifier.weight(1f),
                    onClick = { triggerDownload("SportClub-desktop.msi", "/downloads/SportClub-desktop.msi") }
                )
            }

            // Install instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Installation Instructions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        InstructionBlock(
                            icon = Icons.Default.Android,
                            title = "Android",
                            steps = listOf(
                                "Download the APK via the button above",
                                "Open the APK file on your device",
                                "Allow installation from unknown sources if prompted",
                                "Follow the on-screen instructions"
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        InstructionBlock(
                            icon = Icons.Default.Computer,
                            title = "Windows",
                            steps = listOf(
                                "Download the MSI installer via the button above",
                                "Double-click the downloaded .msi file",
                                "Follow the installation wizard",
                                "Launch SportClub from the Start menu"
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // App features
            Text("What's in the App?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppFeatureCard(Icons.Default.CalendarMonth, "Class Schedule", "Browse and reserve spots in upcoming classes day by day", Modifier.weight(1f))
                AppFeatureCard(Icons.Default.FitnessCenter, "My Classes", "View your upcoming and past classes in one place", Modifier.weight(1f))
                AppFeatureCard(Icons.Default.CreditCard, "Membership", "See your subscription status and upgrade at any time", Modifier.weight(1f))
                AppFeatureCard(Icons.Default.HourglassBottom, "Waitlist", "Join the waitlist for full classes and get a spot when one opens", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlatformCard(
    icon: ImageVector,
    platform: String,
    description: String,
    buttonLabel: String,
    badge: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = badgeColor)
            Text(platform, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(badgeColor.copy(0.12f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(badge, fontSize = 11.sp, color = badgeColor, fontWeight = FontWeight.Medium)
            }
            Text(
                description, fontSize = 13.sp, color = OnSurfaceVariant,
                textAlign = TextAlign.Center, lineHeight = 19.sp
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun InstructionBlock(icon: ImageVector, title: String, steps: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Primary)
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        steps.forEachIndexed { i, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${i + 1}.", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(step, fontSize = 13.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppFeatureCard(icon: ImageVector, title: String, desc: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp), tint = Primary)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(desc, fontSize = 12.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 17.sp)
        }
    }
}
