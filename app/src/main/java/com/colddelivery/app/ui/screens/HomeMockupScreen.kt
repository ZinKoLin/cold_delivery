package com.colddelivery.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colddelivery.app.R
import com.colddelivery.app.data.local.entity.StockBatchEntity
import com.colddelivery.app.ui.components.*
import com.colddelivery.app.ui.feature.FeatureViewModel
import com.colddelivery.app.ui.home.HomeViewModel
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import java.time.format.DateTimeFormatter

@Composable fun HomeMockupScreen(onNewDelivery: () -> Unit, onPrice: () -> Unit = {}, vm: HomeViewModel = hiltViewModel(), feature: FeatureViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val products by feature.products.collectAsState()
    val prices by feature.prices().collectAsState(initial = emptyList())
    val threshold by feature.lowStockThreshold.collectAsState()
    var batches by remember { mutableStateOf<Map<Long, List<StockBatchEntity>>>(emptyMap()) }
    LaunchedEffect(products) { batches = products.associate { it.id to feature.stockBatches(it.id) } }
    val alerts = products.mapNotNull { product -> val remaining = batches[product.id].orEmpty().sumOf { it.remainingQty }; if (remaining <= threshold) product to remaining else null }.sortedBy { it.second }
    val delivered = state.deliveredCustomerIds.intersect(state.todayCustomers.map { it.id }.toSet()).size
    val pending = (state.todayCustomers.size - delivered).coerceAtLeast(0)

    Box(Modifier.fillMaxSize().background(Ivory)) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 17.dp), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(top = 9.dp, bottom = 83.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(82.dp)) {
                    HomeSkyline(Modifier.align(Alignment.BottomEnd).size(148.dp, 61.dp))
                    Column(Modifier.align(Alignment.TopStart)) {
                        Text("${vm.today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} • ${vm.today.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}", fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, color = ColdDeliveryColors.Charcoal, maxLines = 1)
                        Text(if (vm.today.dayOfWeek.name == "SUNDAY") "Closed Day" else "Today Delivery", fontSize = 11.sp, lineHeight = 15.sp, color = ColdDeliveryColors.SecondaryText, modifier = Modifier.padding(top = 2.dp))
                    }
                    if (vm.today.dayOfWeek.name != "SUNDAY") {
                        Surface(Modifier.align(Alignment.BottomStart), shape = CircleShape, color = Beige) {
                            Text("${state.todayCustomers.size} ${if (state.todayCustomers.size == 1) "Customer" else "Customers"} Today", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = DeepRed, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1)
                        }
                    }
                }
                HomeGoldDivider()
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HomeSummary("Customers", state.todayCustomers.size.toString(), ColdDeliveryColors.Charcoal, Icons.Default.People, Modifier.weight(1f))
                    HomeSummary("Delivered", delivered.toString(), DeliveredGreen, Icons.Default.LocalShipping, Modifier.weight(1f))
                    HomeSummary("Pending", pending.toString(), ColdDeliveryColors.Pending, Icons.Default.WarningAmber, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ColdDeliveryCard(Modifier.weight(1f).height(155.dp)) {
                        Column(Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 10.dp)) {
                            Text("Today's Price", color = ColdDeliveryColors.Charcoal, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            HorizontalDivider(Modifier.padding(top = 6.dp, bottom = 5.dp), color = Gold.copy(alpha = .5f))
                            if (prices.isEmpty()) Text("Set today's prices", color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp)
                            prices.take(3).forEach { price ->
                                val product = products.firstOrNull { it.id == price.productId }
                                Row(Modifier.fillMaxWidth().height(25.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(product?.productName ?: "Product", Modifier.weight(1f), color = ColdDeliveryColors.Charcoal, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.width(3.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("%,d".format(price.price), color = ColdDeliveryColors.Charcoal, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("MMK", color = ColdDeliveryColors.SecondaryText, fontSize = 8.sp, lineHeight = 9.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = onPrice, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(23.dp)) { Text("Open prices", color = DeepRed, fontSize = 10.sp) }
                        }
                    }
                    ColdDeliveryCard(Modifier.weight(1f).height(155.dp)) {
                        Column(Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, null, tint = if (alerts.isEmpty()) DeliveredGreen else DeepRed, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Stock Alert", color = if (alerts.isEmpty()) DeliveredGreen else DeepRed, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            HorizontalDivider(Modifier.padding(top = 6.dp, bottom = 5.dp), color = Gold.copy(alpha = .5f))
                            if (alerts.isEmpty()) Text("Stock levels healthy", color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp)
                            alerts.take(2).forEach { (product, remaining) ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                    Text(product.productName, color = ColdDeliveryColors.Charcoal, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (remaining == 0) "Zero Stock · 0 cartons" else "$remaining cartons · Low Stock", color = if (remaining == 0) DeepRed else ColdDeliveryColors.SecondaryText, fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            item { Text("Today's Customers", color = ColdDeliveryColors.Charcoal, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp)) }
            items(state.todayCustomers, key = { it.id }) { customer ->
                ColdDeliveryCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(36.dp), shape = CircleShape, color = DeepRed.copy(alpha = .09f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory2, null, tint = DeepRed, modifier = Modifier.size(19.dp)) } }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(customer.name, color = ColdDeliveryColors.Charcoal, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(customer.phone, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 1)
                            Text(customer.address, color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp, lineHeight = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(5.dp))
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            HomeStatusChip(state.deliveredCustomerIds.contains(customer.id))
                            Text("›", color = DeepRed, fontSize = 18.sp, lineHeight = 18.sp, modifier = Modifier.padding(end = 2.dp))
                        }
                    }
                }
            }
        }
        Text("Myanmar’s refreshment reaches further together", color = Gold.copy(alpha = .78f), fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 10.sp, lineHeight = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 49.dp).width(210.dp), maxLines = 2)
        FloatingActionButton(onClick = onNewDelivery, containerColor = DeepRed, contentColor = Color.White, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(6.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 15.dp).size(52.dp)) { Icon(Icons.Default.Add, "New delivery") }
    }
}

@Composable private fun HomeGoldDivider() {
    Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = Gold.copy(alpha = .47f))
        Text("  ❈  ", color = Gold, fontSize = 14.sp, lineHeight = 17.sp)
        HorizontalDivider(Modifier.weight(1f), color = Gold.copy(alpha = .47f))
    }
}

@Composable private fun HomeSkyline(modifier: Modifier) {
    Box(modifier) {
        PagodaLineArt(Modifier.align(Alignment.BottomCenter).size(66.dp, 58.dp), color = Gold, opacity = .23f)
        PagodaLineArt(Modifier.align(Alignment.BottomStart).padding(start = 22.dp).size(30.dp, 31.dp), color = Gold, opacity = .16f)
        PagodaLineArt(Modifier.align(Alignment.BottomEnd).padding(end = 15.dp).size(30.dp, 36.dp), color = Gold, opacity = .16f)
        Canvas(Modifier.fillMaxWidth().height(9.dp).align(Alignment.BottomCenter)) {
            drawLine(Gold.copy(alpha = .18f), Offset(0f, size.height * .65f), Offset(size.width, size.height * .65f), 1.dp.toPx())
        }
    }
}

@Composable private fun HomeStatusChip(delivered: Boolean) {
    val tone = if (delivered) DeliveredGreen else ColdDeliveryColors.Pending
    Surface(shape = CircleShape, color = tone.copy(alpha = .10f)) {
        Row(Modifier.padding(horizontal = 7.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (delivered) Icons.Default.CheckCircle else Icons.Default.AccessTime, null, tint = tone, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(3.dp))
            Text(stringResource(if (delivered) R.string.delivered else R.string.pending), color = tone, fontSize = 9.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable private fun HomeSummary(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier) {
    ColdDeliveryCard(modifier.height(85.dp)) {
        Box(Modifier.fillMaxSize()) {
            Text("❈", color = Gold.copy(alpha = .20f), fontSize = 19.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 1.dp))
            Column(Modifier.fillMaxSize().padding(horizontal = 9.dp, vertical = 7.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
                Text(value, color = color, fontSize = 25.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold)
                Text(label, color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp, lineHeight = 12.sp, maxLines = 1)
            }
        }
    }
}
