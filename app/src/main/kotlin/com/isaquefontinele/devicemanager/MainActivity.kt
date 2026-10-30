package com.isaquefontinele.devicemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.isaquefontinele.devicemanager.core.designsystem.theme.DeviceManagerTheme
import com.isaquefontinele.devicemanager.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DeviceManagerTheme {
                AppNavHost()
            }
        }
    }
}
