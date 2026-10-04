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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notification.NotificationHelper
import com.example.ui.NutriViewModel
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.WhatsAppGreen

@Composable
fun ReportsScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val waterMl by viewModel.currentWaterMl.collectAsState()

    val totalGrams = (dailySummary.totalProtein + dailySummary.totalCarbs + dailySummary.totalFat).coerceAtLeast(1f)
    val proteinPct = ((dailySummary.totalProtein / totalGrams) * 100).toInt()
    val carbPct = ((dailySummary.totalCarbs / totalGrams) * 100).toInt()
    val fatPct = (100 - proteinPct - carbPct).coerceAtLeast(0)

    val calorieCompliance = if ((user?.calorieGoal ?: 2000) > 0) {
        ((dailySummary.totalCalories.toFloat() / (user?.calorieGoal ?: 2000)) * 100).toInt()
    } else 0

    val waterCompliance = if ((user?.waterGoalMl ?: 2500) > 0) {
        ((waterMl.toFloat() / (user?.waterGoalMl ?: 2500)) * 100).toInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "Relatório de Progresso em Tempo Real",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Análise detalhada de calorias, distribuição de macros e conquistas",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Summary Score Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
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
                        Text(
                            text = "Aderência do Dia",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$calorieCompliance% da meta",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportMetricCard(
                            title = "Consumidas",
                            value = "${dailySummary.totalCalories}",
                            unit = "kcal",
                            color = PrimaryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            title = "Restantes",
                            value = "${((user?.calorieGoal ?: 2000) - dailySummary.totalCalories).coerceAtLeast(0)}",
                            unit = "kcal",
                            color = AccentCoral,
                            modifier = Modifier.weight(1f)
                        )
                        ReportMetricCard(
                            title = "Água",
                            value = "$waterMl",
                            unit = "ml",
                            color = SecondaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Macro Distribution Bar
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Distribuição Percentual de Macros",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Proporção de energia obtida por grupo nutricional",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tri-color segmented bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        if (proteinPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(proteinPct.toFloat())
                                    .fillMaxSize()
                                    .background(MacroProteinColor)
                            )
                        }
                        if (carbPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(carbPct.toFloat())
                                    .fillMaxSize()
                                    .background(MacroCarbColor)
                            )
                        }
                        if (fatPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(fatPct.toFloat())
                                    .fillMaxSize()
                                    .background(MacroFatColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroLegendItem("Proteína ($proteinPct%)", MacroProteinColor, "${dailySummary.totalProtein.toInt()}g")
                        MacroLegendItem("Carbo ($carbPct%)", MacroCarbColor, "${dailySummary.totalCarbs.toInt()}g")
                        MacroLegendItem("Gordura ($fatPct%)", MacroFatColor, "${dailySummary.totalFat.toInt()}g")
                    }
                }
            }
        }

        // Achievements & Badges
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Conquistas e Engajamento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BadgeRow(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "${user?.streakDays ?: 1} Dias Consecutivos",
                        subtitle = "Consistência alimentar registrada com sucesso",
                        color = AccentCoral,
                        isUnlocked = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BadgeRow(
                        icon = Icons.Default.LocalDrink,
                        title = "Mestre da Hidratação",
                        subtitle = if (waterCompliance >= 80) "Meta de hidratação atingida!" else "$waterCompliance% da meta diária",
                        color = SecondaryBlue,
                        isUnlocked = waterCompliance >= 80
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BadgeRow(
                        icon = Icons.Default.FitnessCenter,
                        title = "Foco em Proteínas",
                        subtitle = "${dailySummary.totalProtein.toInt()}g de ${user?.proteinGoalGrams ?: 140}g de proteínas alcançadas",
                        color = MacroProteinColor,
                        isUnlocked = dailySummary.totalProtein >= (user?.proteinGoalGrams ?: 140) * 0.8f
                    )
                }
            }
        }

        // WhatsApp & Social Share Action Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryGreen.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Compartilhamento & Integração WhatsApp",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Compartilhe este relatório diretamente com seu nutricionista ou nas redes sociais.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                user?.let { u ->
                                    NotificationHelper.shareDailyProgressViaWhatsApp(
                                        context,
                                        u,
                                        dailySummary,
                                        u.phone
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reports_whatsapp_button")
                        ) {
                            Text("Enviar WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reports_share_social_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Redes Sociais", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(text = unit, fontSize = 10.sp, color = color)
        }
    }
}

@Composable
private fun MacroLegendItem(label: String, color: Color, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = amount, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BadgeRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) color.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) color else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}
