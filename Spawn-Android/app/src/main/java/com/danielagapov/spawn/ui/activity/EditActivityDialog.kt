package com.danielagapov.spawn.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danielagapov.spawn.core.model.ActivityPartialUpdateDTO
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.util.Dates
import com.danielagapov.spawn.core.util.Distance
import com.danielagapov.spawn.ui.create.ActivityLocationPicker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun EditActivityDialog(activity: FullFeedActivityDTO, vm: ActivityDetailViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val originalStart = remember(activity.id) { Dates.parse(activity.startTime) }
    val originalEnd = remember(activity.id) { Dates.parse(activity.endTime) }
    val originalDuration = remember(activity.id) {
        originalStart?.let { start -> originalEnd?.let { Duration.between(start, it) } }
    }
    val originalMinutes = originalDuration?.toMinutes()?.toString().orEmpty()
    val initialTime = remember(activity.id) {
        originalStart?.atZone(zone)?.toLocalDateTime() ?: LocalDateTime.now().plusMinutes(15)
    }
    var title by rememberSaveable(activity.id) { mutableStateOf(activity.title.orEmpty()) }
    var note by rememberSaveable(activity.id) { mutableStateOf(activity.note.orEmpty()) }
    var date by rememberSaveable(activity.id) { mutableStateOf(initialTime.toLocalDate()) }
    var time by rememberSaveable(activity.id) { mutableStateOf(initialTime.toLocalTime()) }
    var minutes by rememberSaveable(activity.id) { mutableStateOf(originalMinutes) }
    var location by remember(activity.id) { mutableStateOf(activity.location) }
    var pickingLocation by rememberSaveable(activity.id) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val saving = vm.savingEdit

    if (pickingLocation) {
        Dialog(
            onDismissRequest = { pickingLocation = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(Modifier.fillMaxSize()) {
                ActivityLocationPicker(
                    bottomPadding = 0.dp,
                    onBack = { pickingLocation = false },
                    initialLocation = location,
                    onSelect = { location = it; pickingLocation = false },
                )
            }
        }
        return
    }

    fun save() {
        val startChanged = date != initialTime.toLocalDate() || time != initialTime.toLocalTime()
        val selectedStart = LocalDateTime.of(date, time).atZone(zone).toInstant()
        val durationChanged = minutes != originalMinutes
        val duration = if (durationChanged) minutes.toLongOrNull()?.takeIf { it in 1..1439 }?.let(Duration::ofMinutes)
            else originalDuration
        if (durationChanged && duration == null) {
            error = "Enter a duration between 1 and 1,439 minutes."
            return
        }
        val start = selectedStart.takeIf { startChanged || (originalStart == null && durationChanged) }
        val end = duration?.let { (start ?: originalStart)?.plus(it) }?.takeIf { startChanged || durationChanged }
        val now = Instant.now()
        if (start?.isAfter(now) == false || end?.isAfter(now) == false) {
            error = "Choose a new time that is in the future."
            return
        }
        error = null
        val updates = ActivityPartialUpdateDTO(
            title = title.trim().takeIf { it != activity.title.orEmpty() },
            note = note.takeIf { it != activity.note.orEmpty() },
            startTime = start?.let(Dates::iso),
            endTime = end?.let(Dates::iso),
            location = location?.takeIf { it != activity.location }?.copy(id = null),
        )
        if (updates == ActivityPartialUpdateDTO()) onDismiss()
        else vm.update(updates, onDismiss) { error = it }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Edit activity") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    title, { title = it.take(60) }, enabled = !saving, label = { Text("Title") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    note, { note = it }, enabled = !saving, label = { Text("Note") },
                    maxLines = 4, modifier = Modifier.fillMaxWidth(),
                )
                EditDetailButton("Date", date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)), !saving) {
                    DatePickerDialog(context, { _, year, month, day ->
                        date = LocalDate.of(year, month + 1, day)
                    }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                        datePicker.minDate = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
                    }.show()
                }
                EditDetailButton("Time", time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)), !saving) {
                    TimePickerDialog(context, { _, hour, minute ->
                        time = LocalTime.of(hour, minute)
                    }, time.hour, time.minute, DateFormat.is24HourFormat(context)).show()
                }
                OutlinedTextField(
                    minutes, { if (it.length <= 4 && it.all(Char::isDigit)) minutes = it },
                    enabled = !saving, label = { Text("Duration (minutes)") },
                    placeholder = { Text("Indefinite") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                EditDetailButton("Location", location?.let { Distance.shortLocation(it.name) } ?: "Choose location", !saving) {
                    pickingLocation = true
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton({ save() }, enabled = !saving && title.isNotBlank()) { Text(if (saving) "Saving…" else "Save") }
        },
        dismissButton = { TextButton(onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}

@Composable
private fun EditDetailButton(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick, modifier = Modifier.fillMaxWidth(), enabled = enabled) {
        Text(label)
        Spacer(Modifier.width(8.dp))
        Text(value)
    }
}
