package com.example.tapcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var tick by remember { mutableStateOf(0) }
    var tab by remember { mutableStateOf(0) }

    fun change(d: Int) {
        Store.add(ctx, d)
        tick++
        scope.launch { CounterWidget().updateAll(ctx) }
    }

    val all = remember(tick) { Store.all(ctx) }
    val today = LocalDate.now()
    val todayTaps = all.filter { it.date() == today }

    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("আজকের কাউন্ট", fontSize = 14.sp)
        Text(todayTaps.sumOf { it.delta }.toString(), fontSize = 56.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { change(-1) }) { Text("−") }
            Button(onClick = { change(1) }) { Text("+") }
        }
        Spacer(Modifier.height(12.dp))
        TabRow(selectedTabIndex = tab) {
            listOf("আজ", "সপ্তাহ", "মাস").forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i }) { Text(t, Modifier.padding(12.dp)) }
            }
        }
        Spacer(Modifier.height(8.dp))
        when (tab) {
            0 -> TodayView(todayTaps)
            1 -> RangeView(all, (0L..6L).map { today.minusDays(it) })
            else -> RangeView(all, (0 until today.dayOfMonth).map { today.minusDays(it.toLong()) })
        }
    }
}

@Composable
fun TodayView(taps: List<Tap>) {
    val ups = taps.filter { it.delta > 0 }
    Column {
        if (ups.isNotEmpty()) {
            Text("প্রথম ট্যাপ: ${Store.fmtTime(ups.first().time)}")
            Text("শেষ ট্যাপ: ${Store.fmtTime(ups.last().time)}")
            Text("প্রথম থেকে শেষ পর্যন্ত সময়: ${Store.fmtDuration(Store.span(taps))}")
        } else Text("আজ এখনো কোনো ট্যাপ নেই")
        Spacer(Modifier.height(8.dp))
        var n = 0
        val rows = taps.map { if (it.delta > 0) { n++; "#$n   ${Store.fmtTime(it.time)}" } else "−    ${Store.fmtTime(it.time)}" }
        LazyColumn { items(rows) { Text(it, Modifier.padding(vertical = 3.dp)) } }
    }
}

@Composable
fun RangeView(all: List<Tap>, days: List<LocalDate>) {
    val totals = days.map { d -> all.filter { it.date() == d } }
    Column {
        Text("মোট: ${totals.sumOf { l -> l.sumOf { it.delta } }}   |   গড়/দিন: " +
                "%.1f".format(totals.sumOf { l -> l.sumOf { it.delta } }.toDouble() / days.size))
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(days.indices.toList()) { i ->
                val l = totals[i]
                val span = if (l.any { it.delta > 0 }) Store.fmtDuration(Store.span(l)) else "-"
                Text("${days[i]}:  ${l.sumOf { it.delta }}   (প্রথম→শেষ: $span)",
                    Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
