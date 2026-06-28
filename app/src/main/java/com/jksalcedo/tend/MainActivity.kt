package com.jksalcedo.tend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jksalcedo.tend.ui.navigation.TendNavGraph
import com.jksalcedo.tend.ui.theme.TendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            TendTheme(dynamicColor = dynamic) {
                TendNavGraph()
            }
        }
    }
}