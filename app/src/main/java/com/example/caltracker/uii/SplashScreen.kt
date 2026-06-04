package com.example.caltracker.uii

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caltracker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // ── Animations ────────────────────────────────────────────────────────────

    // Logo scale: bounces in
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    // Logo alpha
    var logoVisible by remember { mutableStateOf(false) }
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    // App name slide up
    var nameVisible by remember { mutableStateOf(false) }
    val nameOffsetY by animateFloatAsState(
        targetValue = if (nameVisible) 0f else 60f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "nameOffset"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (nameVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "nameAlpha"
    )

    // Tagline fade in
    var taglineVisible by remember { mutableStateOf(false) }
    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "taglineAlpha"
    )

    // Dots loading animation
    var dotsVisible by remember { mutableStateOf(false) }
    val dotsAlpha by animateFloatAsState(
        targetValue = if (dotsVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "dotsAlpha"
    )

    // Infinite dot pulse
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0)
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(200)
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(400)
        ),
        label = "dot3"
    )

    // ── Animation Sequence ────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(100)
        logoVisible = true
        delay(400)
        nameVisible = true
        delay(300)
        taglineVisible = true
        delay(400)
        dotsVisible = true
        delay(1200)
        onSplashComplete()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBg,
                        CardBg.copy(alpha = 0.8f),
                        DarkBg
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo emoji with scale + alpha
            Text(
                text = "🥗",
                fontSize = 90.sp,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(Modifier.height(24.dp))

            // App name slides up
            Text(
                text = "NutriSnap",
                color = GreenAccent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .alpha(nameAlpha)
                    .offset(y = nameOffsetY.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Tagline fades in
            Text(
                text = "AI-Powered Nutrition Tracking",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(Modifier.height(48.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(dotsAlpha)
            ) {
                repeat(3) { index ->
                    val alpha = when (index) {
                        0 -> dot1Alpha
                        1 -> dot2Alpha
                        else -> dot3Alpha
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(alpha)
                            .background(
                                GreenAccent,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }

        // Version text at bottom
        Text(
            text = "v1.0",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha)
        )
    }
}