package com.example.caltracker.uii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.caltracker.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ─── Particle Data ────────────────────────────────────────────────────────────

data class Particle(
    val id: Int,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

// ─── Success Animation Overlay ────────────────────────────────────────────────

@Composable
fun SuccessAnimationOverlay(
    foodName: String,
    calories: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Haptic feedback
    LaunchedEffect(Unit) {
        val vibrator = context.getSystemService(
            android.content.Context.VIBRATOR_SERVICE
        ) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                android.os.VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 50),
                    intArrayOf(0, 200, 0, 150),
                    -1
                )
            )
        }
        delay(2200)
        onDismiss()
    }

    // ── Animation States ──────────────────────────────────────────────────────

    // Background fade
    var bgVisible by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (bgVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "bg"
    )

    // Circle scale
    var circleVisible by remember { mutableStateOf(false) }
    val circleScale by animateFloatAsState(
        targetValue = if (circleVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circle"
    )

    // Checkmark draw progress
    var checkVisible by remember { mutableStateOf(false) }
    val checkAlpha by animateFloatAsState(
        targetValue = if (checkVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "check"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (checkVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    // Text slide up
    var textVisible by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "textAlpha"
    )
    val textOffsetY by animateFloatAsState(
        targetValue = if (textVisible) 0f else 30f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "textOffset"
    )

    // Particles
    var particlesVisible by remember { mutableStateOf(false) }
    val particleProgress by animateFloatAsState(
        targetValue = if (particlesVisible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "particles"
    )

    // Outer ring pulse
    val ringPulse = rememberInfiniteTransition(label = "ring")
    val ringScale by ringPulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )
    val ringAlpha by ringPulse.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    // Generate particles once
    val particles = remember {
        List(20) { i ->
            Particle(
                id = i,
                angle = (i * 18f) + Random.nextFloat() * 15f,
                speed = 80f + Random.nextFloat() * 120f,
                size = 4f + Random.nextFloat() * 8f,
                color = listOf(
                    GreenAccent,
                    ProteinColor,
                    CarbsColor,
                    FatColor,
                    Color.White,
                    GreenAccent.copy(alpha = 0.7f)
                ).random()
            )
        }
    }

    // Sequence animations
    LaunchedEffect(Unit) {
        bgVisible = true
        delay(100)
        circleVisible = true
        delay(200)
        checkVisible = true
        particlesVisible = true
        delay(200)
        textVisible = true
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha * 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Particle Canvas ───────────────────────────────────────────
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Particles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (particlesVisible) {
                            drawParticles(particles, particleProgress, center)
                        }
                    }

                    // Outer pulse ring
                    Box(
                        modifier = Modifier
                            .size(130.dp * ringScale)
                            .clip(CircleShape)
                            .background(
                                GreenAccent.copy(alpha = ringAlpha * circleScale * 0.3f)
                            )
                    )

                    // Main circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(circleScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GreenAccent.copy(0.3f),
                                        GreenAccent.copy(0.1f)
                                    )
                                )
                            )
                            .glowEffect(GreenAccent, 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Checkmark
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier
                                .size(54.dp)
                                .scale(checkScale)
                                .alpha(checkAlpha)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Success Text ──────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .offset(y = textOffsetY.dp)
                ) {
                    Text(
                        "Added to Diary! 🎉",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        foodName,
                        color = GreenAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$calories kcal logged",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Dismiss pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GreenAccent.copy(0.15f))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Tap anywhere to dismiss",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Draw Particles ───────────────────────────────────────────────────────────

fun DrawScope.drawParticles(
    particles: List<Particle>,
    progress: Float,
    center: Offset
) {
    particles.forEach { particle ->
        val angleRad = Math.toRadians(particle.angle.toDouble()).toFloat()
        val distance = particle.speed * progress

        val x = center.x + cos(angleRad) * distance
        val y = center.y + sin(angleRad) * distance

        // Fade out near end
        val alpha = when {
            progress < 0.3f -> progress / 0.3f
            progress > 0.7f -> 1f - ((progress - 0.7f) / 0.3f)
            else -> 1f
        }.coerceIn(0f, 1f)

        // Size shrinks over time
        val currentSize = particle.size * (1f - progress * 0.5f)

        drawCircle(
            color = particle.color.copy(alpha = alpha),
            radius = currentSize,
            center = Offset(x, y)
        )
    }
}