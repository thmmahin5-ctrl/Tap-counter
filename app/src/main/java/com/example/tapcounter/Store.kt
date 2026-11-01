package com.example.tapcounter

import android.content.Context
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class Tap(val time: Long, val delta: Int) {
    fun date(): LocalDate = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()
}

object Store {
    private fun file(c: Context) = File(c.filesDir, "taps.txt")

    @Synchronized
    fun add(c: Context, delta: Int) {
        file(c).appendText("${System.currentTimeMillis()}|$delta\n")
    }

    @Synchronized
    fun all(c: Context): List<Tap> {
        val f = file(c)
        if (!f.exists()) return emptyList()
        return f.readLines().mapNotNull { line ->
            val p = line.split("|")
            val t = p.getOrNull(0)?.toLongOrNull()
            val d = p.getOrNull(1)?.toIntOrNull()
            if (t != null && d != null) Tap(t, d) else null
        }
    }

    fun day(c: Context, date: LocalDate) = all(c).filter { it.date() == date }

    // প্রথম ও শেষ (+) ট্যাপের মধ্যে সময়
    fun span(list: List<Tap>): Long {
        val ups = list.filter { it.delta > 0 }
        return if (ups.size >= 2) ups.last().time - ups.first().time else 0L
    }

    fun fmtDuration(ms: Long): String {
        val s = ms / 1000
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return when {
            h > 0 -> "${h}ঘ ${m}মি ${sec}সে"
            m > 0 -> "${m}মি ${sec}সে"
            else -> "${sec}সে"
        }
    }

    private val tf = DateTimeFormatter.ofPattern("hh:mm:ss a")
    fun fmtTime(ms: Long): String =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalTime().format(tf)
}
