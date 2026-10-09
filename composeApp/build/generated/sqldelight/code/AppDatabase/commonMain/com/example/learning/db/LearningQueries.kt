package com.example.learning.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class LearningQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAllCourses(mapper: (
    id: Long,
    title: String,
    instructor: String,
    progress: Long,
    lessonCount: Long,
  ) -> T): Query<T> = Query(-452_581_540, arrayOf("CourseEntity"), driver, "Learning.sq",
      "selectAllCourses",
      "SELECT CourseEntity.id, CourseEntity.title, CourseEntity.instructor, CourseEntity.progress, CourseEntity.lessonCount FROM CourseEntity ORDER BY id") {
      cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectAllCourses(): Query<CourseEntity> = selectAllCourses { id, title, instructor,
      progress, lessonCount ->
    CourseEntity(
      id,
      title,
      instructor,
      progress,
      lessonCount
    )
  }

  public fun <T : Any> selectCourse(id: Long, mapper: (
    id: Long,
    title: String,
    instructor: String,
    progress: Long,
    lessonCount: Long,
  ) -> T): Query<T> = SelectCourseQuery(id) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectCourse(id: Long): Query<CourseEntity> = selectCourse(id) { id_, title,
      instructor, progress, lessonCount ->
    CourseEntity(
      id_,
      title,
      instructor,
      progress,
      lessonCount
    )
  }

  public fun <T : Any> selectLessons(courseId: Long, mapper: (
    id: Long,
    courseId: Long,
    title: String,
    completed: Long,
    position: Long,
  ) -> T): Query<T> = SelectLessonsQuery(courseId) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getLong(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!
    )
  }

  public fun selectLessons(courseId: Long): Query<LessonEntity> = selectLessons(courseId) { id,
      courseId_, title, completed, position ->
    LessonEntity(
      id,
      courseId_,
      title,
      completed,
      position
    )
  }

  public fun countLessons(courseId: Long): Query<Long> = CountLessonsQuery(courseId) { cursor ->
    cursor.getLong(0)!!
  }

  public fun countCompleted(courseId: Long): Query<Long> = CountCompletedQuery(courseId) { cursor ->
    cursor.getLong(0)!!
  }

  public fun insertCourseIfAbsent(
    id: Long?,
    title: String,
    instructor: String,
    progress: Long,
    lessonCount: Long,
  ) {
    driver.execute(156_568_819,
        """INSERT OR IGNORE INTO CourseEntity(id, title, instructor, progress, lessonCount) VALUES (?, ?, ?, ?, ?)""",
        5) {
          bindLong(0, id)
          bindString(1, title)
          bindString(2, instructor)
          bindLong(3, progress)
          bindLong(4, lessonCount)
        }
    notifyQueries(156_568_819) { emit ->
      emit("CourseEntity")
    }
  }

  public fun updateCourseInfo(
    title: String,
    instructor: String,
    lessonCount: Long,
    id: Long,
  ) {
    driver.execute(-1_921_947_781,
        """UPDATE CourseEntity SET title = ?, instructor = ?, lessonCount = ? WHERE id = ?""", 4) {
          bindString(0, title)
          bindString(1, instructor)
          bindLong(2, lessonCount)
          bindLong(3, id)
        }
    notifyQueries(-1_921_947_781) { emit ->
      emit("CourseEntity")
    }
  }

  public fun updateProgress(progress: Long, id: Long) {
    driver.execute(-111_816_001, """UPDATE CourseEntity SET progress = ? WHERE id = ?""", 2) {
          bindLong(0, progress)
          bindLong(1, id)
        }
    notifyQueries(-111_816_001) { emit ->
      emit("CourseEntity")
    }
  }

  public fun insertLesson(
    id: Long?,
    courseId: Long,
    title: String,
    completed: Long,
    position: Long,
  ) {
    driver.execute(1_272_844_346,
        """INSERT OR REPLACE INTO LessonEntity(id, courseId, title, completed, position) VALUES (?, ?, ?, ?, ?)""",
        5) {
          bindLong(0, id)
          bindLong(1, courseId)
          bindString(2, title)
          bindLong(3, completed)
          bindLong(4, position)
        }
    notifyQueries(1_272_844_346) { emit ->
      emit("LessonEntity")
    }
  }

  public fun markLessonCompleted(id: Long) {
    driver.execute(1_538_693_725, """UPDATE LessonEntity SET completed = 1 WHERE id = ?""", 1) {
          bindLong(0, id)
        }
    notifyQueries(1_538_693_725) { emit ->
      emit("LessonEntity")
    }
  }

  private inner class SelectCourseQuery<out T : Any>(
    public val id: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CourseEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CourseEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_983_727_840,
        """SELECT CourseEntity.id, CourseEntity.title, CourseEntity.instructor, CourseEntity.progress, CourseEntity.lessonCount FROM CourseEntity WHERE id = ?""",
        mapper, 1) {
      bindLong(0, id)
    }

    override fun toString(): String = "Learning.sq:selectCourse"
  }

  private inner class SelectLessonsQuery<out T : Any>(
    public val courseId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("LessonEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("LessonEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(2_038_432_726,
        """SELECT LessonEntity.id, LessonEntity.courseId, LessonEntity.title, LessonEntity.completed, LessonEntity.position FROM LessonEntity WHERE courseId = ? ORDER BY position""",
        mapper, 1) {
      bindLong(0, courseId)
    }

    override fun toString(): String = "Learning.sq:selectLessons"
  }

  private inner class CountLessonsQuery<out T : Any>(
    public val courseId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("LessonEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("LessonEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_999_952_619,
        """SELECT COUNT(*) FROM LessonEntity WHERE courseId = ?""", mapper, 1) {
      bindLong(0, courseId)
    }

    override fun toString(): String = "Learning.sq:countLessons"
  }

  private inner class CountCompletedQuery<out T : Any>(
    public val courseId: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("LessonEntity", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("LessonEntity", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(407_360_165,
        """SELECT COUNT(*) FROM LessonEntity WHERE courseId = ? AND completed = 1""", mapper, 1) {
      bindLong(0, courseId)
    }

    override fun toString(): String = "Learning.sq:countCompleted"
  }
}
