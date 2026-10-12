package com.lutfilia.rundown.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Gold
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Bentuk hati. `s` = setengah lebar kira-kira. */
fun heartPath(cx: Float, cy: Float, s: Float): Path = Path().apply {
    moveTo(cx, cy + s * 0.95f)
    cubicTo(cx - s * 1.9f, cy - s * 0.15f, cx - s * 0.95f, cy - s * 1.35f, cx, cy - s * 0.45f)
    cubicTo(cx + s * 0.95f, cy - s * 1.35f, cx + s * 1.9f, cy - s * 0.15f, cx, cy + s * 0.95f)
    close()
}

/** Jam sekarang, update tiap detik. Dipakai untuk status rundown (lokal, tanpa API). */
@Composable
fun rememberNow(): State<LocalDateTime> = produceState(initialValue = LocalDateTime.now()) {
    while (true) {
        delay(1000L)
        value = LocalDateTime.now()
    }
}

/** Kilau yang bergerak: dipakai untuk skeleton loading. Pasang .clip(shape) sebelum modifier ini. */
fun Modifier.shimmer(
    base: Color = BlushDeep.copy(alpha = 0.35f),
    highlight: Color = Color.White.copy(alpha = 0.85f)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    this.drawBehind {
        val w = size.width.coerceAtLeast(1f)
        val startX = -w + progress * w * 2.4f
        drawRect(base)
        drawRect(
            Brush.linearGradient(
                colors = listOf(Color.Transparent, highlight, Color.Transparent),
                start = Offset(startX, 0f),
                end = Offset(startX + w, size.height * 0.5f)
            )
        )
    }
}

/** Teks dengan kilau warna yang berjalan pelan. */
@Composable
fun ShimmerText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(RoseDark, Rose, Gold, Rose, RoseDark)
) {
    val transition = rememberInfiniteTransition(label = "shimmerText")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "shift"
    )
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(shift, 0f),
        end = Offset(shift + 300f, 0f),
        tileMode = TileMode.Mirror
    )
    Text(text = text, modifier = modifier, style = style.copy(brush = brush))
}

/** Muncul dengan efek "pop" (membal kecil). `index` dipakai untuk jeda berurutan. */
fun Modifier.popIn(index: Int = 0): Modifier = composed {
    val sc = remember { Animatable(0.85f) }
    val al = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index * 60L).coerceAtMost(480L))
        launch { al.animateTo(1f, tween(300)) }
        sc.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    graphicsLayer {
        scaleX = sc.value
        scaleY = sc.value
        alpha = al.value
    }
}

/** Klik dengan efek menciut-membal. Bisa tambah long-press. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounceClick(
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.combinedClickable(
        interactionSource = source,
        indication = null,
        enabled = enabled,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

/** Denyut halus berulang. */
fun Modifier.pulse(amount: Float = 0.06f, periodMs: Int = 1400): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "pulse")
    val s by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + amount,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    graphicsLayer {
        scaleX = s
        scaleY = s
    }
}

/** Detak jantung "dug-dug". */
@Composable
fun rememberHeartbeat(): State<Float> {
    val transition = rememberInfiniteTransition(label = "heartbeat")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes<Float> {
                durationMillis = 1300
                1f at 0
                1.22f at 140 using FastOutSlowInEasing
                1f at 300
                1.14f at 440 using FastOutSlowInEasing
                1f at 640
                1f at 1300
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "beat"
    )
}
