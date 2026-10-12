//package com.lutfilia.rundown.ui.components
//
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.keyframes
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.StrokeJoin
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.rotate
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import com.lutfilia.rundown.ui.theme.Rose
//import com.lutfilia.rundown.ui.theme.RoseDark
//
///**
// * Kucing maskot yang digambar penuh dengan Canvas (tanpa file gambar).
// * - ekor bergoyang, kedip berkala, bernapas pelan
// * - moving = true  -> melompat-lompat kecil (dipakai saat berjalan di peta)
// * - sleepy = true  -> mata terpejam
// */
//@Composable
//fun CatMascot(
//    modifier: Modifier = Modifier,
//    size: Dp = 64.dp,
//    moving: Boolean = false,
//    facingLeft: Boolean = false,
//    sleepy: Boolean = false,
//    bodyColor: Color = Color(0xFFFFF6EE),
//    accent: Color = Rose
//) {
//    val transition = rememberInfiniteTransition(label = "cat")
//    val tail by transition.animateFloat(
//        initialValue = -16f,
//        targetValue = 16f,
//        animationSpec = infiniteRepeatable(
//            tween(if (moving) 380 else 1100, easing = FastOutSlowInEasing),
//            RepeatMode.Reverse
//        ),
//        label = "tail"
//    )
//    val blink by transition.animateFloat(
//        initialValue = 1f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            keyframes<Float> {
//                durationMillis = 3600
//                1f at 0
//                1f at 3300
//                0.08f at 3420
//                1f at 3540
//                1f at 3600
//            }
//        ),
//        label = "blink"
//    )
//    val hop by transition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            tween(if (moving) 260 else 1800, easing = FastOutSlowInEasing),
//            RepeatMode.Reverse
//        ),
//        label = "hop"
//    )
//
//    Canvas(
//        modifier
//            .size(size)
//            .graphicsLayer {
//                scaleX = if (facingLeft) -1f else 1f
//                translationY = -(if (moving) 8.dp.toPx() else 2.dp.toPx()) * hop
//            }
//    ) {
//        val s = this.size.minDimension / 100f
//        val ink = Color(0xFF4F1D3A)
//        val line = Color(0xFFD8A394)
//        val fill = bodyColor
//        val lw = Stroke(width = 1.8f * s)
//
//        // ekor
//        val tailPath = Path().apply {
//            moveTo(68f * s, 84f * s)
//            cubicTo(92f * s, 86f * s, 98f * s, 62f * s, 86f * s, 52f * s)
//        }
//        rotate(tail, Offset(68f * s, 84f * s)) {
//            drawPath(tailPath, line, style = Stroke(10f * s, cap = StrokeCap.Round))
//            drawPath(tailPath, fill, style = Stroke(7f * s, cap = StrokeCap.Round))
//        }
//
//        // badan + kaki
//        drawOval(fill, Offset(27f * s, 54f * s), Size(46f * s, 38f * s))
//        drawOval(line, Offset(27f * s, 54f * s), Size(46f * s, 38f * s), style = lw)
//        drawOval(fill, Offset(33f * s, 86f * s), Size(13f * s, 9f * s))
//        drawOval(line, Offset(33f * s, 86f * s), Size(13f * s, 9f * s), style = lw)
//        drawOval(fill, Offset(54f * s, 86f * s), Size(13f * s, 9f * s))
//        drawOval(line, Offset(54f * s, 86f * s), Size(13f * s, 9f * s), style = lw)
//
//        // telinga
//        val earL = Path().apply {
//            moveTo(27f * s, 30f * s); lineTo(30f * s, 7f * s); lineTo(45f * s, 20f * s); close()
//        }
//        val earR = Path().apply {
//            moveTo(73f * s, 30f * s); lineTo(70f * s, 7f * s); lineTo(55f * s, 20f * s); close()
//        }
//        listOf(earL, earR).forEach {
//            drawPath(it, fill)
//            drawPath(it, line, style = Stroke(1.8f * s, join = StrokeJoin.Round))
//        }
//        val inL = Path().apply {
//            moveTo(31f * s, 27f * s); lineTo(33f * s, 14f * s); lineTo(41f * s, 21f * s); close()
//        }
//        val inR = Path().apply {
//            moveTo(69f * s, 27f * s); lineTo(67f * s, 14f * s); lineTo(59f * s, 21f * s); close()
//        }
//        drawPath(inL, accent.copy(alpha = 0.5f))
//        drawPath(inR, accent.copy(alpha = 0.5f))
//
//        // kepala
//        drawCircle(fill, 26f * s, Offset(50f * s, 40f * s))
//        drawCircle(line, 26f * s, Offset(50f * s, 40f * s), style = lw)
//
//        // pipi merona
//        drawCircle(accent.copy(alpha = 0.28f), 5f * s, Offset(35f * s, 48f * s))
//        drawCircle(accent.copy(alpha = 0.28f), 5f * s, Offset(65f * s, 48f * s))
//
//        // mata
//        val eyeY = 40f * s
//        listOf(41f, 59f).forEach { ex ->
//            val cx = ex * s
//            if (sleepy) {
//                val p = Path().apply {
//                    moveTo(cx - 3.6f * s, eyeY)
//                    cubicTo(cx - 2f * s, eyeY + 3.6f * s, cx + 2f * s, eyeY + 3.6f * s, cx + 3.6f * s, eyeY)
//                }
//                drawPath(p, ink, style = Stroke(1.6f * s, cap = StrokeCap.Round))
//            } else {
//                val h = 4.6f * s * blink
//                drawOval(ink, Offset(cx - 3.2f * s, eyeY - h), Size(6.4f * s, h * 2f))
//                drawCircle(Color.White, 1.1f * s, Offset(cx - 0.8f * s, eyeY - h * 0.45f))
//            }
//        }
//
//        // hidung, mulut, kumis
//        drawCircle(accent, 2.2f * s, Offset(50f * s, 46.5f * s))
//        val mouth = Path().apply {
//            moveTo(50f * s, 48.7f * s)
//            cubicTo(50f * s, 53f * s, 46f * s, 54f * s, 43.5f * s, 51.5f * s)
//            moveTo(50f * s, 48.7f * s)
//            cubicTo(50f * s, 53f * s, 54f * s, 54f * s, 56.5f * s, 51.5f * s)
//        }
//        drawPath(mouth, ink, style = Stroke(1.3f * s, cap = StrokeCap.Round))
//        val whisk = line.copy(alpha = 0.9f)
//        drawLine(whisk, Offset(31f * s, 46f * s), Offset(15f * s, 43f * s), 1.2f * s, StrokeCap.Round)
//        drawLine(whisk, Offset(31f * s, 49f * s), Offset(15f * s, 51f * s), 1.2f * s, StrokeCap.Round)
//        drawLine(whisk, Offset(69f * s, 46f * s), Offset(85f * s, 43f * s), 1.2f * s, StrokeCap.Round)
//        drawLine(whisk, Offset(69f * s, 49f * s), Offset(85f * s, 51f * s), 1.2f * s, StrokeCap.Round)
//
//        // pita kecil di leher
//        val bowL = Path().apply {
//            moveTo(50f * s, 64f * s); lineTo(39f * s, 57f * s); lineTo(39f * s, 71f * s); close()
//        }
//        val bowR = Path().apply {
//            moveTo(50f * s, 64f * s); lineTo(61f * s, 57f * s); lineTo(61f * s, 71f * s); close()
//        }
//        drawPath(bowL, accent)
//        drawPath(bowR, accent)
//        drawCircle(RoseDark, 3.3f * s, Offset(50f * s, 64f * s))
//    }
//}

package com.lutfilia.rundown.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lutfilia.rundown.R
import com.lutfilia.rundown.ui.theme.Rose

@Composable
fun CatMascot(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    moving: Boolean = false,
    facingLeft: Boolean = false,
    sleepy: Boolean = false,
    zoom: Float = 1f,                      // <- baru
    bodyColor: Color = Color(0xFFFFF6EE),
    accent: Color = Rose
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = !sleepy,
        speed = if (moving) 1.6f else 1f
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = if (facingLeft) -zoom else zoom
                scaleY = zoom
                alpha = if (sleepy) 0.75f else 1f
            }
    )
}