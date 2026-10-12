package com.lutfilia.rundown.ui.screens.detail

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.data.model.ErrorDetail
import com.lutfilia.rundown.data.model.ErrorItem
import com.lutfilia.rundown.ui.components.EmptyState
import com.lutfilia.rundown.ui.components.LoveCard
import com.lutfilia.rundown.ui.components.ShimmerBlock
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.components.popIn
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.AmberSoft
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.Gold
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.hm
import com.lutfilia.rundown.util.ratingText

/**
 * Tab "Gagal": rundown yang ditolak server (mis. jam selesai < jam mulai).
 * Di sini status TIDAK dibedakan selesai/berjalan seperti di tab rundown.
 */
@Composable
fun ErrorTab(
    errors: ErrorDetail?,
    selection: Set<Int>,
    onTap: (ErrorItem) -> Unit,
    onLongPress: (ErrorItem) -> Unit,
    onResend: (ErrorItem) -> Unit,
    onFix: (ErrorItem) -> Unit,
    onDelete: (ErrorItem) -> Unit,
    onOpenMap: (String) -> Unit
) {
    when {
        errors == null -> Column(
            Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(2) { ShimmerBlock(Modifier.fillMaxWidth().height(150.dp), RoundedCornerShape(22.dp)) }
        }
        errors.items.isEmpty() -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EmptyState(
                title = "Semua aman",
                subtitle = "Tidak ada rundown yang error saat dikirim di tanggal ini."
            )
        }
        else -> LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(errors.items, key = { _, e -> e.id }) { i, e ->
                ErrorCard(
                    item = e,
                    selected = e.id in selection,
                    selecting = selection.isNotEmpty(),
                    onTap = { onTap(e) },
                    onLongPress = { onLongPress(e) },
                    onResend = { onResend(e) },
                    onFix = { onFix(e) },
                    onDelete = { onDelete(e) },
                    onOpenMap = onOpenMap,
                    modifier = Modifier.animateItem().popIn(i)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ErrorCard(
    item: ErrorItem,
    selected: Boolean,
    selecting: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onResend: () -> Unit,
    onFix: () -> Unit,
    onDelete: () -> Unit,
    onOpenMap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LoveCard(
        modifier
            .fillMaxWidth()
            .bounceClick(onLongClick = onLongPress, onClick = onTap),
        selected = selected
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmberSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Warning, null, tint = Amber)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.nama.ifBlank { "(tanpa nama)" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Wine,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${item.start?.hm() ?: "--:--"} – ${item.end?.hm() ?: "--:--"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WineSoft
                    )
                }
                if (selected) Icon(Icons.Rounded.CheckCircle, null, tint = Rose)
            }

            // alasan penolakan dari server
            Text(
                item.detail.ifBlank { "Data ditolak server." },
                style = MaterialTheme.typography.bodySmall,
                color = Amber,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmberSoft)
                    .padding(12.dp)
            )

            val rating = item.rating
            if (item.link.isNotBlank() || (rating != null && rating > 0.0)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (item.link.isNotBlank()) {
                        Row(
                            Modifier
                                .clip(CircleShape)
                                .bounceClick { onOpenMap(item.link) }
                                .background(Blush)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Map, null, Modifier.size(14.dp), tint = RoseDark)
                            Spacer(Modifier.width(4.dp))
                            Text("Lihat lokasi", style = MaterialTheme.typography.labelMedium, color = RoseDark)
                        }
                    }
                    if (rating != null && rating > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, Modifier.size(14.dp), tint = Gold)
                            Text(rating.ratingText(), style = MaterialTheme.typography.labelMedium, color = WineSoft)
                        }
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionPill("Kirim ulang", Icons.Rounded.Replay, filled = true, onClick = onResend)
                ActionPill("Perbaiki", Icons.Rounded.Edit, filled = false, onClick = onFix)
                if (!selecting) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, "Hapus", tint = WineSoft)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .bounceClick(onClick = onClick)
            .height(36.dp)
            .clip(CircleShape)
            .background(if (filled) Rose else Blush)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = if (filled) Color.White else RoseDark)
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (filled) Color.White else RoseDark
        )
    }
}
