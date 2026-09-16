package com.colddelivery.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(primary = ColdDeliveryColors.DeepRed, secondary = ColdDeliveryColors.Gold, background = ColdDeliveryColors.Ivory, surface = ColdDeliveryColors.Cream, onPrimary = Color.White, onBackground = ColdDeliveryColors.Charcoal, onSurface = ColdDeliveryColors.Charcoal)
@Composable fun ColdDeliveryTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = Scheme, typography = ColdDeliveryTypography, content = content) }
