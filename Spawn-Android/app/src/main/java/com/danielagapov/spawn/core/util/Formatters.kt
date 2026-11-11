package com.danielagapov.spawn.core.util

import android.content.Context
import android.location.Location
import androidx.compose.ui.graphics.Color
import com.danielagapov.spawn.core.model.FullFeedActivityDTO
import com.danielagapov.spawn.core.model.LocationDTO
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

object Dates {
    fun parse(s: String?): Instant? {
        if (s.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(s).toInstant()
        } catch (_: Exception) {
            try { Instant.parse(s) } catch (_: Exception) {
                try { java.time.LocalDateTime.parse(s).atZone(ZoneId.systemDefault()).toInstant() } catch (_: Exception) { null }
            }
        }
    }

    /** ISO-8601 with offset, no fractional seconds — what the backend expects. */
    fun iso(instant: Instant): String =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()))

    private val hmma = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val hmm = DateTimeFormatter.ofPattern("h:mm", Locale.US)
    private val dayName = DateTimeFormatter.ofPattern("EEEE", Locale.US)
    private val monthDay = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    fun time(i: Instant): String = hmma.format(i.atZone(ZoneId.systemDefault())).lowercase(Locale.US).replace(" ", "")

    private fun zdt(i: Instant): ZonedDateTime = i.atZone(ZoneId.systemDefault())

    /** Same wording as iOS FormatterService.formatActivityTime. */
    fun activityTime(a: FullFeedActivityDTO, now: Instant = Instant.now()): String {
        val start = parse(a.startTime) ?: return "Anytime"
        val end = parse(a.endTime)
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startDay = zdt(start).toLocalDate()

        fun dayContext(d: LocalDate): String = when {
            d == today -> "today"
            d == today.plusDays(1) -> "tomorrow"
            d == today.minusDays(1) -> "yesterday"
            sameWeek(d, today) -> dayName.format(d).lowercase(Locale.US)
            else -> monthDay.format(d)
        }

        if (end != null) {
            val sameDay = startDay == zdt(end).toLocalDate()
            if (now >= start) {
                return if (sameDay) "${hmm.format(zdt(start))} - ${hmma.format(zdt(end))}"
                else "${hmma.format(zdt(start))} - ${hmma.format(zdt(end))}"
            }
            val range = if (sameDay) "${hmm.format(zdt(start))} - ${hmma.format(zdt(end))}" else "${hmma.format(zdt(start))} - ${hmma.format(zdt(end))}"
            return when (val ctx = dayContext(startDay)) {
                "today" -> range
                else -> "$range • ${ctx.replaceFirstChar { it.uppercase() }}"
            }
        }
        val t = hmma.format(zdt(start))
        return when (val ctx = dayContext(startDay)) {
            "today" -> if (now >= start) "Started at $t" else "Starts at $t"
            "tomorrow", "yesterday" -> "$ctx at $t".replaceFirstChar { it.uppercase() }
            else -> if (sameWeek(startDay, today)) "${ctx.replaceFirstChar { it.uppercase() }} at $t" else "$t • $ctx"
        }
    }

    /** "In 2 hours • 6 - 7:30pm" style line used on the activity drawer (Figma). */
    fun relativeAndRange(a: FullFeedActivityDTO): String {
        val status = ActivityStatus.of(a)
        val range = activityTime(a)
        return if (status == ActivityStatus.HappeningNow) "Happening now • $range" else "${status.text} • $range"
    }

    private fun sameWeek(a: LocalDate, b: LocalDate): Boolean {
        val wf = WeekFields.of(Locale.getDefault())
        return a.get(wf.weekOfWeekBasedYear()) == b.get(wf.weekOfWeekBasedYear()) && a.year == b.year
    }

    fun relativeAgo(s: String?): String {
        val i = parse(s) ?: return ""
        val d = Duration.between(i, Instant.now())
        return when {
            d.toMinutes() < 1 -> "just now"
            d.toMinutes() < 60 -> "${d.toMinutes()}m ago"
            d.toHours() < 24 -> "${d.toHours()}h ago"
            d.toDays() < 7 -> "${d.toDays()}d ago"
            else -> monthDay.format(zdt(i))
        }
    }
}

sealed class ActivityStatus(val text: String) {
    data object HappeningNow : ActivityStatus("Happening Now")
    data class InMinutes(val m: Long) : ActivityStatus("In $m min")
    data class InHours(val h: Long) : ActivityStatus("In $h hour${if (h == 1L) "" else "s"}")
    data object LaterToday : ActivityStatus("Later Today")
    data class InDays(val d: Long) : ActivityStatus(if (d == 1L) "Tomorrow" else "In $d days")
    data object Past : ActivityStatus("Already Happened")

    val isLive: Boolean get() = this == HappeningNow

    companion object {
        fun of(a: FullFeedActivityDTO, now: Instant = Instant.now()): ActivityStatus {
            val start = Dates.parse(a.startTime) ?: return HappeningNow
            val end = Dates.parse(a.endTime)
            if (end != null && now >= end) return Past
            if (now >= start) return HappeningNow
            val mins = Duration.between(now, start).toMinutes()
            val zone = ZoneId.systemDefault()
            val days = ChronoUnit.DAYS.between(LocalDate.now(zone), start.atZone(zone).toLocalDate())
            return when {
                mins < 60 -> InMinutes(maxOf(1, mins))
                days == 0L && mins < 6 * 60 -> InHours(mins / 60)
                days == 0L -> LaterToday
                else -> InDays(days)
            }
        }
    }
}

object Distance {
    fun between(from: Location?, to: LocationDTO?): String? {
        if (from == null || to == null) return null
        val out = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, out)
        val m = out[0]
        return when {
            m < 1000 -> "${m.toInt()}m away"
            m < 100_000 -> String.format(Locale.US, "%.1fkm away", m / 1000f)
            else -> "${(m / 1000).toInt()}km away"
        }
    }

    /** Street-ish short name, same trimming as iOS ActivityInfoViewModel(.location). */
    fun shortLocation(name: String?): String {
        val raw = name ?: return "No Location"
        val parts = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 && parts[0].firstOrNull()?.isDigit() == true -> parts[0] + " " + parts[1]
            parts.isNotEmpty() -> parts[0]
            else -> raw
        }
    }
}

/**
 * Stable color per activity, like iOS ActivityColorService: rotate through the four Figma
 * activity colors and remember the assignment so a card keeps its color across launches.
 */
class ActivityColors(context: Context) {
    private val prefs = context.getSharedPreferences("activity_colors", Context.MODE_PRIVATE)

    fun indexFor(activityId: String): Int {
        val existing = prefs.getInt(activityId, -1)
        if (existing >= 0) return existing
        val next = prefs.getInt("__next", 0)
        prefs.edit().putInt(activityId, next % 4).putInt("__next", next + 1).apply()
        return next % 4
    }
}

fun hexColor(hex: String?, fallback: Color = Color(0xFF6B81FB)): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.removePrefix("#")
        val v = clean.toLong(16)
        when (clean.length) {
            6 -> Color(0xFF000000 or v)
            8 -> Color(((v and 0xFF) shl 24) or (v shr 8))
            else -> fallback
        }
    } catch (_: Exception) { fallback }
}
