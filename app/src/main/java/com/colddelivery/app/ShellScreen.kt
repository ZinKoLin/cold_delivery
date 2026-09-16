package com.colddelivery.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.colddelivery.app.ui.components.ColdDeliveryHeader
import com.colddelivery.app.ui.feature.FeatureViewModel
import com.colddelivery.app.ui.feature.LanguageManager
import com.colddelivery.app.ui.theme.ColdDeliveryColors

@Composable fun ShellScreen(title: String, nav: NavHostController, content: @Composable () -> Unit = { Column(Modifier.padding(24.dp)) { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(8.dp)); Text(stringResource(R.string.offline_delivery_management), style = MaterialTheme.typography.bodyLarge) } }) {
    val feature: FeatureViewModel = hiltViewModel()
    val language by feature.language.collectAsState()
    val context = LocalContext.current
    Scaffold(containerColor = ColdDeliveryColors.Ivory, topBar = { ColdDeliveryHeader(language = if (title == "Home") language else null, onLanguage = { feature.saveLanguage(it); LanguageManager.apply(context, it) }, homeArtwork = title == "Home") }, bottomBar = { ColdDeliveryBottomNav(title, nav) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) { content() }
    }
}

@Composable fun ColdDeliveryBottomNav(title: String, nav: NavHostController) {
    val labels = listOf(R.string.home, R.string.customers, R.string.stock, R.string.history, R.string.settings)
    val icons: List<ImageVector> = listOf(Icons.Default.Home, Icons.Default.People, Icons.Default.Inventory, Icons.Default.History, Icons.Default.Settings)
    val routes = listOf("home", "customers", "stock", "history", "settings")
    val active = routes.indexOf(title.lowercase()).takeIf { it >= 0 } ?: -1
    Column(Modifier.fillMaxWidth().shadow(5.dp).background(ColdDeliveryColors.Cream).navigationBarsPadding()) {
        HorizontalDivider(color = ColdDeliveryColors.Gold.copy(alpha = .28f), thickness = 1.dp)
        Row(Modifier.fillMaxWidth().height(61.dp), verticalAlignment = Alignment.CenterVertically) {
            routes.indices.forEach { index ->
                val selected = index == active
                val tint = if (selected) ColdDeliveryColors.DeepRed else ColdDeliveryColors.SecondaryText
                Column(Modifier.weight(1f).fillMaxHeight().clickable { nav.navigate(routes[index]) { launchSingleTop = true } }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.width(24.dp).height(2.dp).background(if (selected) ColdDeliveryColors.DeepRed else Color.Transparent))
                    Spacer(Modifier.height(7.dp))
                    Icon(icons[index], stringResource(labels[index]), tint = tint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(labels[index]), color = tint, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
