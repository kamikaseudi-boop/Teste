package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.MealType
import com.example.ui.AiScanUiState
import com.example.ui.NutriViewModel
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroFiberColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.PrimaryGreen

@Composable
fun AiScannerScreen(
    viewModel: NutriViewModel,
    onMealSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanState by viewModel.aiScanState.collectAsState()
    var customHintText by remember { mutableStateOf("") }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            viewModel.analyzeFoodImage(it, customHintText.ifBlank { null })
        }
    }

    // Photo Picker Launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { b ->
                    viewModel.analyzeFoodImage(b, customHintText.ifBlank { null })
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    when (val state = scanState) {
        is AiScanUiState.Idle -> {
            IdleScanView(
                onTakePhoto = { cameraLauncher.launch() },
                onPickGallery = {
                    pickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onSelectSampleDish = { sampleTitle, sampleHint ->
                    val sampleBitmap = createSampleFoodBitmap(sampleTitle)
                    viewModel.analyzeFoodImage(sampleBitmap, sampleHint)
                },
                customHintText = customHintText,
                onCustomHintChanged = { customHintText = it },
                modifier = modifier
            )
        }

        is AiScanUiState.Scanning -> {
            ScanningProgressView(modifier = modifier)
        }

        is AiScanUiState.Success -> {
            ScanResultView(
                state = state,
                onPortionChanged = { viewModel.updateAiPortionMultiplier(it) },
                onMealTypeSelected = { viewModel.updateAiSelectedMealType(it) },
                onSave = {
                    viewModel.saveAiDetectedMealToDiary()
                    onMealSaved()
                },
                onRetake = { viewModel.resetAiScanState() },
                modifier = modifier
            )
        }

        is AiScanUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Não foi possível analisar",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { viewModel.resetAiScanState() }) {
                    Text("Tentar Novamente")
                }
            }
        }
    }
}

@Composable
private fun IdleScanView(
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onSelectSampleDish: (String, String) -> Unit,
    customHintText: String,
    onCustomHintChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("idle_scan_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Scanner Nutricional com IA",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Reconhecimento visual instantâneo de pratos e macros",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Image Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_food_banner),
                            contentDescription = "Alimentos saudáveis",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Aponte a câmera para o prato ou envie uma foto da galeria. A inteligência artificial identificará cada alimento, estimará as gramas e calculará as calorias e macronutrientes automaticamente.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Custom Hint Input
        item {
            OutlinedTextField(
                value = customHintText,
                onValueChange = onCustomHintChanged,
                label = { Text("Dica adicional (opcional)") },
                placeholder = { Text("Ex: Feito com azeite, sem açúcar, frango grelhado...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_custom_hint_input"),
                singleLine = true
            )
        }

        // Action Buttons (Camera & Gallery)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("camera_capture_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Câmera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tirar Foto", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPickGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("gallery_picker_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Galeria")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeria", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Test with Preset Dishes
        item {
            Text(
                text = "⚡ Teste Imediato com Pratos Prontos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Toque em qualquer prato abaixo para simular o reconhecimento visual completo:",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        item {
            val sampleDishes = listOf(
                Pair("Prato Feito Brasileiro", "Arroz, feijão, patinho grelhado e salada mista"),
                Pair("Frango com Quinoa & Legumes", "Peito de frango grelhado com quinoa e brócolis"),
                Pair("Salmão com Aspargos e Abacate", "Filé de salmão com abacate e legumes"),
                Pair("Tapioca Fit com Ovos", "Tapioca com ovos mexidos e queijo minas"),
                Pair("Açaí Puro com Banana", "Tigela de açaí natural com banana e aveia")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sampleDishes.forEach { (title, desc) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSampleDish(title, desc) }
                            .testTag("sample_dish_${title.take(10)}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Testar IA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanningProgressView(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.12f))
                .border(2.dp, PrimaryGreen.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Analisando",
                tint = PrimaryGreen,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Analisando alimento...",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Identificando ingredientes e calculando macronutrientes com IA",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            color = PrimaryGreen,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun ScanResultView(
    state: AiScanUiState.Success,
    onPortionChanged: (Float) -> Unit,
    onMealTypeSelected: (MealType) -> Unit,
    onSave: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val multiplier = state.portionMultiplier
    val scaledCalories = (result.calories * multiplier).toInt()
    val scaledWeight = (result.estimatedWeightGrams * multiplier).toInt()
    val scaledCarbs = result.carbs * multiplier
    val scaledProtein = result.protein * multiplier
    val scaledFat = result.fat * multiplier
    val scaledFiber = result.fiber * multiplier

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("scan_result_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Header Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${(result.confidenceScore * 100).toInt()}% Precisão",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                        }

                        IconButton(onClick = onRetake) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Escanear outro",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = result.dishTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Porção estimada: $scaledWeight g • $scaledCalories kcal",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Portion Adjuster Slider
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ajustar Porção",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${(multiplier * 100).toInt()}% ($scaledWeight g)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = multiplier,
                        onValueChange = onPortionChanged,
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("portion_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50% (Pequena)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100% (Média)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("200% (Grande)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Macro Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroStatBox("Proteínas", "${scaledProtein.toInt()}g", MacroProteinColor, Modifier.weight(1f))
                MacroStatBox("Carbos", "${scaledCarbs.toInt()}g", MacroCarbColor, Modifier.weight(1f))
                MacroStatBox("Gorduras", "${scaledFat.toInt()}g", MacroFatColor, Modifier.weight(1f))
                MacroStatBox("Fibras", "${scaledFiber.toInt()}g", MacroFiberColor, Modifier.weight(1f))
            }
        }

        // Health Tip Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGreen.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = result.healthTip,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Ingredients Breakdown
        if (result.ingredients.isNotEmpty()) {
            item {
                Text(
                    text = "Ingredientes Identificados",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(result.ingredients) { ing ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = ing.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${(ing.grams * multiplier).toInt()}g • P:${(ing.protein * multiplier).toInt()}g C:${(ing.carbs * multiplier).toInt()}g G:${(ing.fat * multiplier).toInt()}g",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Text(
                            text = "${(ing.calories * multiplier).toInt()} kcal",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Meal Type Selector
        item {
            Text(
                text = "Salvar na Refeição:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                items(MealType.entries) { mType ->
                    FilterChip(
                        selected = state.selectedMealType == mType,
                        onClick = { onMealTypeSelected(mType) },
                        label = { Text(mType.ptName) },
                        leadingIcon = if (state.selectedMealType == mType) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        // Save & Retake Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Outra Foto")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(2f)
                        .testTag("save_ai_meal_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Salvar no Diário", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MacroStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun createSampleFoodBitmap(name: String): Bitmap {
    val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
    }

    // Background gradient color based on dish
    paint.color = android.graphics.Color.rgb(34, 197, 94)
    canvas.drawRect(0f, 0f, 300f, 300f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 24f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(name.take(18), 150f, 150f, paint)

    return bitmap
}
