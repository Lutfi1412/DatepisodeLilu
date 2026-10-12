package com.lutfilia.rundown.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.AmberSoft
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Gold
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.RoseLight
import com.lutfilia.rundown.ui.theme.Sage
import com.lutfilia.rundown.ui.theme.SageSoft
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Couple
import kotlin.math.roundToInt

// ======================================================================= Kartu

/** Kartu putih lembut dengan bayangan merah muda. Dipakai di hampir semua layar. */
@Composable
fun LoveCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    selected: Boolean = false,
    container: Color = Color.White.copy(alpha = 0.92f),
    border: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) Rose else (border ?: BlushDeep.copy(alpha = 0.5f)),
        label = "cardBorder"
    )
    Column(
        modifier = modifier
            .shadow(8.dp, shape, ambientColor = Rose.copy(alpha = 0.15f), spotColor = Rose.copy(alpha = 0.25f))
            .clip(shape)
            .background(container)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape),
        content = content
    )
}

/**
 * Bentuk tiket: sudut membulat + takik setengah lingkaran di atas & bawah pada posisi [notchX].
 * Dipakai untuk kartu tanggal supaya terasa seperti tiket kencan.
 */
class TicketShape(
    private val corner: Dp = 22.dp,
    private val notchRadius: Dp = 9.dp,
    private val notchX: Dp = 84.dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { corner.toPx() }
        val n = with(density) { notchRadius.toPx() }
        val x = with(density) { notchX.toPx() }
        val base = Path().apply {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r, r)))
        }
        val top = Path().apply { addOval(Rect(Offset(x, 0f), n)) }
        val bottom = Path().apply { addOval(Rect(Offset(x, size.height), n)) }
        val cutTop = Path.combine(PathOperation.Difference, base, top)
        val result = Path.combine(PathOperation.Difference, cutTop, bottom)
        return Outline.Generic(result)
    }
}

// ======================================================================= Hati

@Composable
fun GradientHeart(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    beating: Boolean = true,
    light: Boolean = false
) {
    val beat = rememberHeartbeat()
    Canvas(
        modifier
            .size(size)
            .graphicsLayer {
                val b = if (beating) beat.value else 1f
                scaleX = b
                scaleY = b
            }
    ) {
        val s = this.size.minDimension / 2.3f
        val c = Offset(this.size.width / 2f, this.size.height / 2f - s * 0.1f)
        drawPath(
            heartPath(c.x, c.y, s),
            Brush.linearGradient(
                colors = if (light) listOf(Color.White, BlushDeep) else listOf(RoseLight, Rose, RoseDark),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )
        )
        drawOval(
            Color.White.copy(alpha = 0.45f),
            Offset(c.x - s * 0.95f, c.y - s * 0.62f),
            Size(s * 0.55f, s * 0.3f)
        )
    }
}

// ======================================================================= Chip & rating

@Composable
fun StatusChip(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(13.dp), tint = content)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun DateStatusChip(status: String, modifier: Modifier = Modifier) {
    val s = status.lowercase()
    when {
        "selesai" in s -> StatusChip("Selesai", WineSoft.copy(alpha = 0.12f), WineSoft, modifier, Icons.Rounded.Check)
        "akan" in s -> StatusChip("Akan datang", Blush, RoseDark, modifier, Icons.Rounded.Favorite)
        s.isNotBlank() -> StatusChip(s.replaceFirstChar { it.uppercase() }, Blush, WineSoft, modifier)
    }
}

@Composable
fun RundownStatusChip(status: String, modifier: Modifier = Modifier) {
    val s = status.lowercase()
    when {
        "sukses" in s -> StatusChip("Sukses", SageSoft, Sage, modifier, Icons.Rounded.Check)
        "peringatan" in s -> StatusChip("Peringatan", AmberSoft, Amber, modifier, Icons.Rounded.Warning)
        s.isNotBlank() -> StatusChip(s.replaceFirstChar { it.uppercase() }, Blush, WineSoft, modifier)
    }
}

/** Bintang dengan isi pecahan (4.6 = 4 bintang penuh + 60% bintang kelima). */
@Composable
fun RatingStars(rating: Double, modifier: Modifier = Modifier, starSize: Dp = 16.dp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            val fraction = (rating - i).coerceIn(0.0, 1.0).toFloat()
            Box(Modifier.size(starSize)) {
                Icon(Icons.Rounded.StarBorder, null, Modifier.size(starSize), tint = Gold)
                if (fraction > 0f) {
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        Modifier
                            .size(starSize)
                            .drawWithContent {
                                clipRect(right = this.size.width * fraction) {
                                    this@drawWithContent.drawContent()
                                }
                            },
                        tint = Gold
                    )
                }
            }
        }
    }
}

// ======================================================================= Tombol

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val brush: Brush = if (enabled) Brush.horizontalGradient(listOf(Rose, Color(0xFFEF7C9A)))
    else SolidColor(BlushDeep)
    Row(
        modifier
            .bounceClick(enabled = enabled, onClick = onClick)
            .height(52.dp)
            .shadow(
                if (enabled) 8.dp else 0.dp, CircleShape,
                ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(brush)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(20.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    tint: Color = Rose
) {
    val c = if (enabled) tint else BlushDeep
    Row(
        modifier
            .bounceClick(enabled = enabled, onClick = onClick)
            .height(52.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.8f))
            .border(1.5.dp, c, CircleShape)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(19.dp), tint = c)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = c, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

// ======================================================================= Header, tab, judul

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(44.dp)
                    .shadow(6.dp, CircleShape, ambientColor = Rose.copy(alpha = 0.2f), spotColor = Rose.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali", tint = Wine)
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Wine, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WineSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing()
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, count: Int? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Wine)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = RoseDark,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Blush)
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            )
        }
    }
}

data class TabSpec(val title: String, val badge: Int = 0)

/** Tab berbentuk pil dengan indikator yang bergeser membal. */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PillTabs(
    selected: Int,
    tabs: List<TabSpec>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(BlushDeep.copy(alpha = 0.35f))
            .padding(4.dp)
    ) {
        val tabW = maxWidth / tabs.size
        val offset by animateDpAsState(
            targetValue = tabW * selected,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
            label = "pillOffset"
        )
        Box(
            Modifier
                .offset(x = offset)
                .width(tabW)
                .height(44.dp)
                .shadow(4.dp, CircleShape, ambientColor = Rose.copy(alpha = 0.2f), spotColor = Rose.copy(alpha = 0.3f))
                .background(Color.White, CircleShape)
        )
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { i, tab ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tab.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected == i) RoseDark else WineSoft
                        )
                        if (tab.badge > 0) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${tab.badge}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Amber)
                                    .padding(horizontal = 7.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================= Loading, kosong, error

@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(12.dp)) {
    Box(modifier.clip(shape).shimmer())
}

/** Layar loading: hati berdetak + gelombang + kucing. Menahan sentuhan selama tampil. */
@Composable
fun LoveLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Lagi nyiapin buat ${Couple.PARTNER_A} & ${Couple.PARTNER_B}…"
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(250)), exit = fadeOut(tween(250))) {
        Box(
            modifier
                .fillMaxSize()
                .background(Wine.copy(alpha = 0.4f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .padding(32.dp)
                    .shadow(16.dp, RoundedCornerShape(30.dp), ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Ripples()
                    GradientHeart(size = 62.dp)
                }
                Spacer(Modifier.height(6.dp))
                CatMascot(size = 64.dp, moving = true)
                Spacer(Modifier.height(10.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.titleSmall,
                    color = Wine,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun Ripples() {
    val transition = rememberInfiniteTransition(label = "ripples")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "rippleT"
    )
    Canvas(Modifier.requiredSize(120.dp)) {
        val maxR = this.size.minDimension / 2f
        listOf(0f, 0.5f).forEach { off ->
            val q = (t + off) % 1f
            drawCircle(
                Rose.copy(alpha = (1f - q) * 0.45f),
                radius = maxR * (0.45f + 0.55f * q),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    sleepy: Boolean = false
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CatMascot(size = 120.dp, sleepy = sleepy, zoom = 1.5f)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Wine, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = WineSoft, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            GradientButton(actionLabel, onAction, icon = Icons.Rounded.Favorite)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CatMascot(size = 110.dp, sleepy = true)
        Spacer(Modifier.height(12.dp))
        Icon(Icons.Rounded.CloudOff, null, tint = WineSoft)
        Spacer(Modifier.height(6.dp))
        Text("Data belum bisa dimuat", style = MaterialTheme.typography.titleLarge, color = Wine, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = WineSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        GradientButton("Muat ulang", onRetry)
    }
}

@Composable
fun LoveConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        icon = { Icon(Icons.Rounded.Delete, null, tint = Rose) },
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Wine, textAlign = TextAlign.Center) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = WineSoft) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = RoseDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = WineSoft) }
        }
    )
}

@Composable
fun LoveSnackbarHost(state: SnackbarHostState) {
    SnackbarHost(state) { data ->
        Snackbar(
            snackbarData = data,
            shape = RoundedCornerShape(18.dp),
            containerColor = Wine,
            contentColor = Color.White,
            actionColor = RoseLight
        )
    }
}
