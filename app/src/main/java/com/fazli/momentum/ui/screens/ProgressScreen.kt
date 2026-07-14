package com.fazli.momentum.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fazli.momentum.MomentumApplication
import com.fazli.momentum.data.ProgressCounter
import com.fazli.momentum.ui.components.StatChip

@Composable
fun ProgressScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MomentumApplication
    val viewModel: ProgressViewModel = viewModel(
        factory = ProgressViewModelFactory(app.repository, app.settingsRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) return

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle("Progress Counter") }
        items(uiState.counters, key = { it.id }) { counter ->
            CounterCard(counter, onStep = { viewModel.stepCounter(counter, it) }, onSet = { viewModel.setCounterValue(counter.id, it) })
        }

        item { SectionTitle("Auto-Tally 90 Hari") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip("Hari Sukses", "${uiState.totalSuccessDays}", Modifier.wrapContentWidth())
                StatChip("Rate", "${uiState.overallRate.toInt()}%", Modifier.wrapContentWidth())
                StatChip("Streak Terpanjang", "${uiState.longestStreak}", Modifier.wrapContentWidth())
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                uiState.bonusTallies.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { tally ->
                            StatChip(tally.label, "${tally.count}x", Modifier.wrapContentWidth())
                        }
                    }
                }
            }
        }

        item { SectionTitle("Milestone") }
        uiState.milestonesByMonth.forEach { (month, milestones) ->
            item(key = "month_$month") {
                Text("Bulan $month", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            items(milestones, key = { it.id }) { m ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = m.done, onCheckedChange = { viewModel.toggleMilestone(m.id, it) })
                    Column {
                        Text(m.title, style = MaterialTheme.typography.bodyMedium, textDecoration = if (m.done) TextDecoration.LineThrough else null)
                        Text(m.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { SectionTitle("Weekly Review") }
        item { WeeklyReviewForm(onSubmit = viewModel::submitReview) }
        items(uiState.reviews, key = { it.id }) { review ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(review.weekStartDate, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Menang: ${review.win}", style = MaterialTheme.typography.bodySmall)
                    Text("Macet: ${review.struggle}", style = MaterialTheme.typography.bodySmall)
                    Text("Penyesuaian: ${review.adjust}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun CounterCard(counter: ProgressCounter, onStep: (Int) -> Unit, onSet: (Int) -> Unit) {
    var editValue by remember(counter.id) { mutableStateOf(counter.currentValue.toString()) }
    val progress by animateFloatAsState(
        targetValue = if (counter.targetValue != null && counter.targetValue > 0) (counter.currentValue.toFloat() / counter.targetValue).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(400),
        label = "counter-progress"
    )

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(counter.label, style = MaterialTheme.typography.titleSmall)
            Text(
                text = if (counter.targetValue != null) "${counter.currentValue} / ${counter.targetValue}" else "${counter.currentValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (counter.targetValue != null) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = { onStep(-1) }) { Text("−", style = MaterialTheme.typography.titleLarge) }
                IconButton(onClick = { onStep(1) }) { Icon(Icons.Filled.Add, contentDescription = "Tambah") }
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.width(100.dp),
                    label = { Text("Set") }
                )
                IconButton(onClick = { editValue.toIntOrNull()?.let(onSet) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Simpan angka")
                }
            }
        }
    }
}

@Composable
private fun WeeklyReviewForm(onSubmit: (String, String, String) -> Unit) {
    var win by remember { mutableStateOf("") }
    var struggle by remember { mutableStateOf("") }
    var adjust by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = win, onValueChange = { win = it }, label = { Text("Menang minggu ini") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = struggle, onValueChange = { struggle = it }, label = { Text("Macet di mana") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = adjust, onValueChange = { adjust = it }, label = { Text("Penyesuaian") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    onSubmit(win, struggle, adjust)
                    win = ""; struggle = ""; adjust = ""
                },
                enabled = win.isNotBlank() || struggle.isNotBlank() || adjust.isNotBlank()
            ) { Text("Simpan Review") }
        }
    }
}
