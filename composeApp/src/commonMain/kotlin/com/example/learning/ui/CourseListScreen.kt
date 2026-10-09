package com.example.learning.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learning.domain.Course
import com.example.learning.presentation.CourseListUiState
import com.example.learning.presentation.CourseListViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    vm: CourseListViewModel,
    offlineSwitch: MutableStateFlow<Boolean>,
    onCourseClick: (Int) -> Unit,
) {
    val state by vm.state.collectAsState()
    val offline by offlineSwitch.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = { Text("My Courses") }, actions = {
            Text("Offline", style = MaterialTheme.typography.labelSmall)
            Switch(offline, { offlineSwitch.value = it }, Modifier.padding(horizontal = 4.dp))
            TextButton(onClick = vm::refresh) { Text("Refresh") }
        })
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                CourseListUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                CourseListUiState.Empty -> Text("No courses available yet.", Modifier.align(Alignment.Center))
                is CourseListUiState.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = vm::refresh, modifier = Modifier.padding(top = 8.dp)) { Text("Retry") }
                }
                is CourseListUiState.Success -> Column {
                    if (s.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                    s.notice?.let {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                            Text(it, Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.courses, key = { it.id }) { CourseCard(it) { onCourseClick(it.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onContinue: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(course.instructor, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { course.progress / 100f }, modifier = Modifier.fillMaxWidth())
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${course.progress}% • ${course.lessons} lessons", style = MaterialTheme.typography.bodySmall)
                Button(onClick = onContinue) { Text("Continue") }
            }
        }
    }
}
