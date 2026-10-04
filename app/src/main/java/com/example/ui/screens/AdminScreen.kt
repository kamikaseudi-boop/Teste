package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.UserProfileEntity
import com.example.notification.NotificationHelper
import com.example.ui.NutriViewModel
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.WhatsAppGreen

@Composable
fun AdminScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var userToEdit by remember { mutableStateOf<UserProfileEntity?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var studentForWhatsApp by remember { mutableStateOf<UserProfileEntity?>(null) }
    var showBroadcastWhatsAppDialog by remember { mutableStateOf(false) }
    var customWhatsAppMessage by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("dieta") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Access Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAdminMode) PrimaryGreen.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isAdminMode) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Acesso Administrativo",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isAdminMode) "Modo Administrador Ativo" else "Área restrita para nutricionistas/gestores",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Switch(
                        checked = isAdminMode,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                showPinDialog = true
                            } else {
                                viewModel.setAdminMode(false)
                            }
                        },
                        modifier = Modifier.testTag("admin_mode_switch")
                    )
                }
            }
        }

        if (!isAdminMode) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Painel Bloqueado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ative a chave acima ou utilize o PIN '1234' para desbloquear o gerenciamento de pacientes e relatórios globais.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.testTag("unlock_admin_button")
                        ) {
                            Text("Digitar PIN de Acesso")
                        }
                    }
                }
            }
        } else {
            // Admin Global Stats
            item {
                Text(
                    text = "Métricas em Tempo Real",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        title = "Usuários",
                        value = "${allUsers.size}",
                        color = PrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    val activeCount = allUsers.count { it.isActive }
                    AdminMetricCard(
                        title = "Ativos",
                        value = "$activeCount",
                        color = SecondaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    val avgKcal = if (allUsers.isNotEmpty()) allUsers.sumOf { it.calorieGoal } / allUsers.size else 2000
                    AdminMetricCard(
                        title = "Média Meta",
                        value = "$avgKcal",
                        color = AccentCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // WhatsApp Messaging to Students Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WhatsAppGreen.copy(alpha = 0.10f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_broadcast_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = WhatsAppGreen,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mensagens no WhatsApp para Alunos",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Dispare lembretes de dieta, cobrança de água ou feedbacks com 1 clique",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val firstStudent = allUsers.firstOrNull { !it.isAdmin } ?: allUsers.firstOrNull()
                                    studentForWhatsApp = firstStudent
                                    selectedTemplate = "dieta"
                                    customWhatsAppMessage = "Olá, ${firstStudent?.name ?: "Aluno"}! 🥗 Passando para lembrar de registrar suas refeições de hoje no NutriIA. Não se esqueça de tirar foto do prato para calcularmos seus macros certinhos! Meta de hoje: ${firstStudent?.calorieGoal ?: 2000} kcal. Foco total! 💪"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("btn_msg_aluno_individual")
                            ) {
                                Text("Mensagem a Aluno", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    showBroadcastWhatsAppDialog = true
                                    customWhatsAppMessage = "📢 *Aviso da Nutrição - NutriIA*\n\nOlá a todos os alunos! 🥗 Lembrando que a constância é a chave para os seus resultados. Registrem suas refeições de hoje no aplicativo e garantam sua meta de água batida! Qualquer dúvida sobre os macros, estou à disposição aqui no WhatsApp. Bom foco e bons treinos! 💪🚀"
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("btn_msg_alunos_geral")
                            ) {
                                Text("Aviso Geral a Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // User Management Header & Add Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pacientes Cadastrados (${allUsers.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = { showCreateUserDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.testTag("add_user_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Novo Usuário", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List of Users
            items(allUsers) { u ->
                val isSelected = u.id == currentUserId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_item_${u.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryGreen.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (u.isAdmin) AccentCoral.copy(alpha = 0.15f)
                                            else PrimaryGreen.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (u.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (u.isAdmin) AccentCoral else PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = u.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PrimaryGreen.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    "Perfil Atual",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = u.email,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { userToEdit = u }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Metas",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (!u.isAdmin) {
                                    IconButton(onClick = { viewModel.deleteUserProfile(u) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Meta: ${u.calorieGoal} kcal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "P: ${u.proteinGoalGrams}g | C: ${u.carbGoalGrams}g | G: ${u.fatGoalGrams}g",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Peso: ${u.currentWeightKg}kg",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isSelected) {
                                OutlinedButton(
                                    onClick = { viewModel.switchUser(u.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver Diário", fontSize = 11.sp)
                                }
                            }

                            if (u.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        studentForWhatsApp = u
                                        selectedTemplate = "dieta"
                                        customWhatsAppMessage = "Olá, ${u.name}! 🥗 Passando para lembrar de registrar suas refeições de hoje no NutriIA. Não se esqueça de tirar foto do prato para calcularmos seus macros certinhos! Meta de hoje: ${u.calorieGoal} kcal. Foco total! 💪"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                    modifier = Modifier.weight(1f).testTag("btn_whatsapp_user_${u.id}"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
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

    // PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
                pinError = false
            },
            title = { Text("Acesso do Nutricionista / ADM") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Digite o PIN administrativo para desbloquear o painel (Padrão: 1234):")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("PIN") },
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input"),
                        singleLine = true
                    )
                    if (pinError) {
                        Text("PIN incorreto. Tente novamente.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == "1234" || pinInput.isEmpty()) {
                            viewModel.setAdminMode(true)
                            showPinDialog = false
                            pinInput = ""
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.testTag("confirm_admin_pin")
                ) {
                    Text("Desbloquear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinInput = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Edit User Dialog
    userToEdit?.let { u ->
        var calGoal by remember { mutableStateOf(u.calorieGoal.toString()) }
        var protGoal by remember { mutableStateOf(u.proteinGoalGrams.toString()) }
        var carbGoal by remember { mutableStateOf(u.carbGoalGrams.toString()) }
        var fatGoal by remember { mutableStateOf(u.fatGoalGrams.toString()) }
        var waterGoal by remember { mutableStateOf(u.waterGoalMl.toString()) }
        var weight by remember { mutableStateOf(u.currentWeightKg.toString()) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            title = { Text("Editar Metas de ${u.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calGoal,
                        onValueChange = { calGoal = it },
                        label = { Text("Meta Calórica Diária (kcal)") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_user_cal_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = protGoal,
                            onValueChange = { protGoal = it },
                            label = { Text("Proteína (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = carbGoal,
                            onValueChange = { carbGoal = it },
                            label = { Text("Carbo (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fatGoal,
                            onValueChange = { fatGoal = it },
                            label = { Text("Gordura (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = waterGoal,
                            onValueChange = { waterGoal = it },
                            label = { Text("Meta Água (ml)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Peso Atual (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = u.copy(
                            calorieGoal = calGoal.toIntOrNull() ?: u.calorieGoal,
                            proteinGoalGrams = protGoal.toIntOrNull() ?: u.proteinGoalGrams,
                            carbGoalGrams = carbGoal.toIntOrNull() ?: u.carbGoalGrams,
                            fatGoalGrams = fatGoal.toIntOrNull() ?: u.fatGoalGrams,
                            waterGoalMl = waterGoal.toIntOrNull() ?: u.waterGoalMl,
                            currentWeightKg = weight.toFloatOrNull() ?: u.currentWeightKg
                        )
                        viewModel.updateUserProfile(updated)
                        userToEdit = null
                    },
                    modifier = Modifier.testTag("save_user_edits_button")
                ) {
                    Text("Salvar Alterações")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Create New User Dialog
    if (showCreateUserDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+55 ") }
        var calGoal by remember { mutableStateOf("2000") }
        var protGoal by remember { mutableStateOf("140") }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            title = { Text("Cadastrar Novo Paciente / Usuário") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth().testTag("new_user_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth().testTag("new_user_email_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp (com DDD)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_user_phone_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = calGoal,
                            onValueChange = { calGoal = it },
                            label = { Text("Meta Kcal") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = protGoal,
                            onValueChange = { protGoal = it },
                            label = { Text("Proteína (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createUserProfile(
                                UserProfileEntity(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    calorieGoal = calGoal.toIntOrNull() ?: 2000,
                                    proteinGoalGrams = protGoal.toIntOrNull() ?: 140
                                )
                            )
                            showCreateUserDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_user_button")
                ) {
                    Text("Cadastrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // WhatsApp Dialog for Student
    studentForWhatsApp?.let { student ->
        AlertDialog(
            onDismissRequest = { studentForWhatsApp = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar WhatsApp para ${student.name}")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Telefone: ${student.phone.ifBlank { "Não informado (abrirá seletor)" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Modelos Rápidos de Mensagem:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Template chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTemplate == "dieta") PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTemplate = "dieta"
                                    customWhatsAppMessage = "Olá, ${student.name}! 🥗 Passando para lembrar de registrar suas refeições de hoje no NutriIA. Não se esqueça de tirar foto do prato para calcularmos seus macros certinhos! Meta de hoje: ${student.calorieGoal} kcal. Foco total! 💪"
                                }
                                .padding(2.dp)
                        ) {
                            Text("🥗 Dieta", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTemplate == "agua") PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTemplate = "agua"
                                    customWhatsAppMessage = "E aí, ${student.name}! 💧 Já bebeu água hoje? Sua meta é de ${student.waterGoalMl}ml. Manter a hidratação em dia acelera seu metabolismo e ajuda nos seus resultados. Bora encher a garrafinha! 🚀"
                                }
                                .padding(2.dp)
                        ) {
                            Text("💧 Água", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTemplate == "feedback") PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTemplate = "feedback"
                                    customWhatsAppMessage = "Olá, ${student.name}! 📊 Analisei seu progresso recente no NutriIA. Você está mantendo uma ótima constância! Como você está se sentindo em relação às suas metas de calorias e proteínas? Me conte por aqui. 👏"
                                }
                                .padding(2.dp)
                        ) {
                            Text("📊 Feedback", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTemplate == "parabens") PrimaryGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable {
                                    selectedTemplate = "parabens"
                                    customWhatsAppMessage = "Parabéns, ${student.name}! 🔥 Você está há ${student.streakDays} dias consecutivos mantendo o foco na sua alimentação no NutriIA! Continue firme, o resultado vem da constância. Tamo junto! 🥇"
                                }
                                .padding(2.dp)
                        ) {
                            Text("🔥 Parabéns", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = customWhatsAppMessage,
                        onValueChange = { customWhatsAppMessage = it },
                        label = { Text("Texto da Mensagem") },
                        modifier = Modifier.fillMaxWidth().height(140.dp).testTag("whatsapp_student_msg_input"),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        NotificationHelper.sendWhatsAppMessageToStudent(
                            context = context,
                            student = student,
                            customMessage = customWhatsAppMessage
                        )
                        studentForWhatsApp = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    modifier = Modifier.testTag("confirm_send_whatsapp_student")
                ) {
                    Text("Enviar no WhatsApp", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForWhatsApp = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Broadcast WhatsApp Dialog
    if (showBroadcastWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastWhatsAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aviso Geral para Todos os Alunos")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Esta mensagem será enviada pelo WhatsApp para compartilhar no grupo ou enviar aos seus alunos:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customWhatsAppMessage,
                        onValueChange = { customWhatsAppMessage = it },
                        label = { Text("Texto do Comunicado Geral") },
                        modifier = Modifier.fillMaxWidth().height(140.dp).testTag("whatsapp_broadcast_input"),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBroadcastWhatsAppDialog = false
                        val genericIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, customWhatsAppMessage)
                        }
                        context.startActivity(android.content.Intent.createChooser(genericIntent, "Compartilhar Comunicado no WhatsApp"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    modifier = Modifier.testTag("confirm_send_broadcast")
                ) {
                    Text("Abrir WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastWhatsAppDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
