package com.example.learning.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learning.data.CourseRepository
import com.example.learning.domain.Course
import com.example.learning.domain.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class CourseDetailViewModel(private val courseId: Int, private val repo: CourseRepository) : ViewModel() {
    private val load = MutableStateFlow<String?>(null)

    val state: StateFlow<CourseDetailUiState> =
        combine(repo.observeCourse(courseId), repo.observeLessons(courseId), load) { course, lessons, l ->
            CourseDetailUiState(
                course = course,
                lessons = lessons,
                isLoading = lessons.isEmpty() && l == null,
                error = if (lessons.isEmpty() && !l.isNullOrEmpty()) l else null,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState())

    init { load() }

    fun load() {
        viewModelScope.launch {
            load.value = null
            load.value = repo.ensureLessons(courseId).fold({ "" }, { it.message ?: "Failed to load lessons" })
        }
    }

    fun markCompleted(lesson: Lesson) {
        if (lesson.completed) return
        viewModelScope.launch { repo.markLessonCompleted(lesson) }
    }
}
