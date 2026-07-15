package com.fazli.momentum.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fazli.momentum.MomentumApplication
import androidx.compose.ui.platform.LocalContext
import com.fazli.momentum.ui.components.StatChip
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MomentumApplication
    val viewModel: TodayViewModel = viewModel(
        factory = TodayViewModelFactory(app.repository, app.settingsRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) return

    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HARI KE-${uiState.dayNumber.coerceAtLeast(0)} / ${uiState.periodLengthDays.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.currentDate.format(dayFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            LinearProgressIndicator(
                progress = { (uiState.dayNumber.toFloat() / uiState.periodLengthDays.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "Streak",
                    value = "${uiState.streak} hari",
                    modifier = Modifier.wrapContentWidth()
                )
                StatChip(
                    label = "Completion Rate",
                    value = "${uiState.completionRate.toInt()}%",
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }

        if (uiState.showWarning) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2 hari terakhir WAJIB belum kecentang. Yuk mulai lagi hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            TaskCard(
                title = "WAJIB",
                tasks = uiState.wajibTasks,
                checkedColor = MaterialTheme.colorScheme.primary,
                onToggle = viewModel::toggleTask
            )
        }

        item {
            TaskCard(
                title = "BONUS",
                tasks = uiState.bonusTasks,
                checkedColor = MaterialTheme.colorScheme.secondary,
                onToggle = viewModel::toggleTask
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Catatan Hari Ini",
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedTextField(
                        value = uiState.journalEntry,
                        onValueChange = {
                            viewModel.updateJournal(it)
                            viewModel.saveJournal()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        placeholder = { Text("1-2 kalimat...") }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    title: String,
    tasks: List<TaskWithCompletion>,
    checkedColor: androidx.compose.ui.graphics.Color,
    onToggle: (String, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (tasks.isEmpty()) {
                Text(
                    text = "Belum ada task.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                tasks.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.completed,
                            onCheckedChange = { onToggle(item.task.id, it) },
                            colors = CheckboxDefaults.colors(checkedColor = checkedColor)
                        )
                        Text(text = item.task.title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
