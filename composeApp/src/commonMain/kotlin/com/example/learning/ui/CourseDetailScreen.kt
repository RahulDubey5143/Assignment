package com.example.learning.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learning.presentation.CourseDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(vm: CourseDetailViewModel, onBack: () -> Unit) {
    val s by vm.state.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(s.course?.title ?: "Course") },
            navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back") } },
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                s.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                s.error != null -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = vm::load) { Text("Retry") }
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        s.course?.let {
                            Text("Progress: ${it.progress}%", style = MaterialTheme.typography.titleMedium)
                            LinearProgressIndicator(
                                progress = { it.progress / 100f },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            )
                        }
                    }
                    items(s.lessons, key = { it.id }) { lesson ->
                        ListItem(
                            headlineContent = { Text(lesson.title) },
                            supportingContent = { Text(if (lesson.completed) "✓ Completed" else "○ Pending") },
                            trailingContent = {
                                if (!lesson.completed) OutlinedButton(onClick = { vm.markCompleted(lesson) }) { Text("Mark done") }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
