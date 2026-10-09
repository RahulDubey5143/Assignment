package com.example.learning.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class NetworkException(message: String) : Exception(message)

@Serializable
data class CourseDto(val id: Int, val title: String, val instructor: String, val progress: Int, val lessons: Int)

@Serializable
data class LessonDto(val id: Int, val title: String, val completed: Boolean)

interface CourseApi {
    suspend fun getCourses(): List<CourseDto>
    suspend fun getLessons(courseId: Int): List<LessonDto>
}

interface AuthApi {
    suspend fun login(email: String, password: String)
}

class MockCourseApi(private val json: Json = Json { ignoreUnknownKeys = true }) : CourseApi {
    val offline = MutableStateFlow(false)

    private val coursesJson = """
        [
          {"id":1,"title":"Python Programming","instructor":"John Smith","progress":65,"lessons":20},
          {"id":2,"title":"Generative AI","instructor":"Sarah Williams","progress":40,"lessons":16},
          {"id":3,"title":"Full Stack Development","instructor":"David Brown","progress":25,"lessons":28}
        ]
    """.trimIndent()

    private val lessonNames = listOf(
        "Introduction", "Variables & Data Types", "Control Flow", "Functions", "Collections",
        "OOP", "Error Handling", "Modules", "Testing", "Project",
    )

    override suspend fun getCourses(): List<CourseDto> {
        simulateNetwork()
        return json.decodeFromString(coursesJson)
    }

    override suspend fun getLessons(courseId: Int): List<LessonDto> {
        simulateNetwork()
        val course = json.decodeFromString<List<CourseDto>>(coursesJson).first { it.id == courseId }
        val done = kotlin.math.round(course.lessons * course.progress / 100f).toInt()
        return (0 until course.lessons).map { i ->
            val suffix = if (i >= lessonNames.size) " Part ${i / lessonNames.size + 1}" else ""
            LessonDto(courseId * 1000 + i, lessonNames[i % lessonNames.size] + suffix, i < done)
        }
    }

    private suspend fun simulateNetwork() {
        delay(800)
        if (offline.value) throw NetworkException("No internet connection")
    }
}

class MockAuthApi : AuthApi {
    override suspend fun login(email: String, password: String) {
        delay(1000)
        if (password != "password123") throw IllegalArgumentException("Invalid email or password")
    }
}
