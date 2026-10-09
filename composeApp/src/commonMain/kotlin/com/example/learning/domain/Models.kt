package com.example.learning.domain

import kotlin.math.roundToInt

data class Course(val id: Int, val title: String, val instructor: String, val progress: Int, val lessons: Int)

data class Lesson(val id: Int, val courseId: Int, val title: String, val completed: Boolean)

object ProgressCalculator {
    fun percent(completed: Int, total: Int): Int =
        if (total <= 0) 0 else (completed * 100f / total).roundToInt().coerceIn(0, 100)
}

object Validators {
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !emailRegex.matches(value.trim()) -> "Enter a valid email"
        else -> null
    }
    fun password(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }
}
