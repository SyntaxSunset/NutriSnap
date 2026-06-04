package com.example.caltracker.uii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.data.UserProfile
import com.example.caltracker.ui.theme.*
import kotlin.math.pow

// ─── BMI Data ─────────────────────────────────────────────────────────────────

data class BMIResult(
    val bmi: Float,
    val category: String,
    val color: Color,
    val emoji: String,
    val recommendation: String,
    val idealWeightMin: Float,
    val idealWeightMax: Float,
    val weightDiff: Float
)

fun calculateBMI(profile: UserProfile): BMIResult {
    val heightM = profile.heightCm / 100f
    val bmi = profile.weightKg / (heightM * heightM)

    val idealMin = 18.5f * heightM.pow(2)
    val idealMax = 24.9f * heightM.pow(2)
    val weightDiff = when {
        bmi < 18.5f -> idealMin - profile.weightKg
        bmi > 24.9f -> profile.weightKg - idealMax
        else -> 0f
    }

    return when {
        bmi < 16f -> BMIResult(
            bmi = bmi,
            category = "Severely Underweight",
            color = Color(0xFF42A5F5),
            emoji = "⚠️",
            recommendation = "Your BMI indicates severe underweight. Please consult a healthcare professional immediately and focus on increasing caloric intake.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = weightDiff
        )
        bmi < 18.5f -> BMIResult(
            bmi = bmi,
            category = "Underweight",
            color = ProteinColor,
            emoji = "📈",
            recommendation = "You're slightly underweight. Focus on nutrient-dense foods and increase your daily calorie intake by 300-500 kcal.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = weightDiff
        )
        bmi < 25f -> BMIResult(
            bmi = bmi,
            category = "Normal Weight",
            color = GreenAccent,
            emoji = "✅",
            recommendation = "Excellent! Your BMI is in the healthy range. Maintain your current lifestyle with balanced nutrition and regular exercise.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = 0f
        )
        bmi < 30f -> BMIResult(
            bmi = bmi,
            category = "Overweight",
            color = CarbsColor,
            emoji = "⚡",
            recommendation = "You're slightly overweight. A 500 kcal daily deficit with regular exercise can help you reach a healthy weight.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = weightDiff
        )
        bmi < 35f -> BMIResult(
            bmi = bmi,
            category = "Obese Class I",
            color = Color(0xFFFF7043),
            emoji = "🎯",
            recommendation = "Focus on gradual weight loss through diet and exercise. Aim for 0.5-1kg per week for sustainable results.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = weightDiff
        )
        else -> BMIResult(
            bmi = bmi,
            category = "Obese Class II+",
            color = FatColor,
            emoji = "🏥",
            recommendation = "Please consult a healthcare professional for a personalized weight management plan.",
            idealWeightMin = idealMin,
            idealWeightMax = idealMax,
            weightDiff = weightDiff
        )
    }
}

// ─── BMI Screen ───────────────────────────────────────────────────────────────

@Composable
fun BMIScreen(profile: UserProfile) {
    val bmiResult = remember(profile) { calculateBMI(profile) }

    // Animate BMI gauge
    val animatedBMI by animateFloatAsState(
        targetValue = bmiResult.bmi,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "bmi"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── BMI Gauge ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24f
                val startAngle = 150f
                val sweepAngle = 240f
                val inset = strokeWidth / 2
                val arcSize = Size(
                    size.width - inset * 2,
                    size.height - inset * 2
                )
                val topLeft = Offset(inset, inset)

                // Background track
                drawArc(
                    color = SurfaceBg,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Gradient segments
                val segments = listOf(
                    Triple(ProteinColor, 0f, 0.15f),   // Underweight
                    Triple(GreenAccent, 0.15f, 0.45f),  // Normal
                    Triple(CarbsColor, 0.45f, 0.65f),   // Overweight
                    Triple(Color(0xFFFF7043), 0.65f, 0.80f), // Obese I
                    Triple(FatColor, 0.80f, 1f)          // Obese II+
                )

                segments.forEach { (color, start, end) ->
                    drawArc(
                        color = color.copy(alpha = 0.3f),
                        startAngle = startAngle + sweepAngle * start,
                        sweepAngle = sweepAngle * (end - start),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                    )
                }

                // Progress indicator
                val bmiProgress = ((animatedBMI - 10f) / 30f).coerceIn(0f, 1f)
                drawArc(
                    color = bmiResult.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * bmiProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    bmiResult.emoji,
                    fontSize = 28.sp
                )
                Text(
                    String.format("%.1f", animatedBMI),
                    color = bmiResult.color,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "BMI",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // ── Category Badge ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bmiResult.color.copy(0.15f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                bmiResult.category,
                color = bmiResult.color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Stats Row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BMIStatCard(
                label = "Your Weight",
                value = "${profile.weightKg.toInt()} kg",
                color = bmiResult.color,
                modifier = Modifier.weight(1f)
            )
            BMIStatCard(
                label = "Your Height",
                value = "${profile.heightCm.toInt()} cm",
                color = ProteinColor,
                modifier = Modifier.weight(1f)
            )
            BMIStatCard(
                label = "Ideal Range",
                value = "${bmiResult.idealWeightMin.toInt()}-${bmiResult.idealWeightMax.toInt()} kg",
                color = GreenAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Weight Difference ─────────────────────────────────────────────
        if (bmiResult.weightDiff > 0.5f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bmiResult.color.copy(0.08f))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (bmiResult.bmi < 18.5f) "📈" else "📉",
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            if (bmiResult.bmi < 18.5f)
                                "Gain ${String.format("%.1f", bmiResult.weightDiff)} kg"
                            else
                                "Lose ${String.format("%.1f", bmiResult.weightDiff)} kg",
                            color = bmiResult.color,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "to reach healthy BMI range",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }

        // ── BMI Scale ─────────────────────────────────────────────────────
        BMIScale(currentBMI = bmiResult.bmi)

        Spacer(Modifier.height(14.dp))

        // ── AI Recommendation ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GreenAccent.copy(0.08f),
                            GreenAccent.copy(0.03f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🤖", fontSize = 24.sp)
                Column {
                    Text(
                        "AI Recommendation",
                        color = GreenAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        bmiResult.recommendation,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ─── BMI Scale ────────────────────────────────────────────────────────────────

@Composable
fun BMIScale(currentBMI: Float) {
    val categories = listOf(
        Triple("< 18.5", "Under", ProteinColor),
        Triple("18.5-24.9", "Normal", GreenAccent),
        Triple("25-29.9", "Over", CarbsColor),
        Triple("30-34.9", "Obese I", Color(0xFFFF7043)),
        Triple("> 35", "Obese II", FatColor)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "BMI Scale",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { (range, label, color) ->
                val isCurrent = when (label) {
                    "Under" -> currentBMI < 18.5f
                    "Normal" -> currentBMI in 18.5f..24.9f
                    "Over" -> currentBMI in 25f..29.9f
                    "Obese I" -> currentBMI in 30f..34.9f
                    else -> currentBMI >= 35f
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isCurrent) 10.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isCurrent) color
                                else color.copy(0.3f)
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
                        color = if (isCurrent) color else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold
                        else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    if (isCurrent) {
                        Text(
                            "▲",
                            color = color,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── BMI Stat Card ────────────────────────────────────────────────────────────

@Composable
fun BMIStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.08f))
            .padding(10.dp)
    ) {
        Text(
            value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = TextMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}