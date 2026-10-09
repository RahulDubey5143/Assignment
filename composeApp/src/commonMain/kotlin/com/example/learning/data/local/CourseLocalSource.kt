package com.example.learning.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.example.learning.db.AppDatabase
import com.example.learning.domain.Course
import com.example.learning.domain.Lesson
import com.example.learning.domain.ProgressCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

interface CourseLocalSource {
    fun observeCourses(): Flow<List<Course>>
    fun observeCourse(id: Int): Flow<Course?>
    fun observeLessons(courseId: Int): Flow<List<Lesson>>
    suspend fun saveCourses(courses: List<Course>)
    suspend fun hasLessons(courseId: Int): Boolean
    suspend fun saveLessons(courseId: Int, lessons: List<Lesson>)
    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)
}

class SqlDelightCourseLocalSource(driver: SqlDriver) : CourseLocalSource {
    private val q = AppDatabase(driver).learningQueries
    private val io = Dispatchers.Default

    override fun observeCourses() =
        q.selectAllCourses().asFlow().mapToList(io).map { rows ->
            rows.map { Course(it.id.toInt(), it.title, it.instructor, it.progress.toInt(), it.lessonCount.toInt()) }
        }

    override fun observeCourse(id: Int) =
        q.selectCourse(id.toLong()).asFlow().mapToOneOrNull(io).map { r ->
            r?.let { Course(it.id.toInt(), it.title, it.instructor, it.progress.toInt(), it.lessonCount.toInt()) }
        }

    override fun observeLessons(courseId: Int) =
        q.selectLessons(courseId.toLong()).asFlow().mapToList(io).map { rows ->
            rows.map { Lesson(it.id.toInt(), it.courseId.toInt(), it.title, it.completed == 1L) }
        }

    override suspend fun saveCourses(courses: List<Course>) = withContext(io) {
        q.transaction {
            courses.forEach { c ->
                q.insertCourseIfAbsent(c.id.toLong(), c.title, c.instructor, c.progress.toLong(), c.lessons.toLong())
                q.updateCourseInfo(c.title, c.instructor, c.lessons.toLong(), c.id.toLong())
            }
        }
    }

    override suspend fun hasLessons(courseId: Int) = withContext(io) {
        q.countLessons(courseId.toLong()).executeAsOne() > 0
    }

    override suspend fun saveLessons(courseId: Int, lessons: List<Lesson>) = withContext(io) {
        q.transaction {
            lessons.forEachIndexed { i, l ->
                q.insertLesson(l.id.toLong(), courseId.toLong(), l.title, if (l.completed) 1L else 0L, i.toLong())
            }
            recomputeProgress(courseId)
        }
    }

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) = withContext(io) {
        q.transaction {
            q.markLessonCompleted(lessonId.toLong())
            recomputeProgress(courseId)
        }
    }

    private fun recomputeProgress(courseId: Int) {
        val total = q.countLessons(courseId.toLong()).executeAsOne().toInt()
        val done = q.countCompleted(courseId.toLong()).executeAsOne().toInt()
        q.updateProgress(ProgressCalculator.percent(done, total).toLong(), courseId.toLong())
    }
}