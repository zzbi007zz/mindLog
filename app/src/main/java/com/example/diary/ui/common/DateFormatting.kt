package com.example.diary.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
private val HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d")

/** Local date (system zone) for an epoch-millis timestamp. */
fun dateOf(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

/** e.g. Constructor "14:32". */
fun timeLabel(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(epochMillis).atZone(zone).format(TIME_FORMATTER)

/** e.g. "Mon, Aug 3" — used for the sticky date header. */
fun headerLabel(date: LocalDate): String =
    date.format(HEADER_FORMATTER)