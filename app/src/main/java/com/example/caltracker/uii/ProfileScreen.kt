package com.example.caltracker.uii

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.data.DailyGoal
import com.example.caltracker.data.UserProfile
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.sin

// ─── Profile Screen ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onLogOut: () -> Unit
) {
    val profile by viewModel.profileState.collectAsState()
    val homeState by viewModel.homeState.collectAsState()

    ProfileScreenContent(
        profile = profile ?: UserProfile(),
        goal = homeState.goal,
        streak = homeState.streak.currentStreak,
        onNavigateToSettings = onNavigateToSettings,
        onSaveGoal = { cal, pro, carb, fat ->
            viewModel.saveGoal(cal, pro, carb, fat)
        },
        onLogOut = onLogOut
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    profile: UserProfile,
    goal: DailyGoal,
    streak: Int,
    onNavigateToSettings: () -> Unit,
    onSaveGoal: (Int, Float, Float, Float) -> Unit,
    onLogOut: () -> Unit
) {
    val bmiResult = remember(profile) {
        if (profile.heightCm > 0 && profile.weightKg > 0)
            calculateBMI(profile)
        else null
    }

    // Bottom sheet states
    var showCalorieSheet by remember { mutableStateOf(false) }
    var showProteinSheet by remember { mutableStateOf(false) }
    var showCarbsSheet by remember { mutableStateOf(false) }
    var showFatSheet by remember { mutableStateOf(false) }

    // Temp goal values for editing
    var tempCalories by remember { mutableStateOf(goal.calorieGoal.toString()) }
    var tempProtein by remember { mutableStateOf(goal.proteinGoal.toInt().toString()) }
    var tempCarbs by remember { mutableStateOf(goal.carbsGoal.toInt().toString()) }
    var tempFat by remember { mutableStateOf(goal.fatGoal.toInt().toString()) }

    LaunchedEffect(goal) {
        tempCalories = goal.calorieGoal.toString()
        tempProtein = goal.proteinGoal.toInt().toString()
        tempCarbs = goal.carbsGoal.toInt().toString()
        tempFat = goal.fatGoal.toInt().toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A12),
                        Color(0xFF080808)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 1. Identity Header
            ProfileHeader(
                profile = profile,
                streak = streak,
                onSettingsClick = onNavigateToSettings
            )

            Spacer(Modifier.height(20.dp))

            // 2. Quick Stats Row
            QuickStatsRow(profile = profile, bmiResult = bmiResult)

            Spacer(Modifier.height(20.dp))

            // 3. AI BMI Analysis
            bmiResult?.let {
                BMIAnalysisCard(bmiResult = it)
                Spacer(Modifier.height(20.dp))
            }

            // 4. Daily Macro Goals
            MacroGoalsSection(
                goal = goal,
                onCaloriesClick = { showCalorieSheet = true },
                onProteinClick = { showProteinSheet = true },
                onCarbsClick = { showCarbsSheet = true },
                onFatClick = { showFatSheet = true }
            )

            Spacer(Modifier.height(20.dp))

            // 5. Account Actions
            AccountActionsSection(onLogOut = onLogOut)

            Spacer(Modifier.height(100.dp))
        }

        // ── Goal Edit Bottom Sheets ───────────────────────────────────────────
        if (showCalorieSheet) {
            GoalEditSheet(
                title = "Daily Calorie Goal",
                value = tempCalories,
                unit = "kcal",
                color = CalorieColor,
                onValueChange = { tempCalories = it },
                onSave = {
                    onSaveGoal(
                        tempCalories.toIntOrNull() ?: goal.calorieGoal,
                        goal.proteinGoal,
                        goal.carbsGoal,
                        goal.fatGoal
                    )
                    showCalorieSheet = false
                },
                onDismiss = { showCalorieSheet = false }
            )
        }

        if (showProteinSheet) {
            GoalEditSheet(
                title = "Daily Protein Goal",
                value = tempProtein,
                unit = "g",
                color = ProteinColor,
                onValueChange = { tempProtein = it },
                onSave = {
                    onSaveGoal(
                        goal.calorieGoal,
                        tempProtein.toFloatOrNull() ?: goal.proteinGoal,
                        goal.carbsGoal,
                        goal.fatGoal
                    )
                    showProteinSheet = false
                },
                onDismiss = { showProteinSheet = false }
            )
        }

        if (showCarbsSheet) {
            GoalEditSheet(
                title = "Daily Carbs Goal",
                value = tempCarbs,
                unit = "g",
                color = CarbsColor,
                onValueChange = { tempCarbs = it },
                onSave = {
                    onSaveGoal(
                        goal.calorieGoal,
                        goal.proteinGoal,
                        tempCarbs.toFloatOrNull() ?: goal.carbsGoal,
                        goal.fatGoal
                    )
                    showCarbsSheet = false
                },
                onDismiss = { showCarbsSheet = false }
            )
        }

        if (showFatSheet) {
            GoalEditSheet(
                title = "Daily Fat Goal",
                value = tempFat,
                unit = "g",
                color = FatColor,
                onValueChange = { tempFat = it },
                onSave = {
                    onSaveGoal(
                        goal.calorieGoal,
                        goal.proteinGoal,
                        goal.carbsGoal,
                        tempFat.toFloatOrNull() ?: goal.fatGoal
                    )
                    showFatSheet = false
                },
                onDismiss = { showFatSheet = false }
            )
        }
    }
}

// ─── 1. Profile Header ────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    streak: Int,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                GreenAccent.copy(0.3f),
                                GreenAccent.copy(0.05f)
                            )
                        )
                    )
                    .drawBehind {
                        drawCircle(
                            color = GreenAccent.copy(alpha = 0.4f),
                            style = Stroke(1.5.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profile.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = GreenAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    "Hello, ${profile.name.ifBlank { "there" }} 👋",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Goal badge
                    ProfileBadge(
                        text = when (profile.goal) {
                            "lose" -> "Weight Loss"
                            "gain" -> "Muscle Gain"
                            else -> "Maintenance"
                        },
                        color = when (profile.goal) {
                            "lose" -> FatColor
                            "gain" -> ProteinColor
                            else -> GreenAccent
                        }
                    )
                    // Streak badge
                    if (streak > 0) {
                        ProfileBadge(
                            text = "🔥 $streak days",
                            color = CarbsColor
                        )
                    }
                }
            }
        }

        // Settings icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceBg)
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── 2. Quick Stats Row ───────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(
    profile: UserProfile,
    bmiResult: BMIResult?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatGlassCard(
            label = "Weight",
            value = "${profile.weightKg.toInt()}",
            unit = "kg",
            icon = "⚖️",
            color = ProteinColor,
            modifier = Modifier.weight(1f)
        )
        QuickStatGlassCard(
            label = "Height",
            value = "${profile.heightCm.toInt()}",
            unit = "cm",
            icon = "📏",
            color = CarbsColor,
            modifier = Modifier.weight(1f)
        )
        QuickStatGlassCard(
            label = "BMI",
            value = bmiResult?.let { String.format("%.1f", it.bmi) } ?: "--",
            unit = bmiResult?.category?.take(6) ?: "",
            icon = bmiResult?.emoji ?: "📊",
            color = bmiResult?.color ?: GreenAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatGlassCard(
    label: String,
    value: String,
    unit: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E1E2E).copy(0.95f),
                        Color(0xFF111118).copy(0.98f)
                    )
                )
            )
            .drawBehind {
                // Top highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(0.12f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(10.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 10.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                // Border
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            color.copy(0.4f),
                            color.copy(0.1f)
                        )
                    ),
                    style = Stroke(1.dp.toPx()),
                    cornerRadius =
                        androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .padding(12.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            unit,
            color = TextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

// ─── 3. BMI Analysis Card ─────────────────────────────────────────────────────

@Composable
private fun BMIAnalysisCard(bmiResult: BMIResult) {
    var showRecommendation by remember { mutableStateOf(false) }

    // Animate BMI gauge
    val animatedBMI by animateFloatAsState(
        targetValue = bmiResult.bmi,
        animationSpec = tween(1800, easing = EaseOutCubic),
        label = "bmiGauge"
    )

    GlassCard(showGlow = true, glowColor = bmiResult.color.copy(0.2f)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🤖", fontSize = 18.sp)
                Text(
                    "AI BMI Analysis",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(GreenAccent.copy(0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "AI Powered",
                    color = GreenAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Half circle gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height
                val radius = size.width * 0.38f
                val strokeWidth = 28f
                val startAngle = 180f
                val sweepAngle = 180f

                // Background arc
                drawArc(
                    color = SurfaceBg,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        centerX - radius,
                        centerY - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Colored segments
                val segments = listOf(
                    Pair(ProteinColor, 0f to 0.15f),
                    Pair(GreenAccent, 0.15f to 0.45f),
                    Pair(CarbsColor, 0.45f to 0.65f),
                    Pair(Color(0xFFFF7043), 0.65f to 0.80f),
                    Pair(FatColor, 0.80f to 1f)
                )

                segments.forEach { (color, range) ->
                    drawArc(
                        color = color.copy(0.25f),
                        startAngle = startAngle + sweepAngle * range.first,
                        sweepAngle = sweepAngle * (range.second - range.first),
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                    )
                }

                // Progress arc
                val bmiProgress = ((animatedBMI - 10f) / 30f).coerceIn(0f, 1f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ProteinColor,
                            GreenAccent,
                            CarbsColor,
                            FatColor
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * bmiProgress,
                    useCenter = false,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Needle
                val needleAngle = Math.toRadians(
                    (startAngle + sweepAngle *
                            ((animatedBMI - 10f) / 30f).coerceIn(0f, 1f)).toDouble()
                )
                val needleLength = radius - strokeWidth
                val needleX = centerX + cos(needleAngle).toFloat() * needleLength
                val needleY = centerY + sin(needleAngle).toFloat() * needleLength

                // Needle line
                drawLine(
                    color = Color.White,
                    start = Offset(centerX, centerY),
                    end = Offset(needleX, needleY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Needle center dot
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = bmiResult.color,
                    radius = 5f,
                    center = Offset(centerX, centerY)
                )
            }

            // Center BMI value
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    String.format("%.1f", animatedBMI),
                    color = bmiResult.color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "BMI",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Category + scale labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Underweight", color = ProteinColor, fontSize = 9.sp)
            Text("Normal", color = GreenAccent, fontSize = 9.sp)
            Text("Overweight", color = CarbsColor, fontSize = 9.sp)
            Text("Obese", color = FatColor, fontSize = 9.sp)
        }

        Spacer(Modifier.height(12.dp))

        // Category badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bmiResult.color.copy(0.15f))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bmiResult.emoji, fontSize = 14.sp)
                    Text(
                        bmiResult.category,
                        color = bmiResult.color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Weight difference
        if (bmiResult.weightDiff > 0.5f) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bmiResult.color.copy(0.08f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (bmiResult.bmi < 18.5f) "📈" else "📉",
                    fontSize = 18.sp
                )
                Text(
                    if (bmiResult.bmi < 18.5f)
                        "Gain ${String.format("%.1f", bmiResult.weightDiff)} kg to reach healthy range"
                    else
                        "Lose ${String.format("%.1f", bmiResult.weightDiff)} kg to reach healthy range",
                    color = bmiResult.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Collapsible AI Recommendation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GreenAccent.copy(0.06f))
                .clickable { showRecommendation = !showRecommendation }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🤖", fontSize = 16.sp)
                Text(
                    "AI Recommendation",
                    color = GreenAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                if (showRecommendation) Icons.Filled.ExpandLess
                else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(
            visible = showRecommendation,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    bmiResult.recommendation,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceBg)
                        .padding(12.dp)
                )
            }
        }
    }
}

// ─── 4. Macro Goals Section ───────────────────────────────────────────────────

@Composable
private fun MacroGoalsSection(
    goal: DailyGoal,
    onCaloriesClick: () -> Unit,
    onProteinClick: () -> Unit,
    onCarbsClick: () -> Unit,
    onFatClick: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Daily Goals",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tap to edit",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroGoalCard(
                label = "Calories",
                value = goal.calorieGoal,
                unit = "kcal",
                color = CalorieColor,
                icon = "🔥",
                modifier = Modifier.weight(1f),
                onClick = onCaloriesClick
            )
            MacroGoalCard(
                label = "Protein",
                value = goal.proteinGoal.toInt(),
                unit = "g",
                color = ProteinColor,
                icon = "🥩",
                modifier = Modifier.weight(1f),
                onClick = onProteinClick
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MacroGoalCard(
                label = "Carbs",
                value = goal.carbsGoal.toInt(),
                unit = "g",
                color = CarbsColor,
                icon = "🌾",
                modifier = Modifier.weight(1f),
                onClick = onCarbsClick
            )
            MacroGoalCard(
                label = "Fat",
                value = goal.fatGoal.toInt(),
                unit = "g",
                color = FatColor,
                icon = "🥑",
                modifier = Modifier.weight(1f),
                onClick = onFatClick
            )
        }
    }
}

@Composable
private fun MacroGoalCard(
    label: String,
    value: Int,
    unit: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.08f))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.2f),
                    style = Stroke(1.dp.toPx()),
                    cornerRadius =
                        androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                )
            }
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Column {
            Text(
                "$value $unit",
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Filled.Edit,
            contentDescription = null,
            tint = color.copy(0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

// ─── 5. Account Actions ───────────────────────────────────────────────────────

@Composable
private fun AccountActionsSection(onLogOut: () -> Unit) {
    GlassCard {
        Text(
            "Account",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(8.dp))

        AccountActionItem(
            icon = Icons.Filled.FileDownload,
            label = "Export Data",
            color = ProteinColor,
            onClick = {}
        )

        Divider(color = DividerColor, thickness = 0.5.dp)

        AccountActionItem(
            icon = Icons.Filled.Help,
            label = "Help & Support",
            color = CarbsColor,
            onClick = {}
        )

        Divider(color = DividerColor, thickness = 0.5.dp)

        AccountActionItem(
            icon = Icons.Filled.Logout,
            label = "Log Out",
            color = FatColor,
            onClick = onLogOut
        )
    }
}

@Composable
private fun AccountActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Goal Edit Bottom Sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditSheet(
    title: String,
    value: String,
    unit: String,
    color: Color,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A2E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextMuted.copy(0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Set your daily target",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(unit, color = TextMuted) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = color,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = color
                ),
                shape = RoundedCornerShape(14.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Save",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF080808)
@Composable
fun ProfileScreenPreview() {
    ProfileScreenContent(
        profile = UserProfile(
            name = "Ishaan",
            age = 22,
            weightKg = 53f,
            heightCm = 167f,
            gender = "male",
            activityLevel = "moderate",
            goal = "maintain",
            isProfileComplete = true
        ),
        goal = DailyGoal(
            calorieGoal = 2500,
            proteinGoal = 150f,
            carbsGoal = 300f,
            fatGoal = 70f
        ),
        streak = 5,
        onNavigateToSettings = {},
        onSaveGoal = { _, _, _, _ -> },
        onLogOut = {}
    )
}