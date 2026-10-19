package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyApp
import com.example.ui.theme.StudyFlowTheme
import com.example.ui.theme.getAccentColor

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = state.preferences.isDarkMode
            val isEPaper = state.preferences.isEPaperMode
            val accent = getAccentColor(state.preferences.accentGlassTheme, isDark)

            StudyFlowTheme(isDarkMode = isDark, accentColor = accent, isEPaperMode = isEPaper) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudyApp(viewModel = viewModel)
                }
            }
        }
    }
}
