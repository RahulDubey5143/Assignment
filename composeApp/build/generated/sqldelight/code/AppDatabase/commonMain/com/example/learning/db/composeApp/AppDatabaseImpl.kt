package com.example.learning.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.learning.db.AppDatabase
import com.example.learning.db.LearningQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<AppDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = AppDatabaseImpl.Schema

internal fun KClass<AppDatabase>.newInstance(driver: SqlDriver): AppDatabase =
    AppDatabaseImpl(driver)

private class AppDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), AppDatabase {
  override val learningQueries: LearningQueries = LearningQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE CourseEntity (
          |    id INTEGER NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    instructor TEXT NOT NULL,
          |    progress INTEGER NOT NULL,
          |    lessonCount INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE LessonEntity (
          |    id INTEGER NOT NULL PRIMARY KEY,
          |    courseId INTEGER NOT NULL,
          |    title TEXT NOT NULL,
          |    completed INTEGER NOT NULL DEFAULT 0,
          |    position INTEGER NOT NULL,
          |    FOREIGN KEY (courseId) REFERENCES CourseEntity(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX lesson_course_idx ON LessonEntity(courseId)", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
