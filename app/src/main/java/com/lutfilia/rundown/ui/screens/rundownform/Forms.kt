package com.lutfilia.rundown.ui.screens.rundownform

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lutfilia.rundown.data.model.RundownDraft
import com.lutfilia.rundown.ui.components.GradientButton
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.theme.Amber
import com.lutfilia.rundown.ui.theme.AmberSoft
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Petal
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.MapsLink
import com.lutfilia.rundown.util.hm
import java.time.LocalTime

/** Isi form satu rundown. Dipakai untuk "buat" dan "ubah". */
class RundownFormState(initial: RundownDraft? = null) {
    var nama by mutableStateOf(initial?.nama ?: "")
    var start by mutableStateOf(initial?.start)
    var end by mutableStateOf(initial?.end)
    var link by mutableStateOf(initial?.link ?: "")

    /** Rating tidak diinput user: dibawa dari data lama, atau diisi server dari scraping link. */
    var rating: Double? = initial?.rating
    private val initialLink: String = initial?.link.orEmpty().trim()

    val isValid: Boolean get() = nama.isNotBlank() && start != null && end != null
    val isBlank: Boolean get() = nama.isBlank() && start == null && end == null && link.isBlank()
    val endsBeforeStart: Boolean
        get() = start != null && end != null && !end!!.isAfter(start)

    fun toDraft(): RundownDraft? {
        val s = start ?: return null
        val e = end ?: return null
        if (nama.isBlank()) return null
        val cleanLink = MapsLink.extract(link)
        // Link berubah -> rating lama tidak berlaku lagi, biarkan server scraping ulang.
        val ratingToSend = if (cleanLink != initialLink) null else rating
        return RundownDraft(nama.trim(), s, e, cleanLink, ratingToSend)
    }

    fun clear(nextStart: LocalTime? = null) {
        nama = ""
        start = nextStart
        end = null
        link = ""
        rating = null
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Rose,
    unfocusedBorderColor = BlushDeep,
    focusedLabelColor = Rose,
    unfocusedLabelColor = WineSoft,
    cursorColor = Rose,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = Wine,
    unfocusedTextColor = Wine
)

@Composable
fun RundownFormFields(form: RundownFormState, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = form.nama,
            onValueChange = { form.nama = it },
            label = { Text("Nama acara") },
            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, null, tint = Rose) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeField("Jam mulai", form.start, { form.start = it }, Modifier.weight(1f))
            TimeField("Jam selesai", form.end, { form.end = it }, Modifier.weight(1f))
        }
        if (form.endsBeforeStart) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmberSoft)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Warning, null, Modifier.size(18.dp), tint = Amber)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Jam selesai tidak lebih besar dari jam mulai. Server akan menolaknya dan acara ini masuk ke tab Gagal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Amber
                )
            }
        }
        OutlinedTextField(
            value = form.link,
            // Hasil "Bagikan" dari Google Maps berisi nama + alamat + link: ambil link-nya saja.
            onValueChange = { text ->
                form.link = if (text.any { it.isWhitespace() }) MapsLink.extract(text) else text
            },
            label = { Text("Link Google Maps (opsional)") },
            leadingIcon = { Icon(Icons.Rounded.Place, null, tint = Rose) },
            supportingText = { Text("Boleh link web atau link dari aplikasi Google Maps. Rating diambil otomatis.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape = RoundedCornerShape(18.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TimeField(label: String, time: LocalTime?, onPick: (LocalTime) -> Unit, modifier: Modifier = Modifier) {
    var show by remember { mutableStateOf(false) }
    Column(
        modifier
            .bounceClick { show = true }
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, BlushDeep, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WineSoft)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp), tint = Rose)
            Spacer(Modifier.width(8.dp))
            Text(
                time?.hm() ?: "--:--",
                style = MaterialTheme.typography.titleMedium,
                color = if (time == null) WineSoft.copy(alpha = 0.6f) else Wine
            )
        }
    }
    if (show) {
        TimePickDialog(
            title = label,
            initial = time,
            onDismiss = { show = false },
            onConfirm = {
                onPick(it)
                show = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickDialog(
    title: String,
    initial: LocalTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial?.hour ?: 9,
        initialMinute = initial?.minute ?: 0,
        is24Hour = true
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Wine, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.size(16.dp))
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Blush,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Wine,
                        selectorColor = Rose,
                        timeSelectorSelectedContainerColor = Rose,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = Blush,
                        timeSelectorUnselectedContentColor = Wine
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal", color = WineSoft) }
                    TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                        Text("Pilih", color = RoseDark, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/** Sheet ubah: nilai lama sudah terisi, user cukup mengubah yang perlu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RundownEditSheet(
    title: String,
    subtitle: String,
    confirmLabel: String,
    initial: RundownDraft?,
    onDismiss: () -> Unit,
    onConfirm: (RundownDraft) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val form = remember { RundownFormState(initial) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Petal,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = Wine)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WineSoft)
            }
            RundownFormFields(form)
            GradientButton(
                text = confirmLabel,
                onClick = { form.toDraft()?.let(onConfirm) },
                enabled = form.isValid,
                icon = Icons.AutoMirrored.Rounded.Send,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}