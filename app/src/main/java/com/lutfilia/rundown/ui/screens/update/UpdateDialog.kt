package com.lutfilia.rundown.ui.screens.update

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft

/**
 * Pop up "aplikasi perlu diperbarui". Tidak bisa ditutup lewat area luar atau tombol back:
 * satu-satunya jalan adalah tombol "Mengerti" (membuka link pembaruan).
 */
@Composable
fun UpdateDialog(info: UpdateInfo, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        icon = { Icon(Icons.Rounded.SystemUpdate, null, tint = Rose) },
        title = {
            Text(
                "Aplikasi perlu diperbarui",
                style = MaterialTheme.typography.titleLarge,
                color = Wine,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Versi di HP kamu ${info.current}, sedangkan versi terbaru ${info.latest}. " +
                        "Ketuk Mengerti untuk membuka halaman pembaruan.",
                style = MaterialTheme.typography.bodyMedium,
                color = WineSoft
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Update", color = RoseDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}