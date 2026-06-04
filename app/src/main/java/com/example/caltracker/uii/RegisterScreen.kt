package com.example.caltracker.uii

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.data.UserProfile
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel

@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    var currentStep by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("male") }
    var activityLevel by remember { mutableStateOf("moderate") }
    var goal by remember { mutableStateOf("maintain") }

    val totalSteps = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // ── App Logo ─────────────────────────────────────────────────────────
        Text("🥗", fontSize = 56.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "CalTracker",
            color = GreenAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "AI-Powered Nutrition Tracking",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(32.dp))

        // ── Progress Indicator ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSteps) { step ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (step <= currentStep) GreenAccent
                            else DividerColor,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Step ${currentStep + 1} of $totalSteps",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(32.dp))

        // ── Steps ────────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "step"
        ) { step ->
            when (step) {
                0 -> StepName(name = name, onNameChange = { name = it })
                1 -> StepBodyMetrics(
                    age = age, onAgeChange = { age = it },
                    weight = weightKg, onWeightChange = { weightKg = it },
                    height = heightCm, onHeightChange = { heightCm = it },
                    gender = gender, onGenderChange = { gender = it }
                )
                2 -> StepActivity(
                    activityLevel = activityLevel,
                    onActivityChange = { activityLevel = it }
                )
                3 -> StepGoal(
                    goal = goal,
                    onGoalChange = { goal = it }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Navigation Buttons ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextSecondary)
                    Spacer(Modifier.width(6.dp))
                    Text("Back", color = TextSecondary)
                }
            }

            Button(
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        // Save profile
                        viewModel.saveProfile(
                            UserProfile(
                                name = name.trim().ifBlank { "Friend" },
                                age = age.toIntOrNull() ?: 25,
                                weightKg = weightKg.toFloatOrNull() ?: 70f,
                                heightCm = heightCm.toFloatOrNull() ?: 170f,
                                gender = gender,
                                activityLevel = activityLevel,
                                goal = goal,
                                isProfileComplete = true
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f).height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (currentStep == totalSteps - 1) "Let's Go! 🚀" else "Next",
                    color = DarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (currentStep < totalSteps - 1) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = DarkBg
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─── Step 1: Name ─────────────────────────────────────────────────────────────

@Composable
fun StepName(name: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👋", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "What's your name?",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "We'll personalize your experience",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name", color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = DividerColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GreenAccent
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp,
                color = TextPrimary
            )
        )
    }
}

// ─── Step 2: Body Metrics ─────────────────────────────────────────────────────

@Composable
fun StepBodyMetrics(
    age: String, onAgeChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("📏", fontSize = 40.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Text(
            "Your body metrics",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Used to calculate your daily calorie needs",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        // Gender selector
        Text("Gender", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("male" to "👨 Male", "female" to "👩 Female").forEach { (value, label) ->
                val selected = gender == value
                OutlinedButton(
                    onClick = { onGenderChange(value) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (selected) GreenAccent else DividerColor
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selected)
                            GreenAccent.copy(alpha = 0.15f)
                        else
                            androidx.compose.ui.graphics.Color.Transparent
                    )
                ) {
                    Text(
                        label,
                        color = if (selected) GreenAccent else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Age, Weight, Height fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricField(
                label = "Age",
                value = age,
                unit = "yrs",
                onValueChange = onAgeChange,
                modifier = Modifier.weight(1f)
            )
            MetricField(
                label = "Weight",
                value = weight,
                unit = "kg",
                onValueChange = onWeightChange,
                modifier = Modifier.weight(1f)
            )
            MetricField(
                label = "Height",
                value = height,
                unit = "cm",
                onValueChange = onHeightChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricField(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 11.sp) },
        suffix = { Text(unit, color = TextMuted, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenAccent,
            unfocusedBorderColor = DividerColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = GreenAccent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

// ─── Step 3: Activity Level ───────────────────────────────────────────────────

@Composable
fun StepActivity(activityLevel: String, onActivityChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("🏃", fontSize = 40.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Text(
            "Activity level",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "How active are you on a typical day?",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        val activities = listOf(
            Triple("sedentary", "🪑 Sedentary", "Little or no exercise"),
            Triple("light", "🚶 Light", "Exercise 1-3 days/week"),
            Triple("moderate", "🏃 Moderate", "Exercise 3-5 days/week"),
            Triple("active", "💪 Active", "Exercise 6-7 days/week"),
            Triple("very_active", "🔥 Very Active", "Hard exercise daily")
        )

        activities.forEach { (value, label, desc) ->
            val selected = activityLevel == value
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onActivityChange(value) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected)
                        GreenAccent.copy(alpha = 0.1f)
                    else
                        CardBg
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (selected)
                    BorderStroke(1.dp, GreenAccent)
                else
                    BorderStroke(1.dp, DividerColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            label,
                            color = if (selected) GreenAccent else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(desc, color = TextMuted, fontSize = 12.sp)
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Step 4: Goal ─────────────────────────────────────────────────────────────

@Composable
fun StepGoal(goal: String, onGoalChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("🎯", fontSize = 40.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Text(
            "What's your goal?",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We'll calculate your perfect calorie target",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        val goals = listOf(
            Triple("lose", "🔥 Lose Weight", "500 kcal deficit per day"),
            Triple("maintain", "⚖️ Maintain Weight", "Eat at maintenance calories"),
            Triple("gain", "💪 Gain Muscle", "300 kcal surplus per day")
        )

        goals.forEach { (value, label, desc) ->
            val selected = goal == value
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onGoalChange(value) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected)
                        GreenAccent.copy(alpha = 0.1f)
                    else
                        CardBg
                ),
                shape = RoundedCornerShape(14.dp),
                border = if (selected)
                    BorderStroke(1.5.dp, GreenAccent)
                else
                    BorderStroke(1.dp, DividerColor)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            label,
                            color = if (selected) GreenAccent else TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(desc, color = TextMuted, fontSize = 12.sp)
                    }
                    if (selected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}