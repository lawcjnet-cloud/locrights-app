package com.locrights.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locrights.app.ui.navigation.LocRightsApp
import com.locrights.app.ui.theme.LocRightsTheme
import com.locrights.app.ui.viewmodel.MainViewModel
import com.locrights.app.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as LocRightsApplication
        MainViewModelFactory(app.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            LocRightsTheme(themeMode = settings.themeMode) {
                LocRightsApp(viewModel = viewModel)
            }
        }
    }
}
