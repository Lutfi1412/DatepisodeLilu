package com.lutfilia.rundown

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lutfilia.rundown.ui.components.RomanticBackground
import com.lutfilia.rundown.ui.navigation.AppNavGraph
import com.lutfilia.rundown.ui.theme.RundownTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        setContent {
            RundownTheme {
                RomanticBackground {
                    AppNavGraph()
                }
            }
        }
    }
}
