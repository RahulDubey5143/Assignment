package com.example.learning.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learning.data.CourseRepository
import com.example.learning.domain.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseListUiState {
    data object Loading : CourseListUiState
    data object Empty : CourseListUiState
    data class Error(val message: String) : CourseListUiState
    data class Success(val courses: List<Course>, val notice: String? = null, val isRefreshing: Boolean = false) : CourseListUiState
}

class CourseListViewModel(private val repo: CourseRepository) : ViewModel() {
    private sealed interface Refresh {
        data object Loading : Refresh
        data object Done : Refresh
        data class Failed(val message: String) : Refresh
    }

    private val refresh = MutableStateFlow<Refresh>(Refresh.Loading)

    val state: StateFlow<CourseListUiState> =
        combine(repo.observeCourses(), refresh) { courses, r ->
            when {
                courses.isNotEmpty() -> CourseListUiState.Success(
                    courses,
                    notice = if (r is Refresh.Failed) "Offline: showing saved courses" else null,
                    isRefreshing = r is Refresh.Loading,
                )
                r is Refresh.Loading -> CourseListUiState.Loading
                r is Refresh.Failed -> CourseListUiState.Error(r.message)
                else -> CourseListUiState.Empty
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseListUiState.Loading)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            refresh.value = Refresh.Loading
            refresh.value = repo.refreshCourses().fold(
                onSuccess = { Refresh.Done },
                onFailure = { Refresh.Failed(it.message ?: "Something went wrong") },
            )
        }
    }
}
