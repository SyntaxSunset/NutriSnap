package com.example.caltracker.uii

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.caltracker.data.NutritionResult
import com.example.caltracker.data.MultiFoodResult
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import java.io.File
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.*

@Composable
fun LogFoodScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val state by viewModel.analysisState.collectAsState()
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                viewModel.setImageUri(uri)
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.setImageUri(it)
        }
    }

    // Camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Text("Log Food", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Take a photo or upload from gallery", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        // ── Image Preview or Upload Area ─────────────────────────────────
        if (state.imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = "Food photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Retake button
                IconButton(
                    onClick = { viewModel.clearAnalysis() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(DarkBg.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextPrimary)
                }
            }
        } else {
            // Upload area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(CardBg, RoundedCornerShape(20.dp))
                    .border(1.dp, DividerColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍕", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No photo selected", color = TextSecondary, fontSize = 15.sp)
                    Text("Use the buttons below to add food", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Camera / Gallery Buttons ─────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = GreenAccent)
                Spacer(Modifier.width(6.dp))
                Text("Camera", color = TextPrimary)
            }
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = ProteinColor)
                Spacer(Modifier.width(6.dp))
                Text("Gallery", color = TextPrimary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Analyze Button ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.imageUri != null &&
                    state.result == null &&
                    state.multiResult == null &&
                    !state.isLoading
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Single food button
                Button(
                    onClick = {
                        state.imageUri?.let { uri ->
                            viewModel.analyzeImage(context, uri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DarkBg)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Analyse Single Food",
                        color = DarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                // Multi food button
                Button(
                    onClick = {
                        state.imageUri?.let { uri ->
                            viewModel.analyzeMultipleItems(context, uri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProteinColor),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.DinnerDining, contentDescription = null, tint = DarkBg)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Detect Multiple Foods",
                        color = DarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // ── Loading State ────────────────────────────────────────────────
        AnimatedVisibility(visible = state.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                // AI scanning animation
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val scanPulse = rememberInfiniteTransition(label = "scan")
                    val scanAlpha by scanPulse.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scanAlpha"
                    )
                    val scanScale by scanPulse.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scanScale"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(scanScale)
                    ) {
                        Text("🤖", fontSize = 40.sp, modifier = Modifier.alpha(scanAlpha))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "AI is analyzing...",
                            color = GreenAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(scanAlpha)
                        )
                        Text(
                            "Powered by Gemini",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                ShimmerLoadingCard()
            }
        }

        // ── Error State ──────────────────────────────────────────────────
        AnimatedVisibility(visible = state.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = ErrorColor)
                    Spacer(Modifier.width(8.dp))
                    Text(state.error ?: "", color = ErrorColor, fontSize = 13.sp)
                }
            }
        }

        // ── Result Card ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.result != null,
            enter = slideInVertically() + fadeIn()
        ) {
            state.result?.let { result ->
                NutritionResultCard(
                    result = result,
                    onConfirm = { adjustedResult -> viewModel.confirmAndSaveEntry(adjustedResult) },
                    onRetry = { viewModel.clearAnalysis() }
                )
            }
        }
        // ── Multi Food Result ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.multiResult != null,
            enter = slideInVertically() + fadeIn()
        ) {
            state.multiResult?.let { multiResult ->
                MultiFoodResultCard(
                    multiResult = multiResult,
                    onConfirm = { selected ->
                        viewModel.confirmAndSaveMultipleEntries(selected)
                    },
                    onRetry = { viewModel.clearAnalysis() }
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    // ── Success Animation ────────────────────────────────────────────────────
    val showSuccess by viewModel.showSuccess.collectAsState()

    showSuccess?.let { (foodName, calories) ->
        SuccessAnimationOverlay(
            foodName = foodName,
            calories = calories,
            onDismiss = { viewModel.dismissSuccess() }
        )
    }
}

// ─── Nutrition Result Card ────────────────────────────────────────────────────

@Composable
fun NutritionResultCard(
    result: NutritionResult,
    onConfirm: (NutritionResult) -> Unit,
    onRetry: () -> Unit
) {
    var portionMultiplier by remember { mutableStateOf(1f) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(result.foodName) }

    val adjustedCalories = (result.calories * portionMultiplier).toInt()
    val adjustedProtein = result.protein * portionMultiplier
    val adjustedCarbs = result.carbs * portionMultiplier
    val adjustedFat = result.fat * portionMultiplier

    val adjustedResult = result.copy(
        foodName = editedName,
        calories = adjustedCalories,
        protein = adjustedProtein,
        carbs = adjustedCarbs,
        fat = adjustedFat
    )

    SectionCard {
        // ── Header with Editable Name ─────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✨", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AI Analysis Complete",
                    color = GreenAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                if (isEditingName) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GreenAccent
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isEditingName = false }) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Done",
                                    tint = GreenAccent
                                )
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isEditingName = true }
                    ) {
                        Text(
                            editedName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit name",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        "Tap name to edit",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Calorie Display ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$adjustedCalories",
                    color = GreenAccent,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("calories", color = TextSecondary, fontSize = 13.sp)
                if (portionMultiplier != 1f) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Base: ${result.calories} kcal × ${
                            if (portionMultiplier == portionMultiplier.toInt().toFloat())
                                portionMultiplier.toInt().toString()
                            else
                                String.format("%.1f", portionMultiplier)
                        }x",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Macro Row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MacroStatChip("Protein", "${adjustedProtein.toInt()}g", ProteinColor)
            MacroStatChip("Carbs", "${adjustedCarbs.toInt()}g", CarbsColor)
            MacroStatChip("Fat", "${adjustedFat.toInt()}g", FatColor)
        }

        Spacer(Modifier.height(16.dp))

        // ── Portion Adjuster ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceBg, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Portion Size", color = TextSecondary, fontSize = 13.sp)
                Text(
                    when (portionMultiplier) {
                        0.25f -> "¼ serving"
                        0.5f -> "½ serving"
                        0.75f -> "¾ serving"
                        1f -> "1 serving"
                        1.5f -> "1½ servings"
                        2f -> "2 servings"
                        2.5f -> "2½ servings"
                        3f -> "3 servings"
                        else -> "${String.format("%.1f", portionMultiplier)}x"
                    },
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5f, 1f, 1.5f, 2f).forEach { portion ->
                    val isSelected = portionMultiplier == portion
                    OutlinedButton(
                        onClick = { portionMultiplier = portion },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) GreenAccent else DividerColor
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                GreenAccent.copy(alpha = 0.15f)
                            else
                                androidx.compose.ui.graphics.Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            when (portion) {
                                0.5f -> "½x"
                                1f -> "1x"
                                1.5f -> "1½x"
                                2f -> "2x"
                                else -> "${portion}x"
                            },
                            color = if (isSelected) GreenAccent else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Slider(
                value = portionMultiplier,
                onValueChange = { portionMultiplier = it },
                valueRange = 0.25f..3f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = GreenAccent,
                    activeTrackColor = GreenAccent,
                    inactiveTrackColor = DividerColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("¼x", color = TextMuted, fontSize = 10.sp)
                Text("1x", color = TextMuted, fontSize = 10.sp)
                Text("2x", color = TextMuted, fontSize = 10.sp)
                Text("3x", color = TextMuted, fontSize = 10.sp)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Confidence: ${result.confidence}",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(16.dp))

        // ── Action Buttons ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Text("Retake", color = TextSecondary)
            }
            Button(
                onClick = { onConfirm(adjustedResult) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = DarkBg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Add to Diary",
                    color = DarkBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MultiFoodResultCard(
    multiResult: MultiFoodResult,
    onConfirm: (List<NutritionResult>) -> Unit,
    onRetry: () -> Unit
) {
    val selectedItems = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            multiResult.foods.indices.forEach { put(it, true) }
        }
    }

    val selectedFoods = multiResult.foods.filterIndexed { index, _ ->
        selectedItems[index] == true
    }

    val totalCalories = selectedFoods.sumOf { it.calories }
    val totalProtein = selectedFoods.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = selectedFoods.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = selectedFoods.sumOf { it.fat.toDouble() }.toFloat()

    SectionCard {
        // ── Header ────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🍽️", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Multiple Foods Detected",
                    color = ProteinColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${multiResult.foods.size} items found",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Total calories ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProteinColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$totalCalories",
                        color = GreenAccent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("total kcal", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${totalProtein.toInt()}g",
                        color = ProteinColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("protein", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${totalCarbs.toInt()}g",
                        color = CarbsColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("carbs", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${totalFat.toInt()}g",
                        color = FatColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("fat", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Food Items List ───────────────────────────────────────────────
        Text(
            "Select items to add:",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(8.dp))

        multiResult.foods.forEachIndexed { index, food ->
            val isSelected = selectedItems[index] == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedItems[index] = !isSelected },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        GreenAccent.copy(alpha = 0.1f)
                    else
                        SurfaceBg
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(1.dp, GreenAccent.copy(alpha = 0.5f))
                else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { selectedItems[index] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GreenAccent,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            food.foodName,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "P:${food.protein.toInt()}g",
                                color = ProteinColor,
                                fontSize = 11.sp
                            )
                            Text(
                                "C:${food.carbs.toInt()}g",
                                color = CarbsColor,
                                fontSize = 11.sp
                            )
                            Text(
                                "F:${food.fat.toInt()}g",
                                color = FatColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        "${food.calories} kcal",
                        color = if (isSelected) GreenAccent else TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Action Buttons ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Text("Retake", color = TextSecondary)
            }
            Button(
                onClick = {
                    if (selectedFoods.isNotEmpty()) {
                        onConfirm(selectedFoods)
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFoods.isEmpty())
                        TextMuted
                    else
                        GreenAccent
                ),
                enabled = selectedFoods.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Add ${selectedFoods.size} Items",
                    color = DarkBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
// ─── API Key Dialog ───────────────────────────────────────────────────────────

    @Composable
    fun ApiKeyDialog(currentKey: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
        var key by remember { mutableStateOf(currentKey) }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = CardBg,
            title = { Text("Gemini API Key", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter your Google Gemini API key to enable AI food analysis. Get one free at aistudio.google.com",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("AIzaSy...", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm(key) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Save", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

// ─── Helper ───────────────────────────────────────────────────────────────────

    fun createImageUri(context: Context): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "food_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

