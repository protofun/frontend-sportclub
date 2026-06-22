package org.example.project.member.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.member.navigation.MemberNavigator
import org.example.project.viewmodel.AuthViewModel

// MemberLoginScreen on start app
@Composable
fun MemberLoginScreen(navigator: MemberNavigator, authVm: AuthViewModel) {
    val state = authVm.state
    // Local state fro input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // As soon as login succeeds and currentUser is set, it calls navigator.login()
    // to redirect the user to the correct starting screen
    LaunchedEffect(state.currentUser) {
        val user = state.currentUser
        if (user != null) navigator.login(user)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.width(360.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).background(Color(0xFF1565C0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("SC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }

                Text("Welcome back", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your SportClub account", fontSize = 14.sp, color = Color(0xFF757575))

                // Email input field
                OutlinedTextField(
                    email, { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Password field -> text = hidden
                OutlinedTextField(
                    password, { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Show an error message if login fails
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Login button
                Button(
                    onClick = { authVm.login(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = email.isNotBlank() && password.isNotBlank() && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Sign In", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
