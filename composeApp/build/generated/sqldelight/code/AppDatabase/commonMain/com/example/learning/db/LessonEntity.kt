package com.example.learning.db

import kotlin.Long
import kotlin.String

public data class LessonEntity(
  public val id: Long,
  public val courseId: Long,
  public val title: String,
  public val completed: Long,
  public val position: Long,
)
