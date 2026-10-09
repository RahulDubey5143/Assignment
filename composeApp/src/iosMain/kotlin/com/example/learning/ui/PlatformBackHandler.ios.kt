package com.example.learning.ui

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) { /* iOS uses on-screen back button / swipe handled by host */ }
