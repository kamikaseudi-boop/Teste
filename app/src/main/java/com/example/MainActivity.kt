package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.NutriMainScreen
import com.example.ui.NutriViewModel
import com.example.ui.theme.NutriTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NutriViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkOverride by viewModel.darkThemeOverride.collectAsState()
            val isDark = darkOverride ?: isSystemInDarkTheme()

            NutriTheme(darkTheme = isDark) {
                NutriMainScreen(viewModel = viewModel)
            }
        }
    }
}
