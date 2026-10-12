package com.lutfilia.rundown.ui.screens.rundownform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lutfilia.rundown.data.model.RundownDraft
import com.lutfilia.rundown.ui.components.GradientButton
import com.lutfilia.rundown.ui.components.LoveCard
import com.lutfilia.rundown.ui.components.LoveLoadingOverlay
import com.lutfilia.rundown.ui.components.LoveSnackbarHost
import com.lutfilia.rundown.ui.components.ScreenHeader
import com.lutfilia.rundown.ui.components.SoftButton
import com.lutfilia.rundown.ui.components.popIn
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.hm
import com.lutfilia.rundown.util.toIndoLong

@Composable
fun CreateRundownScreen(
    onBack: () -> Unit,
    onFinished: (tanggalId: Int) -> Unit,
    viewModel: CreateRundownViewModel = viewModel()
) {
    val staged by viewModel.staged.collectAsStateWithLifecycle()
    val sending by viewModel.sending.collectAsStateWithLifecycle()
    val form = remember { RundownFormState() }
    val snackbar = remember { SnackbarHostState() }
    val focus: FocusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                CreateEvent.Done -> onFinished(viewModel.tanggalId)
                is CreateEvent.Error -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    val canAdd = form.isValid
    val canSave = form.isValid || (staged.isNotEmpty() && form.isBlank)

    Box(Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent, snackbarHost = { LoveSnackbarHost(snackbar) }) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .imePadding()
            ) {
                ScreenHeader(
                    title = "Susun rundown",
                    subtitle = viewModel.date?.toIndoLong() ?: "Acara kencan kita",
                    onBack = onBack,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Isi satu acara, lalu pilih: tambah lagi atau simpan semuanya sekaligus.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WineSoft
                    )

                    staged.forEachIndexed { i, d ->
                        key(i, d) {
                            StagedCard(index = i + 1, draft = d, onRemove = { viewModel.unstage(i) }, modifier = Modifier.popIn())
                        }
                    }

                    LoveCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Acara ke-${staged.size + 1}", style = MaterialTheme.typography.titleLarge, color = Wine)
                            RundownFormFields(form)
                        }
                    }

                    if (!form.isBlank && !form.isValid) {
                        Text(
                            "Lengkapi nama acara dan kedua jamnya dulu ya.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WineSoft
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        SoftButton(
                            text = "Tambah lagi",
                            onClick = {
                                form.toDraft()?.let { d ->
                                    focus.clearFocus()
                                    viewModel.stage(d)
                                    form.clear(nextStart = d.end)
                                }
                            },
                            enabled = canAdd,
                            icon = Icons.Rounded.Add,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "Simpan",
                            onClick = {
                                focus.clearFocus()
                                viewModel.submit(form.toDraft())
                            },
                            enabled = canSave,
                            icon = Icons.AutoMirrored.Rounded.Send,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
        LoveLoadingOverlay(visible = sending)
    }
}

@Composable
private fun StagedCard(index: Int, draft: RundownDraft, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val invalid = !draft.end.isAfter(draft.start)
    LoveCard(modifier.fillMaxWidth(), container = Color.White.copy(alpha = 0.85f)) {
        Row(Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Blush),
                contentAlignment = Alignment.Center
            ) {
                Text("$index", style = MaterialTheme.typography.labelLarge, color = RoseDark)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    draft.nama,
                    style = MaterialTheme.typography.titleSmall,
                    color = Wine,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${draft.start.hm()} – ${draft.end.hm()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (invalid) Amber else WineSoft
                    )
                    if (invalid) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.Warning, null, Modifier.size(14.dp), tint = Amber)
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, "Hapus dari daftar", tint = WineSoft)
            }
        }
    }
}
