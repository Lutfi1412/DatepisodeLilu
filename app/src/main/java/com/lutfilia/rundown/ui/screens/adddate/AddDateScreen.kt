package com.lutfilia.rundown.ui.screens.adddate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lutfilia.rundown.ui.components.CatMascot
import com.lutfilia.rundown.ui.components.GradientButton
import com.lutfilia.rundown.ui.components.LoveLoadingOverlay
import com.lutfilia.rundown.ui.components.LoveSnackbarHost
import com.lutfilia.rundown.ui.components.ScreenHeader
import com.lutfilia.rundown.ui.components.bounceClick
import com.lutfilia.rundown.ui.components.pulse
import com.lutfilia.rundown.ui.theme.Blush
import com.lutfilia.rundown.ui.theme.BlushDeep
import com.lutfilia.rundown.ui.theme.Rose
import com.lutfilia.rundown.ui.theme.RoseDark
import com.lutfilia.rundown.ui.theme.Wine
import com.lutfilia.rundown.ui.theme.WineSoft
import com.lutfilia.rundown.util.Indo
import com.lutfilia.rundown.util.dayNameIndo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDateScreen(
    onBack: () -> Unit,
    onCreated: (id: Int, date: LocalDate) -> Unit,
    viewModel: AddDateViewModel = viewModel()
) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    // disimpan sebagai epochDay supaya aman saat konfigurasi berubah
    var selectedEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var label by rememberSaveable { mutableStateOf("") }
    val selected = selectedEpoch?.let { LocalDate.ofEpochDay(it) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is AddDateEvent.Created -> onCreated(ev.id, ev.date)
                is AddDateEvent.Failed -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent, snackbarHost = { LoveSnackbarHost(snackbar) }) { inner ->
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ScreenHeader(title = "Pilih tanggal", subtitle = "Kapan kita jalan?", onBack = onBack)

                DateTicketCard(selected = selected, onClick = { showPicker = true })

                // Tujuan "wah" untuk hari itu, mis. makan sushi di mall.
                OutlinedTextField(
                    value = label,
                    onValueChange = { if (it.length <= 60) label = it },
                    label = { Text("Tujuan hari itu") },
                    placeholder = { Text("Mis. makan sushi di mall") },
                    leadingIcon = { Icon(Icons.Rounded.Favorite, null, tint = Rose) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
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
                    modifier = Modifier.fillMaxWidth()
                )

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CatMascot(size = 96.dp, sleepy = selected == null)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (selected == null) "Ketuk tiket di atas untuk memilih tanggal."
                        else if (label.isBlank()) "Isi tujuan hari itu, lalu lanjut susun rundown-nya."
                        else "Sudah pas? Lanjut susun rundown-nya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WineSoft,
                        textAlign = TextAlign.Center
                    )
                }

                GradientButton(
                    text = "Lanjut isi rundown",
                    onClick = { selected?.let { viewModel.create(it, label.trim()) } },
                    // label wajib diisi; kalau mau opsional, hapus bagian "&& label.isNotBlank()"
                    enabled = selected != null && label.isNotBlank(),
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        LoveLoadingOverlay(visible = busy, message = "Menyimpan tanggal…")
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (selected ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        selectedEpoch = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showPicker = false
                }) { Text("Pilih", color = RoseDark, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Batal", color = WineSoft) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = pickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = Rose,
                    selectedDayContentColor = Color.White,
                    todayContentColor = Rose,
                    todayDateBorderColor = Rose,
                    currentYearContentColor = Rose,
                    selectedYearContainerColor = Rose,
                    weekdayContentColor = WineSoft,
                    navigationContentColor = Wine
                )
            )
        }
    }
}

@Composable
private fun DateTicketCard(selected: LocalDate?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .then(if (selected == null) Modifier.pulse(0.025f, 1600) else Modifier)
            .bounceClick(onClick = onClick)
            .shadow(12.dp, shape, ambientColor = Rose.copy(alpha = 0.25f), spotColor = Rose.copy(alpha = 0.35f))
            .clip(shape)
            .background(Color.White)
            .border(1.5.dp, if (selected == null) BlushDeep else Rose, shape)
            .padding(vertical = 28.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "dateTicket"
        ) { date ->
            if (date == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Blush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(32.dp), tint = Rose)
                    }
                    Text("Belum ada tanggal", style = MaterialTheme.typography.titleLarge, color = Wine)
                    Text("Ketuk untuk memilih", style = MaterialTheme.typography.bodyMedium, color = WineSoft)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(date.dayNameIndo(), style = MaterialTheme.typography.titleMedium, color = WineSoft)
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = Rose
                    )
                    Text(
                        "${Indo.months[date.monthValue - 1]} ${date.year}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Wine
                    )
                }
            }
        }
    }
}