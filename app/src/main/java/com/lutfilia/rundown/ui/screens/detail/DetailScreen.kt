package com.lutfilia.rundown.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lutfilia.rundown.data.model.DateDetail
import com.lutfilia.rundown.data.model.ErrorItem
import com.lutfilia.rundown.data.model.RundownItem
import com.lutfilia.rundown.data.model.toDraft
import com.lutfilia.rundown.data.model.toDraftOrNull
import com.lutfilia.rundown.ui.components.DateStatusChip
import com.lutfilia.rundown.ui.components.EmptyState
import com.lutfilia.rundown.ui.components.ErrorState
import com.lutfilia.rundown.ui.components.GradientButton
import com.lutfilia.rundown.ui.components.LoveConfirmDialog
import com.lutfilia.rundown.ui.components.LoveLoadingOverlay
import com.lutfilia.rundown.ui.components.LoveSnackbarHost
import com.lutfilia.rundown.ui.components.PillTabs
import com.lutfilia.rundown.ui.components.RatingStars
import com.lutfilia.rundown.ui.components.RundownStatusChip
import com.lutfilia.rundown.ui.components.ScreenHeader
import com.lutfilia.rundown.ui.components.ShimmerBlock
import com.lutfilia.rundown.ui.components.SoftButton
import com.lutfilia.rundown.ui.components.StatusChip
import com.lutfilia.rundown.ui.components.TabSpec
import com.lutfilia.rundown.ui.components.pulse
import com.lutfilia.rundown.ui.components.rememberNow
import com.lutfilia.rundown.ui.screens.rundownform.RundownEditSheet
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.AmberSoft
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.GoldSoft
import com.lutfilia.rundown.ui.theme.Petal
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Sage
import com.lutfilia.rundown.ui.theme.SageSoft
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Phase
import com.lutfilia.rundown.util.hm
import com.lutfilia.rundown.util.openLink
import com.lutfilia.rundown.util.phaseOf
import com.lutfilia.rundown.util.toIndoLong
import kotlinx.coroutines.launch
import java.time.LocalDate

private sealed interface EditTarget {
    data class Rundown(val item: RundownItem) : EditTarget
    data class Retry(val item: ErrorItem) : EditTarget
}

private sealed interface PendingDelete {
    data class Rundowns(val ids: List<Int>) : PendingDelete
    data class Errors(val ids: List<Int>) : PendingDelete
}

private fun Set<Int>.toggle(id: Int): Set<Int> = if (id in this) this - id else this + id

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onAddRundown: (tanggalId: Int, dateIso: String) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    val page = pagerState.currentPage

    var rundownSel by remember { mutableStateOf(emptySet<Int>()) }
    var errorSel by remember { mutableStateOf(emptySet<Int>()) }
    var viewing by remember { mutableStateOf<RundownItem?>(null) }
    var editing by remember { mutableStateOf<EditTarget?>(null) }
    var pending by remember { mutableStateOf<PendingDelete?>(null) }
    var showDoc by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.events.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(page) {
        rundownSel = emptySet()
        errorSel = emptySet()
    }

    val detail = state.detail
    val errors = state.errors
    val selection = if (page == 0) rundownSel else errorSel
    val selecting = selection.isNotEmpty()
    val pageTotal = if (page == 0) detail?.items?.size ?: 0 else errors?.items?.size ?: 0
    val warningCount = errors?.items?.size ?: 0

    BackHandler(enabled = selecting) {
        rundownSel = emptySet()
        errorSel = emptySet()
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { LoveSnackbarHost(snackbar) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = page == 0 && !selecting && detail != null,
                    enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { onAddRundown(viewModel.tanggalId, detail?.date?.toString().orEmpty()) },
                        containerColor = Rose,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.pulse(0.04f, 1500)
                    ) {
                        Icon(Icons.Rounded.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rundown baru", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        ) { inner ->
            Column(Modifier.padding(inner).fillMaxSize()) {
                Crossfade(targetState = selecting, label = "headerSwap") { sel ->
                    if (sel) {
                        SelectionBar(
                            count = selection.size,
                            allSelected = selection.size == pageTotal,
                            onClose = {
                                rundownSel = emptySet()
                                errorSel = emptySet()
                            },
                            onToggleAll = {
                                val all = if (page == 0) detail?.items?.map { it.id }.orEmpty().toSet()
                                else errors?.items?.map { it.id }.orEmpty().toSet()
                                val next = if (selection.size == all.size) emptySet() else all
                                if (page == 0) rundownSel = next else errorSel = next
                            },
                            onDelete = {
                                pending = if (page == 0) PendingDelete.Rundowns(selection.toList())
                                else PendingDelete.Errors(selection.toList())
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    } else {
                        DetailHeader(
                            detail = detail,
                            loading = state.loading,
                            warnings = warningCount,
                            docLink = state.dokumentasi,
                            onDocClick = { showDoc = true },
                            onBack = onBack,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                PillTabs(
                    selected = page,
                    tabs = listOf(
                        TabSpec("Rundown"),
                        TabSpec("Error", warningCount)
                    ),
                    onSelect = { scope.launch { pagerState.animateScrollToPage(it) } },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = !selecting,
                        modifier = Modifier.fillMaxSize()
                    ) { p ->
                        if (p == 0) {
                            RundownPage(
                                loading = state.loading,
                                failure = state.failure,
                                detail = detail,
                                selection = rundownSel,
                                onRetry = { viewModel.refresh() },
                                onTap = { item ->
                                    if (rundownSel.isNotEmpty()) rundownSel = rundownSel.toggle(item.id)
                                    else viewing = item
                                },
                                onLongPress = { item -> rundownSel = rundownSel.toggle(item.id) },
                                onAdd = { onAddRundown(viewModel.tanggalId, detail?.date?.toString().orEmpty()) }
                            )
                        } else {
                            ErrorTab(
                                errors = errors,
                                selection = errorSel,
                                onTap = { e -> if (errorSel.isNotEmpty()) errorSel = errorSel.toggle(e.id) },
                                onLongPress = { e -> errorSel = errorSel.toggle(e.id) },
                                onResend = { e ->
                                    val draft = e.toDraftOrNull()
                                    if (draft != null) viewModel.retry(e.id, draft)
                                    else editing = EditTarget.Retry(e) // jam tidak terbaca: minta dilengkapi
                                },
                                onFix = { e -> editing = EditTarget.Retry(e) },
                                onDelete = { e -> pending = PendingDelete.Errors(listOf(e.id)) },
                                onOpenMap = { context.openLink(it) }
                            )
                        }
                    }
                }
            }
        }

        LoveLoadingOverlay(visible = state.busy, message = "Mengirim ke server…")
    }

    // ---- sheet info rundown
    viewing?.let { item ->
        RundownInfoSheet(
            item = item,
            date = detail?.date,
            onDismiss = { viewing = null },
            onOpenMap = { context.openLink(it) },
            onEdit = {
                editing = EditTarget.Rundown(item)
                viewing = null
            },
            onDelete = {
                pending = PendingDelete.Rundowns(listOf(item.id))
                viewing = null
            }
        )
    }

    // ---- sheet ubah / perbaiki (nilai lama sudah terisi)
    editing?.let { target ->
        when (target) {
            is EditTarget.Rundown -> RundownEditSheet(
                title = "Ubah rundown",
                subtitle = "Data lama sudah terisi, ubah yang perlu saja.",
                confirmLabel = "Simpan perubahan",
                initial = target.item.toDraft(),
                onDismiss = { editing = null },
                onConfirm = { draft ->
                    viewModel.update(target.item.id, draft)
                    editing = null
                }
            )
            is EditTarget.Retry -> RundownEditSheet(
                title = "Perbaiki lalu kirim ulang",
                subtitle = target.item.detail.ifBlank { "Data sebelumnya sudah terisi." },
                confirmLabel = "Kirim ulang",
                initial = target.item.toDraftOrNull() ?: com.lutfilia.rundown.data.model.RundownDraft(
                    nama = target.item.nama,
                    start = target.item.start ?: java.time.LocalTime.of(8, 0),
                    end = target.item.end ?: java.time.LocalTime.of(9, 0),
                    link = target.item.link,
                    rating = target.item.rating
                ),
                onDismiss = { editing = null },
                onConfirm = { draft ->
                    viewModel.retry(target.item.id, draft)
                    editing = null
                }
            )
        }
    }

    // ---- sheet dokumentasi
    if (showDoc) {
        DocumentationSheet(
            currentLink = state.dokumentasi.orEmpty(),
            saving = state.savingDoc,
            onDismiss = { showDoc = false },
            onSave = { viewModel.saveDokumentasi(it) },
            onOpen = { context.openLink(it) }
        )
    }

    // ---- konfirmasi hapus
    pending?.let { p ->
        val ids = when (p) {
            is PendingDelete.Rundowns -> p.ids
            is PendingDelete.Errors -> p.ids
        }
        LoveConfirmDialog(
            title = if (ids.size == 1) "Hapus yang ini?" else "Hapus ${ids.size} item?",
            message = when (p) {
                is PendingDelete.Rundowns -> "Rundown yang dipilih akan dihapus dari tanggal ini."
                is PendingDelete.Errors -> "Data gagal yang dipilih akan dihapus dan tidak bisa dikirim ulang."
            },
            confirmText = "Hapus",
            onConfirm = {
                when (p) {
                    is PendingDelete.Rundowns -> viewModel.deleteRundowns(p.ids)
                    is PendingDelete.Errors -> viewModel.deleteErrors(p.ids)
                }
                rundownSel = emptySet()
                errorSel = emptySet()
                pending = null
            },
            onDismiss = { pending = null }
        )
    }
}

// ---------------------------------------------------------------------------------- halaman

@Composable
private fun RundownPage(
    loading: Boolean,
    failure: String?,
    detail: DateDetail?,
    selection: Set<Int>,
    onRetry: () -> Unit,
    onTap: (RundownItem) -> Unit,
    onLongPress: (RundownItem) -> Unit,
    onAdd: () -> Unit
) {
    if (detail == null) {
        if (failure != null) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                ErrorState(failure, onRetry)
            }
        } else {
            BoardSkeleton()
        }
    } else if (detail.items.isEmpty()) {
        if (loading) {
            BoardSkeleton()
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                EmptyState(
                    title = "Belum ada rundown",
                    subtitle = "Tambahkan acara pertama untuk tanggal ini. Kalau baru saja disimpan, tarik layar ke bawah sebentar lagi.",
                    actionLabel = "Susun rundown",
                    onAction = onAdd,
                    sleepy = true
                )
            }
        }
    } else {
        JourneyBoard(
            items = detail.items,
            date = detail.date,
            selection = selection,
            onTap = onTap,
            onLongPress = onLongPress
        )
    }
}

// ---------------------------------------------------------------------------------- header

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailHeader(
    detail: DateDetail?,
    loading: Boolean,
    warnings: Int,
    docLink: String?,
    onDocClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (detail == null && loading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBlock(Modifier.size(44.dp), CircleShape)
                Spacer(Modifier.width(14.dp))
                ShimmerBlock(Modifier.width(200.dp).height(24.dp))
            }
        } else {
            // Ada label: label jadi judul, tanggal lengkap di bawahnya. Tidak ada: tanggal jadi judul.
            val label = detail?.label
            val dateLong = detail?.date?.toIndoLong() ?: detail?.dateText.orEmpty()
            ScreenHeader(
                title = label ?: dateLong.ifBlank { "Detail tanggal" },
                subtitle = if (label != null) dateLong.takeIf { it.isNotBlank() } else null,
                onBack = onBack
            )
            if (detail != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 58.dp)
                ) {
                    DateStatusChip(detail.status)
//                    RundownStatusChip(detail.statusRundown)
                    val w = maxOf(detail.totalWarning, warnings)
                    if (w > 0) StatusChip("$w peringatan", AmberSoft, Amber)
//                    , icon = Icons.Rounded.Warning
                    // dokumentasi: berdenyut pelan kalau acara sudah selesai tapi link masih kosong
                    DocumentationChip(
                        hasLink = !docLink.isNullOrBlank(),
                        remind = docLink != null && docLink.isBlank() && detail.status.contains("selesai"),
                        onClick = onDocClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBar(
    count: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleAll: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color.White)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Batal pilih", tint = Wine) }
        Text("$count dipilih", style = MaterialTheme.typography.titleMedium, color = Wine, modifier = Modifier.weight(1f))
        TextButton(onClick = onToggleAll) {
            Icon(Icons.Rounded.SelectAll, null, Modifier.size(18.dp), tint = RoseDark)
            Spacer(Modifier.width(6.dp))
            Text(if (allSelected) "Batal semua" else "Pilih semua", color = RoseDark)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, "Hapus terpilih", tint = Rose) }
    }
}

// ---------------------------------------------------------------------------------- sheet info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RundownInfoSheet(
    item: RundownItem,
    date: LocalDate?,
    onDismiss: () -> Unit,
    onOpenMap: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now by rememberNow()
    val phase = phaseOf(item, date, now)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Petal,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (phase) {
                Phase.DONE -> StatusChip("Selesai", SageSoft, Sage)
                Phase.RUNNING -> StatusChip("Sedang berlangsung", GoldSoft, Amber)
                Phase.UPCOMING -> StatusChip("Akan datang", Blush, RoseDark)
            }
            Text(item.nama, style = MaterialTheme.typography.headlineMedium, color = Wine)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, Modifier.size(20.dp), tint = Rose)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${item.start.hm()} – ${item.end.hm()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Wine
                )
            }
            val rating = item.rating
            if (rating != null && rating > 0.0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating)
                    Spacer(Modifier.width(8.dp))
                    Text(String.format(java.util.Locale.US, "%.1f", rating), style = MaterialTheme.typography.titleSmall, color = WineSoft)
                }
            } else {
                Text("Belum ada rating", style = MaterialTheme.typography.bodySmall, color = WineSoft)
            }
            if (item.link.isNotBlank()) {
                SoftButton(
                    text = "Buka di Google Maps",
                    onClick = { onOpenMap(item.link) },
                    icon = Icons.Rounded.Map,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GradientButton("Ubah", onEdit, Modifier.weight(1f), icon = Icons.Rounded.Edit)
                SoftButton("Hapus", onDelete, Modifier.weight(1f), icon = Icons.Rounded.Delete)
            }
        }
    }
}