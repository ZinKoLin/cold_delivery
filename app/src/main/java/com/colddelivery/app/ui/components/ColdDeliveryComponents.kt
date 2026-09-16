package com.colddelivery.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colddelivery.app.R
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import com.colddelivery.app.ui.theme.ColdDeliveryElevation
import com.colddelivery.app.ui.theme.ColdDeliveryShapes

val Ivory = ColdDeliveryColors.Ivory
val Beige = ColdDeliveryColors.Beige
val DeepRed = ColdDeliveryColors.DeepRed
val Gold = ColdDeliveryColors.Gold
val DeliveredGreen = ColdDeliveryColors.Delivered

@Composable fun ColdDeliveryCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = ColdDeliveryShapes.Card, border = BorderStroke(1.dp, Gold.copy(alpha = .13f)), colors = CardDefaults.cardColors(containerColor = ColdDeliveryColors.Cream), elevation = CardDefaults.cardElevation(ColdDeliveryElevation.card), content = content)
}

@Composable fun PremiumPage(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(Ivory).padding(horizontal = 17.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
}

@Composable fun PremiumPageTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(bottom = 2.dp)) {
        Text(title, color = DeepRed, fontSize = 22.sp, lineHeight = 27.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp, lineHeight = 15.sp) }
    }
}

@Composable fun PremiumOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 46.dp), shape = ColdDeliveryShapes.Button, border = BorderStroke(1.dp, DeepRed.copy(alpha = .6f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepRed)) { Text(text, fontSize = 13.sp, maxLines = 1) }
}

@Composable fun SummaryStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ColdDeliveryCard(modifier) { Column(Modifier.padding(16.dp)) { Text(value, style = MaterialTheme.typography.headlineSmall, color = DeepRed); Text(label, style = MaterialTheme.typography.labelMedium, color = ColdDeliveryColors.SecondaryText) } }
}

@Composable fun StatusChip(delivered: Boolean) {
    val tone = if (delivered) DeliveredGreen else ColdDeliveryColors.Pending
    Surface(shape = CircleShape, color = tone.copy(alpha = .10f)) { Text(stringResource(if (delivered) R.string.delivered else R.string.pending), Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = tone, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable fun PrimaryRedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.heightIn(min = 56.dp), shape = ColdDeliveryShapes.Button, colors = ButtonDefaults.buttonColors(containerColor = DeepRed), elevation = ButtonDefaults.buttonElevation(defaultElevation = ColdDeliveryElevation.button)) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp) }
}

@Composable fun LanguageSegmentedControl(language: String, onLanguage: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(CircleShape).background(ColdDeliveryColors.Cream).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("my" to "မြန်မာ", "en" to "English").forEachIndexed { index, (code, label) ->
            Box(Modifier.clip(CircleShape).background(if (language == code) DeepRed else Color.Transparent).clickable { onLanguage(code) }.padding(horizontal = 11.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (language == code) Color.White else ColdDeliveryColors.Charcoal, fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
            }
            if (index == 0) Spacer(Modifier.width(1.dp).height(18.dp).background(Gold.copy(alpha = .6f)))
        }
    }
}

@Composable fun PagodaLineArt(modifier: Modifier = Modifier, color: Color = Gold, opacity: Float = .78f) {
    Canvas(modifier) {
        val w = size.width; val h = size.height; val c = color.copy(alpha = opacity); val stroke = 1.4.dp.toPx(); val center = w * .5f
        drawLine(c, Offset(center, h * .02f), Offset(center, h * .26f), stroke)
        for (level in 0..4) {
            val y = h * (.30f + level * .095f); val half = w * (.055f + level * .044f)
            val roof = Path().apply { moveTo(center, y - h * .075f); lineTo(center - half, y); lineTo(center + half, y); close() }
            drawPath(roof, c, style = Stroke(stroke))
            drawLine(c, Offset(center - half * .74f, y + h * .02f), Offset(center + half * .74f, y + h * .02f), stroke)
        }
        val baseY = h * .82f
        drawLine(c, Offset(w * .13f, baseY), Offset(w * .87f, baseY), stroke)
        drawLine(c, Offset(w * .24f, h * .69f), Offset(w * .24f, baseY), stroke)
        drawLine(c, Offset(w * .76f, h * .69f), Offset(w * .76f, baseY), stroke)
        drawLine(c, Offset(w * .15f, h * .69f), Offset(w * .85f, h * .69f), stroke)
        drawLine(c, Offset(w * .15f, h * .69f), Offset(w * .27f, h * .60f), stroke)
        drawLine(c, Offset(w * .85f, h * .69f), Offset(w * .73f, h * .60f), stroke)
        drawLine(c, Offset(w * .27f, h * .60f), Offset(w * .73f, h * .60f), stroke)
        drawLine(c, Offset(center - w * .09f, baseY), Offset(center - w * .09f, h * .72f), stroke)
        drawLine(c, Offset(center + w * .09f, baseY), Offset(center + w * .09f, h * .72f), stroke)
        drawLine(c, Offset(center - w * .09f, h * .72f), Offset(center + w * .09f, h * .72f), stroke)
    }
}

@Composable fun ColdDeliveryHeader(title: String = "Cold Delivery", subtitle: String = "Good Drinks · Brighter Tomorrows", language: String? = null, onLanguage: (String) -> Unit = {}, homeArtwork: Boolean = false) {
    Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(ColdDeliveryColors.DarkRed, DeepRed, ColdDeliveryColors.DarkRed))).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(98.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 48.dp, height = 70.dp)) {
                PagodaLineArt(Modifier.fillMaxSize(), opacity = if (homeArtwork) .83f else .93f)
                if (homeArtwork) {
                    PagodaLineArt(Modifier.align(Alignment.BottomStart).size(18.dp, 28.dp), opacity = .34f)
                    PagodaLineArt(Modifier.align(Alignment.BottomEnd).size(18.dp, 32.dp), opacity = .34f)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 22.sp, lineHeight = 28.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(5.dp))
                Text(subtitle, color = Beige, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (language != null) LanguageSegmentedControl(language, onLanguage)
        }
        Text("❈", color = Gold.copy(alpha = if (homeArtwork) .36f else .48f), fontSize = if (homeArtwork) 19.sp else 25.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 5.dp, end = 8.dp))
    }
}

@Composable fun MyanmarDecorativeDivider() {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = Gold.copy(alpha = .52f))
        Text("  ❈  ", color = Gold, fontSize = 18.sp)
        HorizontalDivider(Modifier.weight(1f), color = Gold.copy(alpha = .52f))
    }
}
