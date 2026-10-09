package com.example.learning.db

import kotlin.Long
import kotlin.String

public data class CourseEntity(
  public val id: Long,
  public val title: String,
  public val instructor: String,
  public val progress: Long,
  public val lessonCount: Long,
)
