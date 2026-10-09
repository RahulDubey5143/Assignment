package com.example.learning

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.learning.presentation.*
import com.example.learning.ui.*

private sealed interface Screen {
    data object Login : Screen
    data object Dashboard : Screen
    data class Detail(val courseId: Int) : Screen
}

private val ScreenSaver = Saver<Screen, Int>(
    save = { when (it) { Screen.Login -> -1; Screen.Dashboard -> 0; is Screen.Detail -> it.courseId } },
    restore = { when { it < 0 -> Screen.Login; it == 0 -> Screen.Dashboard; else -> Screen.Detail(it) } },
)

@Composable
fun App(container: AppContainer) {
    MaterialTheme {
        var screen by rememberSaveable(stateSaver = ScreenSaver) { mutableStateOf<Screen>(Screen.Login) }

        PlatformBackHandler(enabled = screen is Screen.Detail) { screen = Screen.Dashboard }

        when (val s = screen) {
            Screen.Login -> LoginScreen(
                vm = viewModel(factory = viewModelFactory { initializer { LoginViewModel(container.authRepository) } }),
                onLoggedIn = { screen = Screen.Dashboard },
            )
            Screen.Dashboard -> CourseListScreen(
                vm = viewModel(factory = viewModelFactory { initializer { CourseListViewModel(container.courseRepository) } }),
                offlineSwitch = container.courseApi.offline,
                onCourseClick = { screen = Screen.Detail(it) },
            )
            is Screen.Detail -> CourseDetailScreen(
                vm = viewModel(key = "detail-${s.courseId}", factory = viewModelFactory {
                    initializer { CourseDetailViewModel(s.courseId, container.courseRepository) }
                }),
                onBack = { screen = Screen.Dashboard },
            )
        }
    }
}
