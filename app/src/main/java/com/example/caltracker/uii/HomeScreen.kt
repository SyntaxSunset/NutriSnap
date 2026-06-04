package com.example.caltracker.uii

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.data.FoodEntry
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: MainViewModel, onLogFood: () -> Unit) {
    val state by viewModel.homeState.collectAsState()
    val profile by viewModel.profileState.collectAsState()
    val today = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

    // Generate AI insights
    val insights = remember(
        state.todayEntries,
        state.totalCalories,
        state.totalProtein,
        state.totalCarbs,
        state.totalFat,
        state.streak.currentStreak
    ) {
        InsightsEngine.generateInsights(
            entries = state.todayEntries,
            goal = state.goal,
            profile = profile,
            streak = state.streak.currentStreak,
            totalCalories = state.totalCalories,
            totalProtein = state.totalProtein,
            totalCarbs = state.totalCarbs,
            totalFat = state.totalFat
        )
    }

    // Group today's entries by meal
    val groupedEntries = remember(state.todayEntries) {
        val map = mutableMapOf<MealType, MutableList<FoodEntry>>()
        MealType.values().forEach { map[it] = mutableListOf() }
        state.todayEntries.forEach { entry ->
            map[getMealType(entry.timestamp)]?.add(entry)
        }
        map
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A12),
                        Color(0xFF080808),
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

            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Good ${greeting()},",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${profile?.name?.ifBlank { "there" } ?: "there"} 👋",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(today, color = TextMuted, fontSize = 12.sp)
                }
                AnimatedStreakBadge(streak = state.streak.currentStreak)
            }

            Spacer(Modifier.height(20.dp))

            // ── Calorie Card ──────────────────────────────────────────────────
            GlassCard(
                showGlow = state.totalCalories > 0,
                glowColor = GreenAccent.copy(0.3f)
            ) {
                CalorieProgressBar(
                    current = state.totalCalories,
                    goal = state.goal.calorieGoal
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Macro Rings ───────────────────────────────────────────────────
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Macros",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${state.todayEntries.size} items logged",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroRing(
                        label = "Protein",
                        current = state.totalProtein,
                        goal = state.goal.proteinGoal,
                        color = ProteinColor,
                        size = 90.dp
                    )
                    MacroRing(
                        label = "Carbs",
                        current = state.totalCarbs,
                        goal = state.goal.carbsGoal,
                        color = CarbsColor,
                        size = 90.dp
                    )
                    MacroRing(
                        label = "Fat",
                        current = state.totalFat,
                        goal = state.goal.fatGoal,
                        color = FatColor,
                        size = 90.dp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── AI Insight Cards ──────────────────────────────────────────────
            if (insights.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🤖 AI Insights",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Personalized",
                        color = GreenAccent,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                insights.forEachIndexed { index, insight ->
                    AIInsightCard(insight = insight, index = index)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Meal Sections ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today's Meals",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.totalCalories} kcal total",
                    color = GreenAccent,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            MealType.values().forEach { mealType ->
                HomeMealSection(
                    mealType = mealType,
                    entries = groupedEntries[mealType] ?: emptyList(),
                    onLogFood = onLogFood
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

// ─── AI Insight Card ──────────────────────────────────────────────────────────

@Composable
fun AIInsightCard(insight: AIInsight, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 150L)
        visible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "insightOffset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "insightAlpha"
    )

    val (bgColor, borderColor, textColor) = when (insight.type) {
        InsightType.SUCCESS -> Triple(
            GreenAccent.copy(0.08f),
            GreenAccent.copy(0.3f),
            GreenAccent
        )
        InsightType.WARNING -> Triple(
            CarbsColor.copy(0.08f),
            CarbsColor.copy(0.3f),
            CarbsColor
        )
        InsightType.ALERT -> Triple(
            FatColor.copy(0.08f),
            FatColor.copy(0.3f),
            FatColor
        )
        InsightType.MOTIVATION -> Triple(
            ProteinColor.copy(0.08f),
            ProteinColor.copy(0.3f),
            ProteinColor
        )
        InsightType.INFO -> Triple(
            Color(0xFF9B59B6).copy(0.08f),
            Color(0xFF9B59B6).copy(0.3f),
            Color(0xFF9B59B6)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = offsetY.dp)
            .alpha(alpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(bgColor, bgColor.copy(0.3f))
                    )
                )
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        style = Stroke(1.dp.toPx()),
                        cornerRadius =
                            androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                    )
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(insight.icon, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    insight.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    insight.message,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Type indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
        }
    }
}

// ─── Home Meal Section ────────────────────────────────────────────────────────

@Composable
fun HomeMealSection(
    mealType: MealType,
    entries: List<FoodEntry>,
    onLogFood: () -> Unit
) {
    val totalCals = entries.sumOf { it.calories }
    var isExpanded by remember { mutableStateOf(true) }

    GlassCard {
        // ── Meal Header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (mealType) {
                                MealType.BREAKFAST -> CarbsColor.copy(0.15f)
                                MealType.LUNCH -> GreenAccent.copy(0.15f)
                                MealType.DINNER -> ProteinColor.copy(0.15f)
                                MealType.SNACKS -> FatColor.copy(0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mealType.emoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        mealType.label,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (entries.isEmpty()) "Not logged yet"
                        else "${entries.size} item${if (entries.size > 1) "s" else ""}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (totalCals > 0) {
                    Text(
                        "$totalCals kcal",
                        color = GreenAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Quick add button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GreenAccent.copy(0.15f))
                        .clickable { onLogFood() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = GreenAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess
                    else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Meal Entries ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                if (entries.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GreenAccent.copy(0.08f))
                                .clickable { onLogFood() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Log ${mealType.label}",
                                color = GreenAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍴", fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "P:${entry.protein.toInt()}g",
                                        color = ProteinColor,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        "C:${entry.carbs.toInt()}g",
                                        color = CarbsColor,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        "F:${entry.fat.toInt()}g",
                                        color = FatColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                "${entry.calories} kcal",
                                color = GreenAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (entries.last() != entry) {
                            Divider(
                                color = DividerColor,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Quick Stat Card ──────────────────────────────────────────────────────────

@Composable
fun QuickStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(0.15f), color.copy(0.03f))
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.25f),
                    style = Stroke(1.dp.toPx()),
                    cornerRadius =
                        androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Text(
            value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ─── Animated Streak Badge ────────────────────────────────────────────────────

@Composable
fun AnimatedStreakBadge(streak: Int) {
    val flamePulse = rememberInfiniteTransition(label = "flame")
    val flameScale by flamePulse.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    val flameAlpha by flamePulse.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameAlpha"
    )
    val glowAlpha by flamePulse.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val animatedStreak by animateIntAsState(
        targetValue = streak,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "streakCounter"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CarbsColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
        // Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CarbsColor.copy(0.25f),
                            CarbsColor.copy(0.08f)
                        )
                    )
                )
                .drawBehind {
                    drawRoundRect(
                        color = CarbsColor.copy(alpha = 0.4f),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius =
                            androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                    )
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "🔥",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .scale(flameScale)
                        .alpha(flameAlpha)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$animatedStreak",
                        color = CarbsColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        if (animatedStreak == 1) "day" else "days",
                        color = CarbsColor.copy(0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun greeting(): String {
    return when (java.time.LocalTime.now().hour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }
}