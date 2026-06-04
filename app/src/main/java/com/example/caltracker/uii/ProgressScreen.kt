package com.example.caltracker.uii

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.caltracker.data.DailySummaryRaw
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale

@Composable
fun ProgressScreen(viewModel: MainViewModel) {
    val state by viewModel.progressState.collectAsState()
    val homeState by viewModel.homeState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Text(
            "Progress",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Your weekly overview", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(20.dp))

        // ── Streak Cards ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StreakCard(
                emoji = "🔥",
                label = "Current Streak",
                value = "${state.streak.currentStreak}",
                unit = "days",
                color = CarbsColor,
                modifier = Modifier.weight(1f)
            )
            StreakCard(
                emoji = "🏆",
                label = "Best Streak",
                value = "${state.streak.longestStreak}",
                unit = "days",
                color = GreenAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Macro Pie Chart ──────────────────────────────────────────────────
        SectionCard {
            Text("Today's Macro Split", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            val protein = homeState.totalProtein
            val carbs = homeState.totalCarbs
            val fat = homeState.totalFat
            val total = protein + carbs + fat

            if (total == 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No data yet today", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Pie chart
                    MacroPieChart(
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        modifier = Modifier.size(160.dp)
                    )

                    // Legend
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        MacroLegendItem(
                            color = ProteinColor,
                            label = "Protein",
                            value = "${protein.toInt()}g",
                            percentage = if (total > 0) (protein / total * 100).toInt() else 0
                        )
                        MacroLegendItem(
                            color = CarbsColor,
                            label = "Carbs",
                            value = "${carbs.toInt()}g",
                            percentage = if (total > 0) (carbs / total * 100).toInt() else 0
                        )
                        MacroLegendItem(
                            color = FatColor,
                            label = "Fat",
                            value = "${fat.toInt()}g",
                            percentage = if (total > 0) (fat / total * 100).toInt() else 0
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Weekly Calories Chart ────────────────────────────────────────────
        SectionCard {
            Text("Weekly Calories", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            if (state.weeklySummary.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data yet — start logging!",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                WeeklyBarChart(
                    data = state.weeklySummary,
                    goal = homeState.goal.calorieGoal
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Weekly Macro Averages ────────────────────────────────────────────
        SectionCard {
            Text("Weekly Macro Averages", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            if (state.weeklySummary.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data yet", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                WeeklyMacroSummary(data = state.weeklySummary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Daily Goal Progress ──────────────────────────────────────────────
        SectionCard {
            Text("Today's Goal Progress", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            GoalRow(
                "Calories",
                homeState.totalCalories.toFloat(),
                homeState.goal.calorieGoal.toFloat(),
                "kcal",
                CalorieColor
            )
            Spacer(Modifier.height(8.dp))
            GoalRow(
                "Protein",
                homeState.totalProtein,
                homeState.goal.proteinGoal,
                "g",
                ProteinColor
            )
            Spacer(Modifier.height(8.dp))
            GoalRow(
                "Carbs",
                homeState.totalCarbs,
                homeState.goal.carbsGoal,
                "g",
                CarbsColor
            )
            Spacer(Modifier.height(8.dp))
            GoalRow(
                "Fat",
                homeState.totalFat,
                homeState.goal.fatGoal,
                "g",
                FatColor
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ─── Macro Pie Chart ──────────────────────────────────────────────────────────

@Composable
fun MacroPieChart(
    protein: Float,
    carbs: Float,
    fat: Float,
    modifier: Modifier = Modifier
) {
    val total = protein + carbs + fat
    val proteinAngle = if (total > 0) (protein / total) * 360f else 0f
    val carbsAngle = if (total > 0) (carbs / total) * 360f else 0f
    val fatAngle = if (total > 0) (fat / total) * 360f else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "pie"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 36f
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)
            val style = Stroke(strokeWidth, cap = StrokeCap.Butt)

            var startAngle = -90f

            // Background ring
            drawArc(
                color = SurfaceBg,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style
            )

            // Protein arc
            val pAngle = proteinAngle * animatedProgress
            if (pAngle > 0f) {
                drawArc(
                    color = ProteinColor,
                    startAngle = startAngle,
                    sweepAngle = pAngle - 2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
                startAngle += proteinAngle
            }

            // Carbs arc
            val cAngle = carbsAngle * animatedProgress
            if (cAngle > 0f) {
                drawArc(
                    color = CarbsColor,
                    startAngle = startAngle,
                    sweepAngle = cAngle - 2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
                startAngle += carbsAngle
            }

            // Fat arc
            val fAngle = fatAngle * animatedProgress
            if (fAngle > 0f) {
                drawArc(
                    color = FatColor,
                    startAngle = startAngle,
                    sweepAngle = fAngle - 2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${total.toInt()}g",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "total",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

// ─── Macro Legend Item ────────────────────────────────────────────────────────

@Composable
fun MacroLegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 13.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "$percentage%",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Streak Card ──────────────────────────────────────────────────────────────

@Composable
fun StreakCard(
    emoji: String,
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = color, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(unit, color = color.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ─── Weekly Bar Chart ─────────────────────────────────────────────────────────

@Composable
fun WeeklyBarChart(data: List<DailySummaryRaw>, goal: Int) {
    var selectedDay by remember { mutableStateOf<DailySummaryRaw?>(null) }
    val maxVal = maxOf(data.maxOfOrNull { it.totalCalories } ?: 0, goal)

    Column {
        // ── Bar Chart ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                val fraction = if (maxVal > 0)
                    day.totalCalories.toFloat() / maxVal
                else 0f

                val isSelected = selectedDay?.dateKey == day.dateKey
                val isGoalHit = day.totalCalories >= goal
                val isNearGoal = day.totalCalories in
                        (goal * 0.85f).toInt() until goal

                val barColor = when {
                    day.totalCalories > goal -> FatColor
                    isGoalHit -> GreenAccent
                    isNearGoal -> CarbsColor
                    else -> ProteinColor
                }

                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(800, easing = EaseOutCubic),
                    label = "bar"
                )

                val barScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    ),
                    label = "barScale"
                )

                val dayLabel = day.dateKey.takeLast(5).replace("-", "/")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            selectedDay = if (isSelected) null else day
                        }
                ) {
                    // Calorie label
                    AnimatedVisibility(
                        visible = isSelected || day.totalCalories > 0,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Text(
                            "${day.totalCalories}",
                            color = if (isSelected) barColor else TextMuted,
                            fontSize = if (isSelected) 11.sp else 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold
                            else FontWeight.Normal
                        )
                    }

                    // Goal badge
                    if (isGoalHit) Text("🎯", fontSize = 10.sp)
                    else if (isNearGoal) Text("🔶", fontSize = 10.sp)

                    Spacer(Modifier.height(2.dp))

                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight(
                                animatedFraction.coerceAtLeast(0.02f)
                            )
                            .scale(barScale)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                if (isSelected)
                                    Brush.verticalGradient(
                                        listOf(barColor, barColor.copy(0.6f))
                                    )
                                else
                                    Brush.verticalGradient(
                                        listOf(
                                            barColor.copy(0.9f),
                                            barColor.copy(0.4f)
                                        )
                                    )
                            )
                            .let { if (isSelected) it.glowEffect(barColor, 8.dp) else it }
                    )

                    Spacer(Modifier.height(6.dp))

                    // Day label
                    Text(
                        dayLabel,
                        color = if (isSelected) barColor else TextMuted,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold
                        else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Selected Day Detail Card ───────────────────────────────────────
        AnimatedVisibility(
            visible = selectedDay != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            selectedDay?.let { day ->
                val barColor = when {
                    day.totalCalories > goal -> FatColor
                    day.totalCalories >= goal -> GreenAccent
                    day.totalCalories >= (goal * 0.85f).toInt() -> CarbsColor
                    else -> ProteinColor
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    barColor.copy(0.15f),
                                    barColor.copy(0.05f)
                                )
                            )
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = barColor.copy(alpha = 0.3f),
                                style = Stroke(1.dp.toPx()),
                                cornerRadius =
                                    androidx.compose.ui.geometry.CornerRadius(
                                        14.dp.toPx()
                                    )
                            )
                        }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                day.dateKey,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                "${day.totalCalories} kcal",
                                color = barColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DayDetailMacro(
                                "Protein",
                                "${day.totalProtein.toInt()}g",
                                ProteinColor
                            )
                            DayDetailMacro(
                                "Carbs",
                                "${day.totalCarbs.toInt()}g",
                                CarbsColor
                            )
                            DayDetailMacro(
                                "Fat",
                                "${day.totalFat.toInt()}g",
                                FatColor
                            )
                            DayDetailMacro(
                                "vs Goal",
                                "${day.totalCalories - goal} kcal",
                                if (day.totalCalories > goal) FatColor
                                else GreenAccent
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Legend ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GreenAccent, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(4.dp))
                Text("Goal hit", color = TextMuted, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CarbsColor, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(4.dp))
                Text("Near goal", color = TextMuted, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(ProteinColor, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(4.dp))
                Text("Under", color = TextMuted, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(FatColor, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(4.dp))
                Text("Over", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

// ─── Day Detail Macro ─────────────────────────────────────────────────────────

@Composable
fun DayDetailMacro(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

// ─── Weekly Macro Summary ─────────────────────────────────────────────────────

@Composable
fun WeeklyMacroSummary(data: List<DailySummaryRaw>) {
    val avgProtein = data.map { it.totalProtein }.average().toFloat()
    val avgCarbs = data.map { it.totalCarbs }.average().toFloat()
    val avgFat = data.map { it.totalFat }.average().toFloat()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AvgMacroCard("Avg Protein", avgProtein, ProteinColor)
        AvgMacroCard("Avg Carbs", avgCarbs, CarbsColor)
        AvgMacroCard("Avg Fat", avgFat, FatColor)
    }
}

@Composable
fun AvgMacroCard(label: String, value: Float, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            "${value.toInt()}g",
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ─── Goal Row ─────────────────────────────────────────────────────────────────

@Composable
fun GoalRow(
    label: String,
    current: Float,
    goal: Float,
    unit: String,
    color: Color
) {
    val progress = if (goal > 0) (current / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "goal"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 13.sp)
            Row {
                Text(
                    "${current.toInt()}",
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    " / ${goal.toInt()} $unit",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(SurfaceBg, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}