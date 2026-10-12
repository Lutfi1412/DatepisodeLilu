package com.lutfilia.rundown.ui.screens.detail

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.data.model.RundownItem
import com.lutfilia.rundown.ui.components.CatMascot
import com.lutfilia.rundown.ui.components.GradientHeart
import com.lutfilia.rundown.ui.components.LoveCard
import com.lutfilia.rundown.ui.components.StatusChip
import com.lutfilia.rundown.ui.components.ShimmerBlock
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.components.heartPath
import com.lutfilia.rundown.ui.components.rememberHeartbeat
import com.lutfilia.rundown.ui.components.rememberNow
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Gold
import com.lutfilia.rundown.ui.theme.GoldSoft
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.RoseLight
import com.lutfilia.rundown.ui.theme.Sage
import com.lutfilia.rundown.ui.theme.SageSoft
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Phase
import com.lutfilia.rundown.util.hm
import com.lutfilia.rundown.util.phaseOf
import com.lutfilia.rundown.util.ratingText
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

// Ukuran peta
private val RowH = 164.dp
private val TopPad = 76.dp
private val BottomPad = 130.dp
private val CardH = 104.dp
private val NodeR = 27.dp
private val CatSize = 68.dp
private val SideGap = 12.dp
private val NodeGap = 10.dp

/** Satu ruas lengkung (bezier kubik) di antara dua titik di peta. */
private data class Seg(val p0: Offset, val c1: Offset, val c2: Offset, val p3: Offset) {
    fun at(t: Float): Offset {
        val u = 1f - t
        return Offset(
            u * u * u * p0.x + 3f * u * u * t * c1.x + 3f * u * t * t * c2.x + t * t * t * p3.x,
            u * u * u * p0.y + 3f * u * u * t * c1.y + 3f * u * t * t * c2.y + t * t * t * p3.y
        )
    }
}

/** index 0 = garis start, 1..n = acara, n+1 = garis finish. Acara zig-zag kiri-kanan. */
private fun buildPoints(n: Int, w: Float, topPad: Float, rowH: Float, bottomPad: Float): List<Offset> {
    val pts = ArrayList<Offset>(n + 2)
    pts += Offset(w * 0.26f, topPad * 0.45f)
    for (i in 0 until n) {
        pts += Offset(if (i % 2 == 0) w * 0.26f else w * 0.74f, topPad + rowH * (i + 0.5f))
    }
    pts += Offset(w * 0.5f, topPad + rowH * n + bottomPad * 0.42f)
    return pts
}

private fun buildSegs(pts: List<Offset>): List<Seg> = pts.zipWithNext { a, b ->
    val dy = (b.y - a.y) * 0.55f
    Seg(a, Offset(a.x, a.y + dy), Offset(b.x, b.y - dy), b)
}

// =====================================================================================
//  HALAMAN RUNDOWN: ringkasan + peta perjalanan
// =====================================================================================

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun JourneyBoard(
    items: List<RundownItem>,
    date: LocalDate?,
    selection: Set<Int>,
    onTap: (RundownItem) -> Unit,
    onLongPress: (RundownItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val nowState = rememberNow()
    // Status dihitung lokal dari jam HP. derivedStateOf: hanya recompose kalau status benar-benar berubah.
    val phases by remember(items, date) {
        derivedStateOf { items.map { phaseOf(it, date, nowState.value) } }
    }
    val scroll = rememberScrollState()

    BoxWithConstraints(modifier.fillMaxSize()) {
        val viewportPx = with(LocalDensity.current) { maxHeight.toPx() }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            JourneySummary(items, phases, Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            JourneyPath(items, phases, selection, onTap, onLongPress, scroll, viewportPx)
            Spacer(Modifier.height(90.dp))
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun JourneyPath(
    items: List<RundownItem>,
    phases: List<Phase>,
    selection: Set<Int>,
    onTap: (RundownItem) -> Unit,
    onLongPress: (RundownItem) -> Unit,
    scroll: ScrollState,
    viewportPx: Float
) {
    val n = items.size
    val density = LocalDensity.current
    var topPx by remember { mutableIntStateOf(0) }
    val totalH = TopPad + RowH * n + BottomPad

    // Posisi target kucing di "ruang titik": 0 = start, k = acara ke-k, n+1 = finish.
    val catTarget: Float = run {
        val running = phases.indexOf(Phase.RUNNING)
        when {
            running >= 0 -> (running + 1).toFloat()
            n > 0 && phases.all { it == Phase.DONE } -> (n + 1).toFloat()
            else -> phases.indexOf(Phase.UPCOMING).coerceAtLeast(0).toFloat()
        }
    }
    val allDone = n > 0 && phases.all { it == Phase.DONE }
    val catPos = remember { Animatable(0f) }
    var catMoving by remember { mutableStateOf(false) }
    LaunchedEffect(catTarget) {
        val dist = abs(catTarget - catPos.value)
        if (dist > 0.001f) {
            catMoving = true
            try {
                catPos.animateTo(
                    catTarget,
                    tween((900 + 650 * dist).toInt().coerceAtMost(5000), easing = FastOutSlowInEasing)
                )
            } finally {
                catMoving = false
            }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(totalH)
            .onGloballyPositioned { topPx = it.positionInParent().y.roundToInt() }
    ) {
        val wPx = with(density) { maxWidth.toPx() }
        val nodeRpx = with(density) { NodeR.toPx() }
        val pts = remember(n, wPx) {
            with(density) { buildPoints(n, wPx, TopPad.toPx(), RowH.toPx(), BottomPad.toPx()) }
        }
        val segs = remember(pts) { buildSegs(pts) }
        val fullPath = remember(segs) {
            Path().apply {
                segs.forEachIndexed { i, s ->
                    if (i == 0) moveTo(s.p0.x, s.p0.y)
                    cubicTo(s.c1.x, s.c1.y, s.c2.x, s.c2.y, s.p3.x, s.p3.y)
                }
            }
        }
        val cardWpx = wPx * 0.74f - nodeRpx - with(density) { (NodeGap + SideGap).toPx() }
        val cardWdp = with(density) { cardWpx.toDp() }

        // Auto-scroll: ikuti posisi kucing.
        LaunchedEffect(catTarget, topPx, viewportPx, pts) {
            val idx = catTarget.toInt().coerceIn(0, pts.lastIndex)
            val y = topPx + pts[idx].y
            delay(450)
            scroll.animateScrollTo((y - viewportPx * 0.4f).roundToInt().coerceAtLeast(0))
        }

        // ---------- jalur (putus-putus = belum dilalui, penuh = sudah dilalui) ----------
        Canvas(Modifier.fillMaxSize()) {
            val dash = PathEffect.dashPathEffect(floatArrayOf(14.dp.toPx(), 12.dp.toPx()))
            drawPath(
                fullPath, BlushDeep,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dash)
            )

            val c = catPos.value
            val traveled = Path()
            var started = false
            val fullCount = floor(c).toInt().coerceIn(0, segs.size)
            for (i in 0 until fullCount) {
                val s = segs[i]
                if (!started) {
                    traveled.moveTo(s.p0.x, s.p0.y); started = true
                }
                traveled.cubicTo(s.c1.x, s.c1.y, s.c2.x, s.c2.y, s.p3.x, s.p3.y)
            }
            if (fullCount < segs.size) {
                val frac = c - fullCount
                if (frac > 0.001f) {
                    val s = segs[fullCount]
                    if (!started) {
                        traveled.moveTo(s.p0.x, s.p0.y); started = true
                    }
                    val steps = 20
                    for (j in 1..steps) {
                        val p = s.at(frac * j / steps)
                        traveled.lineTo(p.x, p.y)
                    }
                }
            }
            if (started) {
                drawPath(
                    traveled,
                    Brush.verticalGradient(listOf(RoseLight, Rose, RoseDark), startY = 0f, endY = size.height),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // hati kecil di tengah tiap ruas
            segs.forEachIndexed { i, s ->
                val mid = s.at(0.5f)
                val passed = c > i + 0.5f
                drawPath(
                    heartPath(mid.x, mid.y, 5.dp.toPx()),
                    if (passed) Rose else BlushDeep
                )
            }
        }

        // ---------- penanda mulai & selesai ----------
        val start = pts.first()
        Row(
            Modifier
                .offset { IntOffset((start.x - 46.dp.toPx()).roundToInt(), (start.y - 16.dp.toPx()).roundToInt()) }
                .width(92.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BlushDeep, CircleShape),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Flag, null, Modifier.size(16.dp), tint = Rose)
            Spacer(Modifier.width(6.dp))
            Text("Mulai", style = MaterialTheme.typography.labelLarge, color = Wine)
        }

        val finish = pts.last()
        Column(
            Modifier
                .offset { IntOffset((finish.x - 90.dp.toPx()).roundToInt(), (finish.y - 30.dp.toPx()).roundToInt()) }
                .width(180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (allDone) GradientHeart(size = 52.dp)
            else Icon(Icons.Rounded.Favorite, null, Modifier.size(34.dp), tint = BlushDeep)
            Spacer(Modifier.height(4.dp))
            Text(
                if (allDone) "Kencan selesai" else "Sampai ketemu di sini",
                style = MaterialTheme.typography.titleSmall,
                color = if (allDone) RoseDark else WineSoft,
                textAlign = TextAlign.Center
            )
        }

        // ---------- node + kartu ----------
        items.forEachIndexed { i, item ->
            val p = pts[i + 1]
            val phase = phases.getOrElse(i) { Phase.UPCOMING }
            val selected = item.id in selection
            val nodeOnLeft = i % 2 == 0

            JourneyNode(
                index = i + 1,
                phase = phase,
                selected = selected,
                onClick = { onTap(item) },
                onLongClick = { onLongPress(item) },
                modifier = Modifier.offset {
                    IntOffset((p.x - nodeRpx).roundToInt(), (p.y - nodeRpx).roundToInt())
                }
            )
            val cardX = if (nodeOnLeft) p.x + nodeRpx + with(density) { NodeGap.toPx() }
            else with(density) { SideGap.toPx() }
            JourneyCard(
                item = item,
                phase = phase,
                selected = selected,
                onClick = { onTap(item) },
                onLongClick = { onLongPress(item) },
                modifier = Modifier
                    .offset {
                        IntOffset(cardX.roundToInt(), (p.y - CardH.toPx() / 2f).roundToInt())
                    }
                    .width(cardWdp)
                    .height(CardH)
            )
        }

        // ---------- kucing (paling atas) ----------
        JourneyCat(
            catPos = catPos,
            segs = segs,
            moving = catMoving,
            target = catTarget,
            happy = allDone,
            sleepy = !catMoving && phases.none { it == Phase.RUNNING } && !allDone
        )
    }
}

/** Dipisah supaya hanya komposabel kecil ini yang digambar ulang tiap frame saat kucing berjalan. */
@Composable
private fun JourneyCat(
    catPos: Animatable<Float, AnimationVector1D>,
    segs: List<Seg>,
    moving: Boolean,
    target: Float,
    happy: Boolean,
    sleepy: Boolean
) {
    val pos = catPos.value
    val k = floor(pos).toInt().coerceIn(0, segs.lastIndex)
    val dx = segs[k].p3.x - segs[k].p0.x
    val forward = target >= pos
    val facingLeft = if (forward) dx < 0f else dx > 0f
    val density = LocalDensity.current
    val sizePx = with(density) { CatSize.toPx() }
    val nodeR = with(density) { NodeR.toPx() }

    Box(
        Modifier.offset {
            val seg = segs[k]
            val p = seg.at((catPos.value - k).coerceIn(0f, 1f))
            IntOffset((p.x - sizePx / 2f).roundToInt(), (p.y - nodeR - sizePx * 0.9f).roundToInt())
        }
    ) {
        CatMascot(
            size = CatSize,
            moving = moving,
            facingLeft = facingLeft,
            sleepy = sleepy && !happy && !moving,
            zoom = 2f
        )
    }
}

// =====================================================================================
//  Node & kartu
// =====================================================================================

@Composable
private fun JourneyNode(
    index: Int,
    phase: Phase,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(NodeR * 2)
            .bounceClick(onLongClick = onLongClick, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            Phase.RUNNING -> {
                RunningRing()
                val beat by rememberHeartbeat()
                Box(
                    Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape, ambientColor = Gold.copy(alpha = 0.5f), spotColor = Gold.copy(alpha = 0.6f))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Gold, Rose))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Favorite, null,
                        Modifier
                            .size(26.dp)
                            .graphicsLayer { scaleX = beat; scaleY = beat },
                        tint = Color.White
                    )
                }
            }
            Phase.DONE -> Box(
                Modifier
                    .size(50.dp)
                    .shadow(6.dp, CircleShape, ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RoseLight, Rose))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, null, Modifier.size(26.dp), tint = Color.White)
            }
            Phase.UPCOMING -> Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.5.dp, BlushDeep, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$index", style = MaterialTheme.typography.titleMedium, color = WineSoft)
            }
        }
        if (selected) {
            Icon(
                Icons.Rounded.CheckCircle, null,
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(6.dp, (-6).dp)
                    .size(24.dp)
                    .background(Color.White, CircleShape),
                tint = Rose
            )
        }
    }
}

@Composable
private fun RunningRing() {
    val transition = rememberInfiniteTransition(label = "ring")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart),
        label = "ringP"
    )
    Canvas(Modifier.requiredSize(124.dp)) {
        val maxR = this.size.minDimension / 2f
        val baseR = NodeR.toPx()
        listOf(0f, 0.5f).forEach { off ->
            val q = (p + off) % 1f
            drawCircle(
                Gold.copy(alpha = (1f - q) * 0.55f),
                radius = baseR + (maxR - baseR) * q,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JourneyCard(
    item: RundownItem,
    phase: Phase,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = when (phase) {
        Phase.DONE -> Color.White.copy(alpha = 0.72f)
        Phase.RUNNING -> Color.White
        Phase.UPCOMING -> Color.White.copy(alpha = 0.92f)
    }
    LoveCard(
        modifier.bounceClick(onLongClick = onLongClick, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        selected = selected,
        container = container,
        border = if (phase == Phase.RUNNING) Gold else null
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                item.nama,
                style = MaterialTheme.typography.titleSmall,
                color = if (phase == Phase.DONE) WineSoft else Wine,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, Modifier.size(14.dp), tint = WineSoft)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${item.start.hm()} – ${item.end.hm()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = WineSoft
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                when (phase) {
                    Phase.DONE -> StatusChip("Selesai", SageSoft, Sage, icon = Icons.Rounded.Check)
                    Phase.RUNNING -> StatusChip("Berlangsung · ${remainingText(item.end)}", GoldSoft, Amber)
                    Phase.UPCOMING -> StatusChip("Akan datang", Blush, RoseDark)
                }
                val r = item.rating
                if (r != null && r > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, Modifier.size(14.dp), tint = Gold)
                        Text(r.ratingText(), style = MaterialTheme.typography.labelMedium, color = WineSoft)
                    }
                }
            }
        }
    }
}

@Composable
private fun remainingText(end: LocalTime): String {
    val now by rememberNow()
    val mins = Duration.between(now.toLocalTime(), end).toMinutes()
    return when {
        mins <= 0L -> "sebentar lagi"
        mins < 60L -> "sisa $mins mnt"
        else -> "sisa ${mins / 60}j ${mins % 60}m"
    }
}

// =====================================================================================
//  Ringkasan di atas peta
// =====================================================================================

@Composable
private fun JourneySummary(items: List<RundownItem>, phases: List<Phase>, modifier: Modifier = Modifier) {
    val total = items.size
    val done = phases.count { it == Phase.DONE }
    val runningIdx = phases.indexOf(Phase.RUNNING)
    val nextIdx = phases.indexOf(Phase.UPCOMING)
    val progress = if (total == 0) 0f else (done + if (runningIdx >= 0) 0.5f else 0f) / total

    val (label, title) = when {
        runningIdx >= 0 -> "Sedang berlangsung" to items[runningIdx].nama
        nextIdx >= 0 ->
            (if (done == 0) "Dimulai pukul ${items[nextIdx].start.hm()}" else "Berikutnya pukul ${items[nextIdx].start.hm()}") to
                items[nextIdx].nama
        else -> "Semua acara selesai" to "Terima kasih sudah jalan bareng"
    }

    LoveCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = Rose)
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Wine,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                LiveClock()
            }
            LoveProgressBar(progress)
            Text(
                "$done dari $total acara selesai",
                style = MaterialTheme.typography.bodySmall,
                color = WineSoft
            )
        }
    }
}

@Composable
private fun LiveClock() {
    val now by rememberNow()
    Text(
        now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
        style = MaterialTheme.typography.titleMedium,
        color = RoseDark,
        fontWeight = FontWeight.Bold
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LoveProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress, tween(900, easing = FastOutSlowInEasing), label = "progress")
    BoxWithConstraints(modifier.fillMaxWidth().height(18.dp)) {
        val w = maxWidth
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Blush)
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .width(w * animated)
                .height(8.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(RoseLight, Rose)))
        )
        Icon(
            Icons.Rounded.Favorite, null,
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = (w * animated - 9.dp).coerceIn(0.dp, w - 18.dp))
                .size(18.dp),
            tint = RoseDark
        )
    }
}

/** Kerangka loading untuk halaman rundown. */
@Composable
fun BoardSkeleton() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ShimmerBlock(Modifier.fillMaxWidth().height(110.dp), RoundedCornerShape(22.dp))
        repeat(3) {
            ShimmerBlock(Modifier.fillMaxWidth(0.8f).height(100.dp), RoundedCornerShape(20.dp))
        }
    }
}
