package com.example.learning.data

import com.example.learning.data.local.CourseLocalSource
import com.example.learning.domain.Course
import com.example.learning.domain.Lesson
import kotlin.coroutines.cancellation.CancellationException

class CourseRepository(private val api: CourseApi, private val local: CourseLocalSource) {
    fun observeCourses() = local.observeCourses()
    fun observeCourse(id: Int) = local.observeCourse(id)
    fun observeLessons(courseId: Int) = local.observeLessons(courseId)

    suspend fun refreshCourses(): Result<Unit> = runCatchingSafe {
        local.saveCourses(api.getCourses().map { Course(it.id, it.title, it.instructor, it.progress, it.lessons) })
    }

    suspend fun ensureLessons(courseId: Int): Result<Unit> = runCatchingSafe {
        if (!local.hasLessons(courseId)) {
            local.saveLessons(courseId, api.getLessons(courseId).map { Lesson(it.id, courseId, it.title, it.completed) })
        }
    }

    suspend fun markLessonCompleted(lesson: Lesson) = local.markLessonCompleted(lesson.courseId, lesson.id)

    private suspend inline fun runCatchingSafe(block: () -> Unit): Result<Unit> =
        try { Result.success(block()) } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
}

class AuthRepository(private val api: AuthApi) {
    suspend fun login(email: String, password: String): Result<Unit> =
        try { Result.success(api.login(email.trim(), password)) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { Result.failure(e) }
}
