package com.example.caltracker.uii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

// ─── Glow Modifier ────────────────────────────────────────────────────────────

fun Modifier.glowEffect(
    color: Color,
    glowRadius: Dp = 12.dp,
    cornerRadius: Dp = 16.dp
): Modifier = this.drawBehind {
    val radiusPx = glowRadius.toPx()
    val paint = androidx.compose.ui.graphics.Paint().apply {
        asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(
                radiusPx,
                0f,
                0f,
                android.graphics.Color.argb(
                    180,
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
            )
        }
    }
    drawContext.canvas.drawRoundRect(
        left = -radiusPx / 2,
        top = -radiusPx / 2,
        right = size.width + radiusPx / 2,
        bottom = size.height + radiusPx / 2,
        radiusX = cornerRadius.toPx(),
        radiusY = cornerRadius.toPx(),
        paint = paint
    )
}

// ─── Glassmorphism Card ───────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = GreenAccent, // Make sure GreenAccent is imported!
    showGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val cornerRadius = 20.dp

    // We can simplify this down to a single Column by moving the shadow
    // and glow effects into the drawBehind modifier.
    Column(
        modifier = modifier
            .fillMaxWidth()
            // ── 1. Shadow & Glow Layer (Clipped to prevent bleed) ────────────
            .drawBehind {
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            cornerRadius = CornerRadius(cornerRadius.toPx())
                        )
                    )
                }

                clipPath(path = path, clipOp = ClipOp.Difference) {
                    // Draw Glow
                    if (showGlow) {
                        val glowPaint = Paint().apply {
                            color = glowColor
                            asFrameworkPaint().apply {
                                // Replicates your 20.dp glow spread
                                maskFilter = android.graphics.BlurMaskFilter(
                                    20.dp.toPx(),
                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }
                        }
                        drawIntoCanvas { canvas ->
                            canvas.drawPath(path, glowPaint)
                        }
                    }

                    // Draw Shadow (Replaces your separate Box shadow)
                    val shadowPaint = Paint().apply {
                        color = Color.Black.copy(alpha = 0.4f)
                        asFrameworkPaint().apply {
                            // Replicates your blur(8.dp)
                            maskFilter = android.graphics.BlurMaskFilter(
                                8.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    }

                    drawIntoCanvas { canvas ->
                        canvas.save()
                        // Replicates your offset(y = 4.dp)
                        canvas.translate(0f, 4.dp.toPx())
                        canvas.drawPath(path, shadowPaint)
                        canvas.restore()
                    }
                }
            }
            // ── 2. Glass Layer ───────────────────────────────────────────────
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF252535).copy(alpha = 0.95f),
                        Color(0xFF151520).copy(alpha = 0.98f)
                    )
                )
            )
            .drawBehind {
                // Top highlight line (glass effect)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(20.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 20.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                // Border
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    style = Stroke(1.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
            .padding(18.dp),
        content = content
    )
}

// ─── Section Card (updated) ───────────────────────────────────────────────────

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = modifier, content = content)
}

// ─── Animated Macro Ring with Glow ───────────────────────────────────────────

@Composable
fun MacroRing(
    label: String,
    current: Float,
    goal: Float,
    color: Color,
    size: Dp = 86.dp,
    strokeWidth: Float = 11f
) {
    val progress = if (goal > 0) (current / goal).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "ring"
    )

    // Pulse animation when full
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            // Glow layer behind ring
            if (progress > 0f) {
                Canvas(modifier = Modifier.size(size)) {
                    val glowStroke = Stroke(strokeWidth * 3f, cap = StrokeCap.Round)
                    val inset = strokeWidth * 1.5f
                    drawArc(
                        color = color.copy(
                            alpha = if (progress >= 1f) glowAlpha * 0.3f else 0.08f
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(
                            this.size.width - inset * 2,
                            this.size.height - inset * 2
                        ),
                        style = glowStroke
                    )
                }
            }

            Canvas(modifier = Modifier.size(size)) {
                val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
                val inset = strokeWidth / 2
                val arcSize = Size(
                    this.size.width - inset * 2,
                    this.size.height - inset * 2
                )
                val topLeft = Offset(inset, inset)

                // Track ring
                drawArc(
                    color = color.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )

                // Progress ring
                if (animatedProgress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f),
                                color,
                                color
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                }
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${current.toInt()}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "g",
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "/ ${goal.toInt()}g",
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

// ─── Gradient Calorie Bar ─────────────────────────────────────────────────────

@Composable
fun CalorieProgressBar(current: Int, goal: Int) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "calBar"
    )

    val gradientColors = when {
        progress >= 1f -> listOf(FatColor, FatGradientEnd)
        progress >= 0.85f -> listOf(CarbsColor, CarbsGradientEnd)
        else -> listOf(GreenGradientStart, GreenGradientEnd)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    "$current",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Text(
                    "kcal consumed",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(goal - current).coerceAtLeast(0)}",
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    "kcal left",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Progress bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(SurfaceBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", color = TextMuted, fontSize = 10.sp)
            Text("Goal: $goal kcal", color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ─── Gradient Macro Chip ──────────────────────────────────────────────────────

@Composable
fun MacroStatChip(label: String, value: String, color: Color) {
    val gradientColors = when (color) {
        ProteinColor -> listOf(ProteinColor.copy(0.2f), ProteinColor.copy(0.05f))
        CarbsColor -> listOf(CarbsColor.copy(0.2f), CarbsColor.copy(0.05f))
        FatColor -> listOf(FatColor.copy(0.2f), FatColor.copy(0.05f))
        else -> listOf(color.copy(0.2f), color.copy(0.05f))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(gradientColors))
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = 0.3f),
                    style = Stroke(1.dp.toPx()),
                    cornerRadius =
                        androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                )
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ─── Shimmer Effect ───────────────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF2A2A2A),
                        Color(0xFF1A1A1A)
                    ),
                    startX = shimmerX,
                    endX = shimmerX + 600f
                )
            )
    )
}

// ─── Shimmer Loading Card ─────────────────────────────────────────────────────

@Composable
fun ShimmerLoadingCard() {
    GlassCard {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            cornerRadius = 8.dp
        )
        Spacer(Modifier.height(16.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp),
            cornerRadius = 8.dp
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(
                modifier = Modifier.weight(1f).height(60.dp),
                cornerRadius = 12.dp
            )
            ShimmerBox(
                modifier = Modifier.weight(1f).height(60.dp),
                cornerRadius = 12.dp
            )
            ShimmerBox(
                modifier = Modifier.weight(1f).height(60.dp),
                cornerRadius = 12.dp
            )
        }
    }
}