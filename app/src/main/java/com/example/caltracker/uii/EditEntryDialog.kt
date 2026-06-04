package com.example.caltracker.uii

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.caltracker.data.FoodEntry
import com.example.caltracker.ui.theme.*

@Composable
fun EditEntryDialog(
    entry: FoodEntry,
    onSave: (FoodEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(entry.name) }
    var calories by remember { mutableStateOf(entry.calories.toString()) }
    var protein by remember { mutableStateOf(entry.protein.toInt().toString()) }
    var carbs by remember { mutableStateOf(entry.carbs.toInt().toString()) }
    var fat by remember { mutableStateOf(entry.fat.toInt().toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E1E2E),
                            Color(0xFF111118)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // ── Header ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Entry",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Food Name ─────────────────────────────────────────────
                EditField(
                    label = "Food Name",
                    value = name,
                    onValueChange = { name = it },
                    color = GreenAccent,
                    keyboardType = KeyboardType.Text
                )

                Spacer(Modifier.height(12.dp))

                // ── Calories ──────────────────────────────────────────────
                EditField(
                    label = "Calories",
                    value = calories,
                    onValueChange = { calories = it },
                    color = CalorieColor,
                    suffix = "kcal",
                    keyboardType = KeyboardType.Number
                )

                Spacer(Modifier.height(12.dp))

                // ── Macros Row ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditField(
                        label = "Protein",
                        value = protein,
                        onValueChange = { protein = it },
                        color = ProteinColor,
                        suffix = "g",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    EditField(
                        label = "Carbs",
                        value = carbs,
                        onValueChange = { carbs = it },
                        color = CarbsColor,
                        suffix = "g",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    EditField(
                        label = "Fat",
                        value = fat,
                        onValueChange = { fat = it },
                        color = FatColor,
                        suffix = "g",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Buttons ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, DividerColor
                        )
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            onSave(
                                entry.copy(
                                    name = name.ifBlank { entry.name },
                                    calories = calories.toIntOrNull()
                                        ?: entry.calories,
                                    protein = protein.toFloatOrNull()
                                        ?: entry.protein,
                                    carbs = carbs.toFloatOrNull()
                                        ?: entry.carbs,
                                    fat = fat.toFloatOrNull()
                                        ?: entry.fat
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Save",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── Edit Field ───────────────────────────────────────────────────────────────

@Composable
fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 12.sp) },
        suffix = if (suffix.isNotEmpty()) {
            { Text(suffix, color = TextMuted, fontSize = 12.sp) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = color,
            unfocusedBorderColor = DividerColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = color,
            focusedLabelColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    )
}