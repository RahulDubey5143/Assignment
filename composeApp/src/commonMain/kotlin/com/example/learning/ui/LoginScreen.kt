package com.example.learning.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.learning.presentation.LoginViewModel

@Composable
fun LoginScreen(vm: LoginViewModel, onLoggedIn: () -> Unit) {
    val s by vm.state.collectAsState()
    LaunchedEffect(s.loggedIn) { if (s.loggedIn) onLoggedIn() }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Learning Dashboard", style = MaterialTheme.typography.headlineMedium)
        Text("Demo: any valid email / password123", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = s.email, onValueChange = vm::onEmailChange, label = { Text("Email") },
            isError = s.emailError != null, supportingText = { s.emailError?.let { Text(it) } },
            singleLine = true, enabled = !s.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = s.password, onValueChange = vm::onPasswordChange, label = { Text("Password") },
            isError = s.passwordError != null, supportingText = { s.passwordError?.let { Text(it) } },
            singleLine = true, enabled = !s.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = vm::login, enabled = !s.isLoading, modifier = Modifier.fillMaxWidth()) {
            if (s.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Login")
        }
    }
}
