package com.lutfilia.rundown.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lutfilia.rundown.data.model.DateItem
import com.lutfilia.rundown.ui.components.DateStatusChip
import com.lutfilia.rundown.ui.components.EmptyState
import com.lutfilia.rundown.ui.components.ErrorState
import com.lutfilia.rundown.ui.components.GradientHeart
import com.lutfilia.rundown.ui.components.LoveConfirmDialog
import com.lutfilia.rundown.ui.components.LoveSnackbarHost
import com.lutfilia.rundown.ui.components.RundownStatusChip
import com.lutfilia.rundown.ui.components.SectionTitle
import com.lutfilia.rundown.ui.components.ShimmerBlock
import com.lutfilia.rundown.ui.components.ShimmerText
import com.lutfilia.rundown.ui.components.TicketShape
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.components.heartPath
import com.lutfilia.rundown.ui.components.popIn
import com.lutfilia.rundown.ui.components.pulse
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.RoseLight
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Couple
import com.lutfilia.rundown.util.dayNameIndo
import com.lutfilia.rundown.util.monthShortIndo
import com.lutfilia.rundown.util.toIndo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onAddDate: () -> Unit,
    onOpenDate: (DateItem) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var toDelete by remember { mutableStateOf<DateItem?>(null) }

    // Muat ulang tiap kali layar ini kembali tampil (mis. setelah tambah tanggal).
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) { viewModel.events.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { LoveSnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddDate,
                containerColor = Rose,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.pulse(0.04f, 1500)
            ) {
                Icon(Icons.Rounded.Favorite, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tanggal baru", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { inner ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            val dates = state.items
            val today = LocalDate.now()
            val upcoming = dates.orEmpty()
                .filter { !it.isFinished }
                .sortedBy { it.date ?: LocalDate.MAX }
            val finished = dates.orEmpty()
                .filter { it.isFinished }
                .sortedByDescending { it.date ?: LocalDate.MIN }
            val hero = upcoming.firstOrNull { it.date != null && !it.date.isBefore(today) }
            val rest = if (hero != null) upcoming.filter { it.id != hero.id } else upcoming

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "header") { HomeHeader() }

                when {
                    dates == null && state.error != null -> item(key = "error") {
                        ErrorState(state.error.orEmpty(), onRetry = { viewModel.refresh() })
                    }
                    dates == null -> items(3) { SkeletonTicket() }
                    dates.isEmpty() -> item(key = "empty") {
                        EmptyState(
                            title = "Belum ada rencana kencan",
                            subtitle = "Pilih satu tanggal dulu, rundown-nya kita susun setelah itu.",
                            actionLabel = "Pilih tanggal",
                            onAction = onAddDate
                        )
                    }
                    else -> {
                        if (hero != null) {
                            item(key = "hero") { HeroTicket(hero, today) { onOpenDate(hero) } }
                        }
                        if (rest.isNotEmpty()) {
                            item(key = "h-up") { SectionTitle("Rencana berikutnya", count = rest.size) }
                            itemsIndexed(rest, key = { _, d -> "up-${d.id}" }) { i, d ->
                                DateTicket(
                                    item = d,
                                    finished = false,
                                    onClick = { onOpenDate(d) },
                                    onDelete = { toDelete = d },
                                    modifier = Modifier
                                        .animateItem()
                                        .popIn(i)
                                )
                            }
                        }
                        if (finished.isNotEmpty()) {
                            item(key = "h-done") { SectionTitle("Sudah lewat", count = finished.size) }
                            itemsIndexed(finished, key = { _, d -> "done-${d.id}" }) { i, d ->
                                DateTicket(
                                    item = d,
                                    finished = true,
                                    onClick = { onOpenDate(d) },
                                    onDelete = { toDelete = d },
                                    modifier = Modifier
                                        .animateItem()
                                        .popIn(i)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { item ->
        LoveConfirmDialog(
            title = "Hapus tanggal ini?",
            message = "Tanggal ${item.date?.toIndo() ?: item.dateText} beserta seluruh rundown dan data gagalnya akan terhapus.",
            confirmText = "Hapus",
            onConfirm = {
                viewModel.delete(item)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun HomeHeader() {
    Column(Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerText(Couple.PARTNER_A, MaterialTheme.typography.displayMedium)
            GradientHeart(Modifier.padding(horizontal = 8.dp), size = 34.dp)
            ShimmerText(Couple.PARTNER_B, MaterialTheme.typography.displayMedium)
        }
        Text(
            "Semua rencana kita, rapi di satu tempat.",
            style = MaterialTheme.typography.bodyMedium,
            color = WineSoft
        )
    }
}

/** Tiket besar untuk kencan terdekat. */
@Composable
private fun HeroTicket(item: DateItem, today: LocalDate, onClick: () -> Unit) {
    val date = item.date
    val days = if (date != null) ChronoUnit.DAYS.between(today, date) else null
    val countdown = when {
        days == null -> "Segera"
        days <= 0L -> "Hari ini"
        days == 1L -> "Besok"
        else -> "$days hari lagi"
    }
    val shape = TicketShape(corner = 30.dp, notchRadius = 12.dp, notchX = 96.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .shadow(14.dp, shape, ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.45f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFE0557A), Color(0xFFC13E68), Color(0xFF8E2D54)),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 700f)
                )
            )
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawPath(heartPath(w * 0.86f, h * 0.30f, 26.dp.toPx()), Color.White.copy(alpha = 0.10f))
            drawPath(heartPath(w * 0.70f, h * 0.82f, 14.dp.toPx()), Color.White.copy(alpha = 0.10f))
            drawPath(heartPath(w * 0.95f, h * 0.78f, 9.dp.toPx()), Color.White.copy(alpha = 0.14f))
            // garis putus-putus pemisah tiket
            val x = 96.dp.toPx()
            var y = 20.dp.toPx()
            while (y < h - 20.dp.toPx()) {
                drawLine(
                    Color.White.copy(alpha = 0.5f),
                    Offset(x, y),
                    Offset(x, y + 6.dp.toPx()),
                    2.dp.toPx()
                )
                y += 13.dp.toPx()
            }
        }
        Row(Modifier.padding(vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.width(96.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    date?.dayOfMonth?.toString() ?: "-",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Text(
                    date?.monthShortIndo() ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Column(Modifier.weight(1f).padding(start = 18.dp, end = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kencan berikutnya", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                Text(
                    countdown,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                item.label?.let { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    date?.let { "${it.dayNameIndo()}, ${it.toIndo()}" } ?: item.dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Buka rundown", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

/** Tiket tanggal biasa: bagian kiri = tanggal, bagian kanan = status. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateTicket(
    item: DateItem,
    finished: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = TicketShape(corner = 22.dp, notchRadius = 9.dp, notchX = 76.dp)
    val stubBrush = if (finished) {
        Brush.verticalGradient(listOf(WineSoft.copy(alpha = 0.55f), WineSoft.copy(alpha = 0.4f)))
    } else {
        Brush.verticalGradient(listOf(RoseLight, Rose))
    }
    Row(
        modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .shadow(8.dp, shape, ambientColor = Rose.copy(alpha = 0.15f), spotColor = Rose.copy(alpha = 0.25f))
            .clip(shape)
            .background(Color.White.copy(alpha = if (finished) 0.72f else 0.95f))
            // bagian kiri (tanggal) diwarnai setinggi kartu, berapa pun tinggi isinya
            .drawBehind { drawRect(stubBrush, size = Size(76.dp.toPx(), size.height)) }
            .heightIn(min = 96.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // kiri: tanggal
        Column(
            Modifier.width(76.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                item.date?.dayOfMonth?.toString() ?: "–",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                item.date?.monthShortIndo() ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        // kanan: detail
        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Ada label: label jadi judul, tanggal lengkap di bawahnya. Tidak ada: tanggal jadi judul.
            val label = item.label
            Text(
                label ?: item.date?.toIndo() ?: item.dateText,
                style = MaterialTheme.typography.titleLarge,
                color = Wine,
                maxLines = if (label != null) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (label != null) {
                    item.date?.let { "${it.dayNameIndo()}, ${it.toIndo()}" } ?: item.dateText
                } else {
                    item.date?.dayNameIndo() ?: "Tanggal"
                },
                style = MaterialTheme.typography.labelMedium,
                color = WineSoft
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DateStatusChip(item.status)
                RundownStatusChip(item.statusRundown)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.DeleteOutline, "Hapus tanggal", tint = WineSoft.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun SkeletonTicket() {
    val shape = TicketShape(corner = 22.dp, notchRadius = 9.dp, notchX = 76.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.8f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBlock(Modifier.width(76.dp).height(92.dp), RoundedCornerShape(0.dp))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerBlock(Modifier.width(90.dp).height(12.dp))
            ShimmerBlock(Modifier.width(170.dp).height(18.dp))
            ShimmerBlock(Modifier.width(120.dp).height(14.dp), CircleShape)
        }
    }
}