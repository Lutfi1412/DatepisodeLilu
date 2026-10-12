package com.lutfilia.rundown.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lutfilia.rundown.ui.components.GradientButton
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.components.pulse
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Petal
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft

/**
 * Chip kecil di header detail tanggal.
 * - belum ada link : outline putih "Tambah dokumentasi"
 * - sudah ada link : pink penuh "Dokumentasi"
 * - remind = true  : berdenyut pelan (acara sudah selesai tapi dokumentasi masih kosong)
 */
@Composable
fun DocumentationChip(
    hasLink: Boolean,
    remind: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (hasLink) Rose else Color.White
    val content = if (hasLink) Color.White else RoseDark
    Row(
        modifier
            .then(if (remind) Modifier.pulse(0.05f, 1200) else Modifier)
            .bounceClick(onClick = onClick)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, if (hasLink) Rose else BlushDeep, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(13.dp), tint = content)
        Spacer(Modifier.width(4.dp))
        Text(
            if (hasLink) "Dokumentasi" else "Tambah Link",
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/**
 * Sheet dokumentasi:
 *  - selalu ada kolom link + tombol ikon update di sampingnya
 *  - kalau sudah ada link tersimpan, di bawahnya muncul tombol "Lihat dokumentasi"
 *  - mengosongkan kolom lalu menekan update = menghapus link (PUT { "link": "" })
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationSheet(
    currentLink: String,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onOpen: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Kalau link tersimpan berubah (setelah berhasil update), isi kolom ikut menyesuaikan.
    var text by remember(currentLink) { mutableStateOf(currentLink) }
    val changed = text.trim() != currentLink
    val canSave = changed && !saving

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
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Blush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, null, tint = Rose)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Dokumentasi", style = MaterialTheme.typography.headlineSmall, color = Wine)
                    Text(
                        "Simpan link Google Drive atau album foto hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WineSoft
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Link dokumentasi") },
                    placeholder = { Text("https://drive.google.com/…") },
                    leadingIcon = { Icon(Icons.Rounded.Link, null, tint = Rose) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Rose,
                        unfocusedBorderColor = BlushDeep,
                        focusedLabelColor = Rose,
                        unfocusedLabelColor = WineSoft,
                        cursorColor = Rose,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Wine,
                        unfocusedTextColor = Wine
                    ),
                    modifier = Modifier.weight(1f)
                )
                // tombol ikon update
                Box(
                    Modifier
                        .bounceClick(enabled = canSave) { onSave(text.trim()) }
                        .size(56.dp)
                        .shadow(
                            if (canSave) 8.dp else 0.dp, CircleShape,
                            ambientColor = Rose.copy(alpha = 0.3f), spotColor = Rose.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(
                            if (canSave || saving) Brush.linearGradient(listOf(Rose, Color(0xFFEF7C9A)))
                            else Brush.linearGradient(listOf(BlushDeep, BlushDeep))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(Icons.Rounded.CloudUpload, "Simpan dokumentasi", tint = Color.White)
                    }
                }
            }

            if (currentLink.isNotBlank()) {
                GradientButton(
                    text = "Lihat dokumentasi",
                    onClick = { onOpen(currentLink) },
                    icon = Icons.Rounded.PhotoLibrary,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "Belum ada dokumentasi. Isi setelah acaranya selesai dan fotonya sudah diunggah.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WineSoft
                )
            }
        }
    }
}