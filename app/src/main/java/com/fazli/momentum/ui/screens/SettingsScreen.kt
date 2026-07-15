package com.fazli.momentum.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fazli.momentum.MomentumApplication
import com.fazli.momentum.data.AppTheme
import com.fazli.momentum.data.Pillar
import com.fazli.momentum.data.Task
import com.fazli.momentum.ui.components.TaskFormDialog
import com.fazli.momentum.ui.theme.colorSchemeFor
import com.fazli.momentum.ui.theme.labelFor
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MomentumApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(app.repository, app.settingsRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (uiState.isLoading) return

    var showTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var defaultPillarIdForNewTask by remember { mutableStateOf<String?>(null) }
    var taskPendingDelete by remember { mutableStateOf<Task?>(null) }
    var addingPillar by remember { mutableStateOf(false) }
    var renamingPillar by remember { mutableStateOf<Pillar?>(null) }
    var pillarPendingDelete by remember { mutableStateOf<Pillar?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) {
                runCatching { viewModel.importData(json) }
                    .onSuccess { statusMessage = "Import berhasil." }
                    .onFailure { statusMessage = "Import gagal: file tidak valid." }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle("Tema") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTheme.entries.forEach { theme ->
                    ThemeOptionCard(
                        theme = theme,
                        selected = theme == uiState.theme,
                        onClick = { viewModel.setTheme(theme) }
                    )
                }
            }
        }

        item { SectionTitle("Kelola Pilar & Task") }
        items(uiState.pillars, key = { it.id }) { pillar ->
            val tasks = uiState.tasksByPillar[pillar].orEmpty()
            Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(pillar.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { renamingPillar = pillar }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Ubah nama pilar", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { pillarPendingDelete = pillar }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus pilar", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (tasks.isEmpty()) {
                        Text(
                            "Belum ada task.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        tasks.forEach { task ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Switch(checked = task.active, onCheckedChange = { viewModel.setTaskActive(task, it) })
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (task.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                )
                                IconButton(onClick = { editingTask = task; showTaskDialog = true }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit task", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { taskPendingDelete = task }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus task", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    TextButton(onClick = { editingTask = null; defaultPillarIdForNewTask = pillar.id; showTaskDialog = true }) {
                        Text("+ Tambah task")
                    }
                }
            }
        }
        item {
            TextButton(onClick = { addingPillar = true }) { Text("+ Tambah Pilar") }
        }

        item { SectionTitle("Reminder") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Reminder Harian", style = MaterialTheme.typography.bodyMedium)
                            Text(uiState.dailyReminderTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {
                            val parts = uiState.dailyReminderTime.split(":").mapNotNull { it.toIntOrNull() }
                            val hour = parts.getOrElse(0) { 8 }
                            val minute = parts.getOrElse(1) { 0 }
                            TimePickerDialog(context, { _, h, m ->
                                viewModel.setDailyReminderTime("%02d:%02d".format(h, m))
                            }, hour, minute, true).show()
                        }) { Text("Ubah") }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Reminder Weekly Review", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(checked = uiState.weeklyReviewReminderEnabled, onCheckedChange = viewModel::setWeeklyReviewReminderEnabled)
                    }
                }
            }
        }

        item { SectionTitle("Periode") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Tanggal Mulai", style = MaterialTheme.typography.bodyMedium)
                            Text(uiState.startDate.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day -> viewModel.setStartDate(LocalDate.of(year, month + 1, day)) },
                                uiState.startDate.year,
                                uiState.startDate.monthValue - 1,
                                uiState.startDate.dayOfMonth
                            ).show()
                        }) { Text("Ubah") }
                    }
                    var periodInput by remember(uiState.periodLengthDays) { mutableStateOf(uiState.periodLengthDays.toString()) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = periodInput,
                            onValueChange = { periodInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Panjang periode (hari)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { periodInput.toIntOrNull()?.takeIf { it > 0 }?.let(viewModel::setPeriodLengthDays) }) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }

        item { SectionTitle("Data") }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                val json = viewModel.exportData()
                                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                                val file = File(dir, "momentum_export_${LocalDate.now()}.json")
                                file.writeText(json)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export data Momentum"))
                            }
                        }
                    ) { Text("Export ke JSON") }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { importLauncher.launch("*/*") }
                    ) { Text("Import dari JSON") }
                    if (statusMessage != null) {
                        Text(statusMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { confirmReset = true }
                    ) { Text("Reset Progress") }
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskFormDialog(
            pillars = uiState.pillars,
            existing = editingTask,
            defaultPillarId = defaultPillarIdForNewTask,
            onDismiss = { showTaskDialog = false },
            onSave = { task ->
                if (editingTask == null) viewModel.addTask(task) else viewModel.updateTask(task)
                showTaskDialog = false
            }
        )
    }

    taskPendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskPendingDelete = null },
            title = { Text("Hapus task?") },
            text = { Text("\"${task.title}\" akan dihapus permanen.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteTask(task); taskPendingDelete = null }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { taskPendingDelete = null }) { Text("Batal") } }
        )
    }

    if (addingPillar) {
        PillarNameDialog(
            title = "Tambah Pilar",
            initialName = "",
            onDismiss = { addingPillar = false },
            onSave = { name -> viewModel.addPillar(name); addingPillar = false }
        )
    }

    renamingPillar?.let { pillar ->
        PillarNameDialog(
            title = "Ubah Nama Pilar",
            initialName = pillar.name,
            onDismiss = { renamingPillar = null },
            onSave = { name -> viewModel.renamePillar(pillar, name); renamingPillar = null }
        )
    }

    pillarPendingDelete?.let { pillar ->
        val taskCount = uiState.tasksByPillar[pillar].orEmpty().size
        AlertDialog(
            onDismissRequest = { pillarPendingDelete = null },
            title = { Text("Hapus pilar?") },
            text = { Text("\"${pillar.name}\" beserta $taskCount task di dalamnya akan dihapus permanen.") },
            confirmButton = { TextButton(onClick = { viewModel.deletePillar(pillar); pillarPendingDelete = null }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { pillarPendingDelete = null }) { Text("Batal") } }
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("Semua riwayat centang, review mingguan, catatan harian, dan progress counter akan dihapus. Centang milestone direset ke belum selesai, dan tanggal mulai periode diset ulang ke hari ini. Pilar & task tidak ikut terhapus. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = { TextButton(onClick = { viewModel.resetProgress(); confirmReset = false }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ThemeOptionCard(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val colors = colorSchemeFor(theme)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(colors.background, colors.primary, colors.secondary).forEach { swatch ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(1.dp, colors.outline, CircleShape)
                )
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(labelFor(theme), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = "Terpilih", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PillarNameDialog(title: String, initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama pilar") }, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim()) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
