package com.colddelivery.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ColdDeliveryColors {
    val DeepRed = Color(0xFFB20D18)
    val DarkRed = Color(0xFF8E0A12)
    val Ivory = Color(0xFFFFF8EA)
    val Cream = Color(0xFFFFFCF5)
    val Beige = Color(0xFFF4E9D5)
    val Gold = Color(0xFFC69A52)
    val Charcoal = Color(0xFF27231F)
    val SecondaryText = Color(0xFF766E63)
    val Delivered = Color(0xFF2F7D47)
    val Pending = Color(0xFFC62828)
}

object ColdDeliveryShapes { val Card = RoundedCornerShape(20.dp); val Input = RoundedCornerShape(16.dp); val Button = RoundedCornerShape(16.dp) }
object ColdDeliverySpacing { val xs = 4.dp; val sm = 8.dp; val md = 12.dp; val lg = 16.dp; val xl = 24.dp; val xxl = 32.dp }
object ColdDeliveryElevation { val card = 3.dp; val button = 5.dp }
val ColdDeliveryTypography = Typography()
