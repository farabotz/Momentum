package com.fazli.momentum.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fazli.momentum.data.Pillar
import com.fazli.momentum.data.Task
import com.fazli.momentum.data.TaskRecurrence
import com.fazli.momentum.data.TaskTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormDialog(
    pillars: List<Pillar>,
    existing: Task?,
    defaultPillarId: String? = null,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var pillarId by remember { mutableStateOf(existing?.pillarId ?: defaultPillarId ?: pillars.firstOrNull()?.id ?: "") }
    var tier by remember { mutableStateOf(existing?.tier ?: TaskTier.WAJIB) }
    var recurrence by remember { mutableStateOf(existing?.recurrence ?: TaskRecurrence.DAILY) }
    var targetMinutes by remember { mutableStateOf(existing?.targetMinutes?.toString() ?: "") }
    var selectedDays by remember {
        mutableStateOf(existing?.daysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.toSet() ?: emptySet())
    }
    var pillarMenuExpanded by remember { mutableStateOf(false) }
    var recurrenceMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = pillarMenuExpanded, onExpandedChange = { pillarMenuExpanded = it }) {
                    OutlinedTextField(
                        value = pillars.firstOrNull { it.id == pillarId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pillarMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded = pillarMenuExpanded, onDismissRequest = { pillarMenuExpanded = false }) {
                        pillars.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { pillarId = p.id; pillarMenuExpanded = false })
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TaskTier.entries.forEach { t ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            RadioButton(selected = tier == t, onClick = { tier = t })
                            Text(t.name)
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = recurrenceMenuExpanded, onExpandedChange = { recurrenceMenuExpanded = it }) {
                    OutlinedTextField(
                        value = recurrence.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recurrence") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded = recurrenceMenuExpanded, onDismissRequest = { recurrenceMenuExpanded = false }) {
                        TaskRecurrence.entries.forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { recurrence = r; recurrenceMenuExpanded = false })
                        }
                    }
                }

                if (recurrence == TaskRecurrence.WEEKLY) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val labels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                        labels.forEachIndexed { idx, label ->
                            val dayNum = idx + 1
                            FilterChip(
                                selected = selectedDays.contains(dayNum),
                                onClick = {
                                    selectedDays = if (selectedDays.contains(dayNum)) selectedDays - dayNum else selectedDays + dayNum
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetMinutes,
                    onValueChange = { targetMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Target menit (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && pillarId.isNotBlank(),
                onClick = {
                    val task = Task(
                        id = existing?.id ?: "t_${System.currentTimeMillis()}",
                        title = title,
                        description = description,
                        pillarId = pillarId,
                        tier = tier,
                        recurrence = recurrence,
                        targetMinutes = targetMinutes.toIntOrNull(),
                        daysOfWeek = if (recurrence == TaskRecurrence.WEEKLY) selectedDays.sorted().joinToString(",") else null,
                        active = existing?.active ?: true,
                        order = existing?.order ?: 999,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(task)
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
