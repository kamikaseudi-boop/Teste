package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notification.NotificationHelper
import com.example.ui.NutriViewModel
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.WhatsAppGreen

@Composable
fun SettingsScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mealRemindersEnabled by viewModel.mealRemindersEnabled.collectAsState()
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsState()
    val darkOverride by viewModel.darkThemeOverride.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var nutritionistPhone by remember { mutableStateOf(user?.phone ?: "") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Configurações e Integrações",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Personalize lembretes, integração com WhatsApp e aparência",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Push Notifications Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lembretes e Notificações Push",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lembretes de Refeições", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Receba alertas no horário do café, almoço e jantar",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = mealRemindersEnabled,
                            onCheckedChange = { viewModel.toggleMealReminders(it) },
                            modifier = Modifier.testTag("meal_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lembretes de Hidratação", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Alertas a cada 2 horas para manter a hidratação em dia",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = waterRemindersEnabled,
                            onCheckedChange = { viewModel.toggleWaterReminders(it) },
                            modifier = Modifier.testTag("water_reminders_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Testar Notificações Imediatas:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendTestNotification("meal") },
                            modifier = Modifier.weight(1f).testTag("test_meal_notif_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Testar Refeição", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.sendTestNotification("water") },
                            modifier = Modifier.weight(1f).testTag("test_water_notif_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Testar Água", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // WhatsApp Integration Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Integração WhatsApp",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Vincule o WhatsApp do seu nutricionista ou personal trainer para envio rápido de relatórios com 1 clique.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nutritionistPhone,
                        onValueChange = { nutritionistPhone = it },
                        label = { Text("Número WhatsApp do Nutricionista") },
                        placeholder = { Text("5511999998888") },
                        modifier = Modifier.fillMaxWidth().testTag("settings_whatsapp_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            user?.let { u ->
                                viewModel.updateUserProfile(u.copy(phone = nutritionistPhone))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_whatsapp_button")
                    ) {
                        Text("Salvar Número do WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Appearance & Dark Mode Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = SecondaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aparência & Modo Escuro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tema do Aplicativo", fontWeight = FontWeight.SemiBold)
                            val themeLabel = when (darkOverride) {
                                true -> "Modo Escuro Forçado"
                                false -> "Modo Claro Forçado"
                                null -> "Padrão do Sistema"
                            }
                            Text(themeLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("toggle_dark_theme_button")
                        ) {
                            Text("Alternar")
                        }
                    }
                }
            }
        }

        // App Info & IA Specs
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NutriIA • Versão 1.0 (Pt-BR)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Modelo de Visão IA: Gemini 2.5 Flash Multimodal\n• Banco de Dados Local: SQLite Room\n• Suporte nativo a Modo Escuro e Notificações Push\n• Desenvolvido com Jetpack Compose & Material 3",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
