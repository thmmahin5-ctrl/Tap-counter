package com.example.tapcounter

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.Button
import androidx.glance.action.ActionParameters
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDate

class CounterWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Content(context) }
    }
}

@Composable
private fun Content(c: Context) {
    val today = Store.day(c, LocalDate.now())
    val count = today.sumOf { it.delta }
    val ups = today.filter { it.delta > 0 }
    val span = if (ups.size >= 2) Store.fmtDuration(Store.span(today)) else "-"
    val black = ColorProvider(Color.Black)

    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(ColorProvider(Color(0xFFF2F2F7)))
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("আজকের কাউন্ট", style = TextStyle(color = black, fontSize = 12.sp))
        Text(count.toString(), style = TextStyle(color = black, fontSize = 36.sp, fontWeight = FontWeight.Bold))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button("−", actionRunCallback<MinusAction>())
            Spacer(GlanceModifier.width(12.dp))
            Button("+", actionRunCallback<PlusAction>())
        }
        Text("প্রথম→শেষ: $span", style = TextStyle(color = black, fontSize = 11.sp))
    }
}

class PlusAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.add(context, 1)
        CounterWidget().updateAll(context)
    }
}

class MinusAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Store.add(context, -1)
        CounterWidget().updateAll(context)
    }
}

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CounterWidget()
}
