package com.example.learning

import androidx.compose.ui.window.ComposeUIViewController
import com.example.learning.data.local.DatabaseDriverFactory

private val container by lazy { AppContainer(DatabaseDriverFactory()) }

fun MainViewController() = ComposeUIViewController { App(container) }
