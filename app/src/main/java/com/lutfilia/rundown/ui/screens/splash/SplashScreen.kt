package com.lutfilia.rundown.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.ui.components.CatMascot
import com.lutfilia.rundown.ui.components.GradientHeart
import com.lutfilia.rundown.ui.components.ShimmerText
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Couple
import kotlinx.coroutines.delay

/** Satu momen pembuka: nama masuk dari kiri-kanan, hati muncul membal di tengah. */
@Composable
fun SplashScreen(updateRequired: Boolean, onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var minTimeDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
        delay(3000)
        minTimeDone = true
    }
    // Lanjut ke home hanya kalau 3 detik sudah lewat DAN aplikasi tidak wajib diperbarui.
    LaunchedEffect(minTimeDone, updateRequired) {
        if (minTimeDone && !updateRequired) onFinished()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                AnimatedVisibility(
                    visible = started,
                    enter = slideInHorizontally(tween(800, 200, FastOutSlowInEasing)) { -it } + fadeIn(tween(800, 200))
                ) {
                    ShimmerText(Couple.PARTNER_A, MaterialTheme.typography.displayLarge)
                }
                AnimatedVisibility(
                    visible = started,
                    enter = scaleIn(
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                        initialScale = 0f
                    ) + fadeIn(tween(300, 700))
                ) {
                    GradientHeart(Modifier.padding(horizontal = 10.dp), size = 48.dp)
                }
                AnimatedVisibility(
                    visible = started,
                    enter = slideInHorizontally(tween(800, 200, FastOutSlowInEasing)) { it } + fadeIn(tween(800, 200))
                ) {
                    ShimmerText(Couple.PARTNER_B, MaterialTheme.typography.displayLarge)
                }
            }
            Spacer(Modifier.height(6.dp))
            AnimatedVisibility(
                visible = started,
                enter = fadeIn(tween(900, 1300)) + slideInVertically(tween(900, 1300)) { it / 2 }
            ) {
                Text(
                    "Semua rencana kita, rapi di satu tempat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WineSoft
                )
            }
            Spacer(Modifier.height(28.dp))
            AnimatedVisibility(
                visible = started,
                enter = fadeIn(tween(500, 700)) + scaleIn(tween(500, 700), initialScale = 0.7f)
            ) {
                CatMascot(size = 220.dp)
            }
        }
    }
}
