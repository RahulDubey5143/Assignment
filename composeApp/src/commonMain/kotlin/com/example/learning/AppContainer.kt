package com.example.learning

import com.example.learning.data.AuthRepository
import com.example.learning.data.CourseRepository
import com.example.learning.data.MockAuthApi
import com.example.learning.data.MockCourseApi
import com.example.learning.data.local.DatabaseDriverFactory
import com.example.learning.data.local.SqlDelightCourseLocalSource

class AppContainer(driverFactory: DatabaseDriverFactory) {
    val courseApi = MockCourseApi()
    val courseRepository = CourseRepository(courseApi, SqlDelightCourseLocalSource(driverFactory.create()))
    val authRepository = AuthRepository(MockAuthApi())
}
