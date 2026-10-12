package com.lutfilia.rundown.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Gold
import com.lutfilia.rundown.ui.theme.Lilac
import com.lutfilia.rundown.ui.theme.Petal
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseLight
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private data class Floater(
    val x: Float,
    val speed: Int,
    val offset: Float,
    val size: Float,
    val alpha: Float,
    val sway: Float,
    val color: Color
)

private data class Sparkle(val x: Float, val y: Float, val phase: Int, val offset: Float)

/** Latar seluruh aplikasi: gradasi blush-lilac, hati yang pelan naik, dan kerlip emas. */
@Composable
fun RomanticBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val floaters = remember {
        val r = Random(11)
        val palette = listOf(Rose, Lilac, Gold, BlushDeep, RoseLight)
        List(16) {
            Floater(
                x = r.nextFloat(),
                speed = 1 + r.nextInt(3),
                offset = r.nextFloat(),
                size = 6f + r.nextFloat() * 12f,
                alpha = 0.12f + r.nextFloat() * 0.2f,
                sway = 1f + r.nextFloat() * 2f,
                color = palette[r.nextInt(palette.size)]
            )
        }
    }
    val sparkles = remember {
        val r = Random(5)
        List(10) { Sparkle(r.nextFloat(), r.nextFloat(), 1 + r.nextInt(3), r.nextFloat()) }
    }
    val transition = rememberInfiniteTransition(label = "romanticBg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "bgTime"
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF8FA), Petal, Color(0xFFF9EAF6))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // kabut warna di dua sudut
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(RoseLight.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(w * 0.95f, h * 0.04f),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.95f, h * 0.04f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Lilac.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(0f, h * 0.96f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(0f, h * 0.96f)
            )
            // hati melayang
            val time = t
            floaters.forEach { f ->
                val p = (time * f.speed + f.offset) % 1f
                val y = h * (1.08f - p * 1.2f)
                val x = w * f.x + sin(p * 2f * PI.toFloat() * f.sway) * 18.dp.toPx()
                val fade = min(p / 0.15f, 1f) * min((1f - p) / 0.2f, 1f)
                drawPath(
                    path = heartPath(x, y, f.size.dp.toPx()),
                    color = f.color.copy(alpha = f.alpha * fade)
                )
            }
            // kerlip
            sparkles.forEach { s ->
                val a = ((sin((time * s.phase + s.offset) * 2f * PI.toFloat()) + 1f) / 2f) * 0.55f
                val cx = w * s.x
                val cy = h * s.y
                val r = 5.dp.toPx()
                val c = Gold.copy(alpha = a)
                drawLine(c, Offset(cx - r, cy), Offset(cx + r, cy), 1.4.dp.toPx(), StrokeCap.Round)
                drawLine(c, Offset(cx, cy - r), Offset(cx, cy + r), 1.4.dp.toPx(), StrokeCap.Round)
                val d = r * 0.5f
                drawLine(c, Offset(cx - d, cy - d), Offset(cx + d, cy + d), 1.dp.toPx(), StrokeCap.Round)
                drawLine(c, Offset(cx - d, cy + d), Offset(cx + d, cy - d), 1.dp.toPx(), StrokeCap.Round)
            }
        }
        content()
    }
}
