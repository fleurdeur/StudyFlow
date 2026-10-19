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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            com.example.nativebridge.NativeBridge.clearVectorIndex()
        }
    }

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val preferences = state.preferences
            val isDark = preferences.isDarkMode
            val isEPaper = preferences.isEPaperMode
            val accent = getAccentColor(preferences.accentGlassTheme, isDark)

            StudyFlowTheme(isDarkMode = isDark, accentColor = accent, isEPaperMode = isEPaper) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudyApp(viewModel = viewModel)
                }
            }
        }
    }
}
