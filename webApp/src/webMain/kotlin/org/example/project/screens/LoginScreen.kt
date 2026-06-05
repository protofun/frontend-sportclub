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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ErrorBanner
import org.example.project.navigation.Navigator
import org.example.project.navigation.Route
import org.example.project.theme.*
import org.example.project.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navigator: Navigator, vm: AuthViewModel) {
    val state = vm.state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isAuthenticated) {
        val user = state.currentUser
        if (state.isAuthenticated && user != null) {
            navigator.login(user)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(440.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SC", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your SportClub account", fontSize = 14.sp, color = OnSurfaceVariant)
                Spacer(Modifier.height(32.dp))

                state.error?.let {
                    ErrorBanner(it) { vm.clearError() }
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedTextField(
                    email, { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    password, { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { vm.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { navigator.navigate(Route.Home) }) {
                        Text("← Back to Home", fontSize = 13.sp)
                    }
                    TextButton(onClick = { navigator.navigate(Route.Register) }) {
                        Text("Create Account", fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Demo credentials hint
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Demo credentials:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OnSurfaceVariant)
                        Text("Staff: admin@sportclub.nl / any password", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text("Instructor: instructor@sportclub.nl / any password", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}
