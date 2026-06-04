package com.example.caltracker.uii

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.data.FoodEntry
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color

// ─── Meal Types ───────────────────────────────────────────────────────────────

enum class MealType(val label: String, val emoji: String, val hourRange: IntRange) {
    BREAKFAST("Breakfast", "🌅", 5..10),
    LUNCH("Lunch", "☀️", 11..14),
    DINNER("Dinner", "🌙", 17..21),
    SNACKS("Snacks", "🍎", 0..23)
}

fun getMealType(timestamp: Long): MealType {
    val hour = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .hour
    return when (hour) {
        in MealType.BREAKFAST.hourRange -> MealType.BREAKFAST
        in MealType.LUNCH.hourRange -> MealType.LUNCH
        in MealType.DINNER.hourRange -> MealType.DINNER
        else -> MealType.SNACKS
    }
}

@Composable
fun DiaryScreen(viewModel: MainViewModel) {
    val state by viewModel.homeState.collectAsState()
    val entries = state.todayEntries
    val today = java.time.LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

    // Group entries by meal type
    val groupedEntries = remember(entries) {
        val map = mutableMapOf<MealType, MutableList<FoodEntry>>()
        MealType.values().forEach { map[it] = mutableListOf() }
        entries.forEach { entry ->
            val mealType = getMealType(entry.timestamp)
            map[mealType]?.add(entry)
        }
        map
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Food Diary",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(today, color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(16.dp))

            // Daily summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiaryStatCard(
                    label = "Calories",
                    value = "${state.totalCalories}",
                    unit = "kcal",
                    modifier = Modifier.weight(1f)
                )
                DiaryStatCard(
                    label = "Protein",
                    value = "${state.totalProtein.toInt()}",
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                DiaryStatCard(
                    label = "Carbs",
                    value = "${state.totalCarbs.toInt()}",
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
                DiaryStatCard(
                    label = "Fat",
                    value = "${state.totalFat.toInt()}",
                    unit = "g",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Divider(color = DividerColor, thickness = 0.5.dp)

        // ── Entries List ─────────────────────────────────────────────────────
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📔", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No entries today",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Log food to see it here", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show each meal group that has entries
                MealType.values().forEach { mealType ->
                    val mealEntries = groupedEntries[mealType] ?: emptyList()
                    if (mealEntries.isNotEmpty()) {
                        item {
                            MealGroupHeader(
                                mealType = mealType,
                                entries = mealEntries
                            )
                        }
                        items(mealEntries, key = { it.id }) { entry ->
                            DiaryEntryCard(
                                entry = entry,
                                onDelete = { viewModel.deleteFoodEntry(entry) },
                                onEdit = { updatedEntry -> viewModel.updateFoodEntry(updatedEntry) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
                item { Spacer(Modifier.height(70.dp)) }
            }
        }
    }
}

// ─── Meal Group Header ────────────────────────────────────────────────────────

@Composable
fun MealGroupHeader(mealType: MealType, entries: List<FoodEntry>) {
    val totalCals = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = entries.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = entries.sumOf { it.fat.toDouble() }.toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(mealType.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    mealType.label,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "P:${totalProtein.toInt()}g  C:${totalCarbs.toInt()}g  F:${totalFat.toInt()}g",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$totalCals",
                color = GreenAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("kcal", color = TextMuted, fontSize = 10.sp)
        }
    }
    Divider(color = DividerColor, thickness = 0.5.dp)
    Spacer(Modifier.height(4.dp))
}

// ─── Diary Stat Card ──────────────────────────────────────────────────────────

@Composable
fun DiaryStatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(unit, color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ─── Diary Entry Card ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiaryEntryCard(entry: FoodEntry, onDelete: () -> Unit, onEdit: (FoodEntry) -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                showDeleteConfirm = true
            }
            false
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val color = if (dismissState.dismissDirection ==
                DismissDirection.EndToStart)
                ErrorColor.copy(alpha = 0.85f)
            else
                Color.Transparent

            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue ==
                    DismissValue.Default) 0.8f else 1.1f,
                label = "icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(14.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        "Delete",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(SurfaceBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getMealType(entry.timestamp).emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MacroTag("P", "${entry.protein.toInt()}g", ProteinColor)
                            MacroTag("C", "${entry.carbs.toInt()}g", CarbsColor)
                            MacroTag("F", "${entry.fat.toInt()}g", FatColor)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatTime(entry.timestamp),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${entry.calories}",
                            color = GreenAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("kcal", color = TextMuted, fontSize = 10.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Edit button
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit",
                                    tint = ProteinColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Delete button
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = ErrorColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text("← swipe", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }
    )

    // Edit dialog
    if (showEditDialog) {
        EditEntryDialog(
            entry = entry,
            onSave = { updatedEntry ->
                onEdit(updatedEntry)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardBg,
            title = {
                Text(
                    "Delete Entry?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Remove ${entry.name} from your diary?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor
                    )
                ) { Text("Delete", color = TextPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ─── Macro Tag ────────────────────────────────────────────────────────────────

@Composable
fun MacroTag(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(" $value", color = color.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatTime(timestamp: Long): String {
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}