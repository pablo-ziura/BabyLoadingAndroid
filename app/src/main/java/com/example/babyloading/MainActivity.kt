package com.example.babyloading

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.babyloading.navigation.AppNavigation
import com.example.babyloading.core.designsystem.theme.BabyLoadingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BabyLoadingTheme {
                AppNavigation()
            }
        }
    }
}
