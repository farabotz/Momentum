package com.fazli.momentum.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fazli.momentum.MomentumApplication
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskRecurrence
import com.fazli.momentum.data.TaskTier
import com.fazli.momentum.ui.components.StatChip
import com.fazli.momentum.ui.components.TaskFormDialog
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MomentumApplication
    val viewModel: PlanViewModel = viewModel(
        factory = PlanViewModelFactory(app.repository, app.settingsRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    if (uiState.isLoading) return

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(visible = uiState.view == PlanView.HARIAN, enter = scaleIn(), exit = scaleOut()) {
                FloatingActionButton(onClick = {
                    editingTask = null
                    showTaskDialog = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah task")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding).padding(16.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PlanView.entries.forEachIndexed { index, view ->
                    SegmentedButton(
                        selected = uiState.view == view,
                        onClick = { viewModel.setView(view) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PlanView.entries.size)
                    ) {
                        Text(
                            when (view) {
                                PlanView.HARIAN -> "Harian"
                                PlanView.MINGGUAN -> "Mingguan"
                                PlanView.BULANAN -> "Bulanan"
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.view,
                transitionSpec = {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
                },
                label = "plan-view"
            ) { view ->
                when (view) {
                    PlanView.HARIAN -> HarianView(
                        uiState = uiState,
                        onToggle = { taskId, completed -> viewModel.toggleTask(taskId, uiState.today, completed) },
                        onEdit = { editingTask = it; showTaskDialog = true },
                        onDelete = { viewModel.deleteTask(it) },
                        onMove = { current, other -> viewModel.moveTask(current, other) }
                    )
                    PlanView.MINGGUAN -> MingguanView(uiState)
                    PlanView.BULANAN -> BulananView(uiState, onMilestoneToggle = viewModel::toggleMilestone)
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskFormDialog(
            pillars = uiState.pillars,
            existing = editingTask,
            onDismiss = { showTaskDialog = false },
            onSave = { task ->
                if (editingTask == null) viewModel.addTask(task) else viewModel.updateTask(task)
                showTaskDialog = false
            }
        )
    }
}

@Composable
private fun HarianView(
    uiState: PlanUiState,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onMove: (Task, Task) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (uiState.pillars.isEmpty()) {
            item {
                Text(
                    "Belum ada pilar. Tambah pilar dulu di Pengaturan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        uiState.pillars.forEach { pillar ->
            val tasks = uiState.todayTasksByPillar[pillar].orEmpty()
            item(key = pillar.id) {
                Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = pillar.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (tasks.isEmpty()) {
                            Text(
                                text = "Belum ada task di pilar ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            tasks.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.completed,
                                        onCheckedChange = { onToggle(item.task.id, it) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = if (item.task.tier == TaskTier.WAJIB) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                    Text(text = item.task.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { onMove(item.task, tasks[index - 1].task) }, enabled = index > 0) {
                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Naikkan urutan", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onMove(item.task, tasks[index + 1].task) }, enabled = index < tasks.size - 1) {
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Turunkan urutan", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onEdit(item.task) }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit task", modifier = Modifier.size(18.dp))
                                    }
                                    var confirmDelete by remember { mutableStateOf(false) }
                                    IconButton(onClick = { confirmDelete = true }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Hapus task", modifier = Modifier.size(18.dp))
                                    }
                                    if (confirmDelete) {
                                        AlertDialog(
                                            onDismissRequest = { confirmDelete = false },
                                            title = { Text("Hapus task?") },
                                            text = { Text("\"${item.task.title}\" akan dihapus permanen.") },
                                            confirmButton = {
                                                TextButton(onClick = { onDelete(item.task); confirmDelete = false }) { Text("Hapus") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { confirmDelete = false }) { Text("Batal") }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MingguanView(uiState: PlanUiState) {
    Column {
        StatChip(
            label = "WAJIB minggu ini",
            value = "${uiState.weekWajibDone} / ${uiState.weekWajibTotal}",
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.padding(top = 16.dp)) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.weekDays) { day ->
                    val isToday = day == uiState.today
                    val tasks = uiState.weekTasksByDay[day].orEmpty()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isToday) androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) else androidx.compose.material3.CardDefaults.cardColors()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("id", "ID")).replaceFirstChar { it.uppercase() } +
                                    " (${day.dayOfMonth}/${day.monthValue})",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (tasks.isEmpty()) {
                                Text("Tidak ada WAJIB terjadwal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                tasks.forEach { t ->
                                    Text("• ${t.title}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulananView(uiState: PlanUiState, onMilestoneToggle: (String, Boolean) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().size(240.dp)
            ) {
                items(uiState.monthDays) { day ->
                    val success = uiState.successDays.contains(day)
                    val isToday = day == uiState.today
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    success -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${day.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (success) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        item {
            Text("Milestone Bulan Ini", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        if (uiState.monthMilestones.isEmpty()) {
            item {
                Text("Belum ada milestone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(uiState.monthMilestones, key = { it.id }) { m ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = m.done, onCheckedChange = { onMilestoneToggle(m.id, it) })
                    Column {
                        Text(
                            text = m.title,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (m.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                        Text(m.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
