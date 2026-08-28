package com.example.diary.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private val HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d")
private val EYEBROW_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d")
private val FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

/** Local date (system zone) for an epoch-millis timestamp. */
fun dateOf(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

/** e.g. "14:32". */
fun timeLabel(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).format(TIME_FORMATTER)

/** e.g. "Mon, Aug 3" — used for the list section header. */
fun headerLabel(date: LocalDate): String =
    date.format(HEADER_FORMATTER)

/** Friendly long eyebrow, e.g. "Thursday, August 28". */
fun todayEyebrow(date: LocalDate = LocalDate.now()): String =
    date.format(EYEBROW_FORMATTER)

/** Full reading date for the detail screen, e.g. "Thursday, August 28, 2026". */
fun fullDateLabel(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).format(FULL_DATE_FORMATTER)