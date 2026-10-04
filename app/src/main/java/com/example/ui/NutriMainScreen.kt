package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.notification.NotificationHelper
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AiScannerScreen
import com.example.ui.screens.DiaryScreen
import com.example.ui.screens.FoodDatabaseScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.WhatsAppGreen

enum class NutriNavigationTab(val label: String, val icon: ImageVector) {
    DIARY("Diário", Icons.Default.Today),
    SCANNER("Scanner IA", Icons.Default.AutoAwesome),
    FOODS("Alimentos", Icons.Default.RestaurantMenu),
    REPORTS("Relatórios", Icons.Default.BarChart),
    ADMIN("Admin", Icons.Default.AdminPanelSettings),
    SETTINGS("Config", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriMainScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NutriNavigationTab.DIARY) }
    val darkOverride by viewModel.darkThemeOverride.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()

    // BackHandler: Navigate back to Diary if on any sub-tab
    BackHandler(enabled = currentTab != NutriNavigationTab.DIARY) {
        currentTab = NutriNavigationTab.DIARY
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = PrimaryGreen.copy(alpha = 0.2f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon),
                                contentDescription = "NutriIA Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NutriIA",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Contador de Calorias IA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // WhatsApp Share quick action
                    IconButton(
                        onClick = {
                            user?.let { u ->
                                NotificationHelper.shareDailyProgressViaWhatsApp(
                                    context = context,
                                    user = u,
                                    summary = summary,
                                    nutritionistPhone = u.phone
                                )
                            }
                        },
                        modifier = Modifier.testTag("top_bar_whatsapp_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "💬",
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("top_bar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (darkOverride == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Alternar Tema",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NutriNavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NutriNavigationTab.DIARY -> {
                    DiaryScreen(
                        viewModel = viewModel,
                        onNavigateToScan = { currentTab = NutriNavigationTab.SCANNER },
                        onNavigateToFoods = { currentTab = NutriNavigationTab.FOODS }
                    )
                }

                NutriNavigationTab.SCANNER -> {
                    AiScannerScreen(
                        viewModel = viewModel,
                        onMealSaved = { currentTab = NutriNavigationTab.DIARY }
                    )
                }

                NutriNavigationTab.FOODS -> {
                    FoodDatabaseScreen(
                        viewModel = viewModel,
                        onFoodAdded = { currentTab = NutriNavigationTab.DIARY }
                    )
                }

                NutriNavigationTab.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel
                    )
                }

                NutriNavigationTab.ADMIN -> {
                    AdminScreen(
                        viewModel = viewModel
                    )
                }

                NutriNavigationTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
