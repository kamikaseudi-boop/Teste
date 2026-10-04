package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntryEntity
import com.example.data.model.MealType
import com.example.notification.NotificationHelper
import com.example.ui.NutriViewModel
import com.example.ui.components.CalorieProgressRing
import com.example.ui.components.MacroProgressBar
import com.example.ui.components.MealItemCard
import com.example.ui.components.WaterTrackerCard
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroFiberColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WhatsAppGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: NutriViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToFoods: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val meals by viewModel.dailyMeals.collectAsState()
    val waterMl by viewModel.currentWaterMl.collectAsState()

    var quickAddMealType by remember { mutableStateOf<MealType?>(null) }
    var quickAddName by remember { mutableStateOf("") }
    var quickAddCalories by remember { mutableStateOf("") }
    var quickAddGrams by remember { mutableStateOf("100") }

    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var whatsappNumber by remember { mutableStateOf(user?.phone ?: "") }

    val parsedDate = try {
        LocalDate.parse(selectedDate)
    } catch (e: Exception) {
        LocalDate.now()
    }
    val isToday = parsedDate == LocalDate.now()
    val formattedDate = parsedDate.format(
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-BR"))
    ).replaceFirstChar { it.uppercase() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("diary_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Greeting & Streak Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Olá, ${user?.name?.split(" ")?.firstOrNull() ?: "Atleta"}! 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Acompanhamento diário de nutrição",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentCoral.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("streak_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Ofensiva",
                            tint = AccentCoral,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user?.streakDays ?: 1} dias",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentCoral
                            )
                        )
                    }
                }
            }
        }

        // Date Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.previousDay() },
                        modifier = Modifier.testTag("prev_day_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Dia Anterior"
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isToday) "Hoje ($formattedDate)" else formattedDate,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextDay() },
                        modifier = Modifier.testTag("next_day_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próximo Dia"
                        )
                    }
                }
            }
        }

        // Calorie Gauge Ring
        item {
            CalorieProgressRing(
                consumed = dailySummary.totalCalories,
                goal = user?.calorieGoal ?: 2000
            )
        }

        // Macro Progress Bars Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Macronutrientes do Dia",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    MacroProgressBar(
                        label = "Proteínas",
                        current = dailySummary.totalProtein,
                        goal = user?.proteinGoalGrams ?: 140,
                        barColor = MacroProteinColor
                    )
                    MacroProgressBar(
                        label = "Carboidratos",
                        current = dailySummary.totalCarbs,
                        goal = user?.carbGoalGrams ?: 220,
                        barColor = MacroCarbColor
                    )
                    MacroProgressBar(
                        label = "Gorduras",
                        current = dailySummary.totalFat,
                        goal = user?.fatGoalGrams ?: 60,
                        barColor = MacroFatColor
                    )
                    MacroProgressBar(
                        label = "Fibras",
                        current = dailySummary.totalFiber,
                        goal = 30,
                        barColor = MacroFiberColor
                    )
                }
            }
        }

        // Water Tracker
        item {
            WaterTrackerCard(
                currentMl = waterMl,
                goalMl = user?.waterGoalMl ?: 2500,
                onAddWater = { amount -> viewModel.addWater(amount) },
                onResetWater = { viewModel.resetWater() }
            )
        }

        // Share & WhatsApp Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showWhatsAppDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("whatsapp_share_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "WhatsApp Diário",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                FilledTonalButton(
                    onClick = {
                        user?.let { u ->
                            NotificationHelper.shareAchievementOnSocial(
                                context,
                                u,
                                u.streakDays,
                                dailySummary.totalCalories
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("social_share_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Conquista",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Meal Sections
        MealType.entries.forEach { mealType ->
            item {
                val mealItems = meals.filter { it.mealType == mealType }
                val mealTotalKcal = mealItems.sumOf { it.calories }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mealType.ptName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$mealTotalKcal kcal",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Row {
                                FilledTonalButton(
                                    onClick = onNavigateToScan,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("scan_button_${mealType.name}")
                                ) {
                                    Text("Foto IA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { quickAddMealType = mealType },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_meal_button_${mealType.name}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Adicionar",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Alimento", fontSize = 11.sp)
                                }
                            }
                        }

                        if (mealItems.isEmpty()) {
                            Text(
                                text = "Nenhum alimento registrado nesta refeição.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            mealItems.forEach { entry ->
                                MealItemCard(
                                    meal = entry,
                                    onDelete = { viewModel.deleteMeal(entry.id) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Quick Add Food Dialog
    quickAddMealType?.let { mType ->
        AlertDialog(
            onDismissRequest = { quickAddMealType = null },
            title = { Text("Registrar em ${mType.ptName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quickAddName,
                        onValueChange = { quickAddName = it },
                        label = { Text("Nome do Alimento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_food_name_input"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickAddCalories,
                            onValueChange = { quickAddCalories = it },
                            label = { Text("Calorias (kcal)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_food_calories_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = quickAddGrams,
                            onValueChange = { quickAddGrams = it },
                            label = { Text("Porção (g)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_food_grams_input"),
                            singleLine = true
                        )
                    }
                    TextButton(
                        onClick = {
                            quickAddMealType = null
                            onNavigateToFoods()
                        }
                    ) {
                        Text("🔍 Buscar no Banco de Alimentos")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kcal = quickAddCalories.toIntOrNull() ?: 150
                        val grams = quickAddGrams.toIntOrNull() ?: 100
                        val name = quickAddName.ifBlank { "Refeição Rápida" }
                        viewModel.addMeal(
                            foodName = name,
                            mealType = mType,
                            grams = grams,
                            calories = kcal,
                            carbs = (kcal * 0.45f) / 4f,
                            protein = (kcal * 0.25f) / 4f,
                            fat = (kcal * 0.30f) / 9f,
                            isAi = false
                        )
                        quickAddMealType = null
                        quickAddName = ""
                        quickAddCalories = ""
                    },
                    modifier = Modifier.testTag("confirm_quick_add_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { quickAddMealType = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // WhatsApp Dialog
    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Compartilhar com Nutricionista") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Envie seu relatório diário completo com calorias, proteínas, carbos, gorduras e água diretamente pelo WhatsApp.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("Número do Nutricionista (opcional)") },
                        placeholder = { Text("Ex: 5511999998888") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_number_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWhatsAppDialog = false
                        user?.let { u ->
                            NotificationHelper.shareDailyProgressViaWhatsApp(
                                context = context,
                                user = u,
                                summary = dailySummary,
                                nutritionistPhone = whatsappNumber
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    modifier = Modifier.testTag("confirm_whatsapp_send")
                ) {
                    Text("Abrir WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}
