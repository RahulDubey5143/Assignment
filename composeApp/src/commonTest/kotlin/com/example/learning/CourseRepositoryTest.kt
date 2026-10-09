package com.example.learning

import com.example.learning.data.*
import com.example.learning.data.local.CourseLocalSource
import com.example.learning.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

private class FakeApi : CourseApi {
    var fail = false
    override suspend fun getCourses(): List<CourseDto> {
        if (fail) throw NetworkException("No internet")
        return listOf(CourseDto(1, "Python Programming", "John Smith", 65, 20))
    }
    override suspend fun getLessons(courseId: Int) = emptyList<LessonDto>()
}

private class FakeLocal : CourseLocalSource {
    val courses = MutableStateFlow<List<Course>>(emptyList())
    override fun observeCourses(): Flow<List<Course>> = courses
    override fun observeCourse(id: Int) = courses.map { l -> l.firstOrNull { it.id == id } }
    override fun observeLessons(courseId: Int) = flowOf(emptyList<Lesson>())
    override suspend fun saveCourses(courses: List<Course>) { this.courses.value = courses }
    override suspend fun hasLessons(courseId: Int) = false
    override suspend fun saveLessons(courseId: Int, lessons: List<Lesson>) {}
    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) {}
}

class CourseRepositoryTest {
    @Test
    fun cachedCoursesSurviveFailedRefresh_offlineScenario() = runTest {
        val api = FakeApi()
        val repo = CourseRepository(api, FakeLocal())

        assertTrue(repo.refreshCourses().isSuccess)
        api.fail = true // "turn off the internet"
        val result = repo.refreshCourses()

        assertTrue(result.isFailure)
        assertEquals("No internet", result.exceptionOrNull()?.message)
        assertEquals(listOf("Python Programming"), repo.observeCourses().first().map { it.title })
    }

    @Test
    fun progressIsRoundedPercentOfCompletedLessons() {
        assertEquals(65, ProgressCalculator.percent(13, 20))
        assertEquals(0, ProgressCalculator.percent(0, 0)) // no divide-by-zero
        assertEquals(100, ProgressCalculator.percent(16, 16))
        assertEquals(36, ProgressCalculator.percent(10, 28)) // 35.7 -> 36
    }
}
