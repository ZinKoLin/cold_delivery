package com.colddelivery.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colddelivery.app.data.local.entity.*
import com.colddelivery.app.ui.components.*
import com.colddelivery.app.ui.feature.FeatureViewModel
import com.colddelivery.app.ui.home.HomeViewModel
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.ZoneOffset

private fun formatMoney(value: Long) = "%,d MMK".format(value)
private fun formatDay(value: Long) = java.time.LocalDate.ofEpochDay(value).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

@Composable fun HomeDashboard(onNewDelivery: () -> Unit, onPrice: () -> Unit = {}, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState(); val delivered = state.deliveredCustomerIds.intersect(state.todayCustomers.map { it.id }.toSet()).size; val pending = (state.todayCustomers.size - delivered).coerceAtLeast(0)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(vertical = 18.dp)) {
        item { Text("${vm.today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} • ${vm.today.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}", style = MaterialTheme.typography.titleMedium, color = DeepRed); Text(stringResource(com.colddelivery.app.R.string.app_name), style = MaterialTheme.typography.headlineLarge) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryStatCard(stringResource(com.colddelivery.app.R.string.customers_label), state.todayCustomers.size.toString(), Modifier.weight(1f)); SummaryStatCard(stringResource(com.colddelivery.app.R.string.delivered), delivered.toString(), Modifier.weight(1f)); SummaryStatCard(stringResource(com.colddelivery.app.R.string.pending), pending.toString(), Modifier.weight(1f)) } }
        item { ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(stringResource(com.colddelivery.app.R.string.todays_price), style = MaterialTheme.typography.titleMedium); Text(stringResource(com.colddelivery.app.R.string.todays_price_subtitle), color = Color.DarkGray); Spacer(Modifier.height(8.dp)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.open_prices), onPrice) } } }
        item { Text(if (state.todayCustomers.isEmpty()) stringResource(com.colddelivery.app.R.string.closed_day) else stringResource(com.colddelivery.app.R.string.todays_customers), style = MaterialTheme.typography.titleLarge) }
        items(state.todayCustomers.take(6), key = { it.id }) { customer -> ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(customer.name, style = MaterialTheme.typography.titleMedium); StatusChip(state.deliveredCustomerIds.contains(customer.id)) }; Text(customer.phone); Text(customer.address, color = Color.DarkGray) } } }
    }
}

@Composable fun CustomersScreen(onAdd: () -> Unit = {}, onOpen: (Long) -> Unit = {}, vm: HomeViewModel = hiltViewModel()) { val state by vm.state.collectAsState(); var query by remember { mutableStateOf("") }; var selectedDay by remember { mutableStateOf<DeliveryDay?>(null) }; val customers = state.customers.filter { it.name.contains(query, true) && (selectedDay == null || it.deliveryDay == selectedDay) }; PremiumPage { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.customers), stringResource(com.colddelivery.app.R.string.customers_subtitle)); TextButton(onClick = onAdd) { Text(stringResource(com.colddelivery.app.R.string.add_customer), color = DeepRed, fontSize = 12.sp) } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { DeliveryDay.values().forEach { d -> FilterChip(selectedDay == d, { selectedDay = if (selectedDay == d) null else d }, label = { Text(d.name.take(3).lowercase().replaceFirstChar { it.uppercase() }, fontSize = 10.sp) }) } }; OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().heightIn(min = 50.dp), label = { Text(stringResource(com.colddelivery.app.R.string.search_customer)) }, singleLine = true, shape = com.colddelivery.app.ui.theme.ColdDeliveryShapes.Input); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SummaryStatCard(stringResource(com.colddelivery.app.R.string.customers_label), customers.size.toString(), Modifier.weight(1f)); SummaryStatCard(stringResource(com.colddelivery.app.R.string.today_label), state.todayCustomers.size.toString(), Modifier.weight(1f)) }; LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) { items(customers, key = { it.id }) { customer -> ColdDeliveryCard(Modifier.fillMaxWidth().clickable { onOpen(customer.id) }) { Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Surface(Modifier.size(36.dp), shape = androidx.compose.foundation.shape.CircleShape, color = DeepRed.copy(alpha = .09f)) { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Text("⌂", color = DeepRed, fontSize = 21.sp) } }; Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(customer.name, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1); Text(customer.phone, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(customer.address, color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }; Column(horizontalAlignment = androidx.compose.ui.Alignment.End) { Text(customer.deliveryDay.name.take(3), color = Gold, fontSize = 9.sp); StatusChip(state.deliveredCustomerIds.contains(customer.id)) } } } } } } }

@Composable fun StockScreen(onAddProduct: () -> Unit = {}, onStockIn: () -> Unit = {}, onOpen: (Long) -> Unit = {}, vm: FeatureViewModel = hiltViewModel()) { val products by vm.products.collectAsState(); val threshold by vm.lowStockThreshold.collectAsState(); var summaries by remember { mutableStateOf<Map<Long, List<StockBatchEntity>>>(emptyMap()) }; LaunchedEffect(products) { summaries = products.associate { it.id to vm.stockBatches(it.id) } }; val total = summaries.values.flatten().sumOf { it.remainingQty }; PremiumPage { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.stock), stringResource(com.colddelivery.app.R.string.stock_subtitle)); Row { TextButton(onClick = onStockIn) { Text(stringResource(com.colddelivery.app.R.string.stock_in), color = DeepRed, fontSize = 11.sp) }; TextButton(onClick = onAddProduct) { Text(stringResource(com.colddelivery.app.R.string.add_product_short), color = DeepRed, fontSize = 11.sp) } } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SummaryStatCard(stringResource(com.colddelivery.app.R.string.products), products.size.toString(), Modifier.weight(1f).height(84.dp)); SummaryStatCard(stringResource(com.colddelivery.app.R.string.cartons), total.toString(), Modifier.weight(1f).height(84.dp)); SummaryStatCard(stringResource(com.colddelivery.app.R.string.alerts), summaries.count { it.value.sumOf(StockBatchEntity::remainingQty) <= threshold }.toString(), Modifier.weight(1f).height(84.dp)) }; ColdDeliveryCard(Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Text("⚠", color = DeepRed, fontSize = 20.sp); Spacer(Modifier.width(8.dp)); Text(stringResource(com.colddelivery.app.R.string.stock_alert), color = DeepRed, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(stringResource(com.colddelivery.app.R.string.products_need_attention, summaries.count { it.value.sumOf(StockBatchEntity::remainingQty) <= threshold }), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) } }; LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) { items(products, key = { it.id }) { product -> val batches = summaries[product.id].orEmpty(); val remaining = batches.sumOf { it.remainingQty }; val oldest = batches.minOfOrNull { it.stockInDate }; ColdDeliveryCard(Modifier.fillMaxWidth().clickable { onOpen(product.id) }) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(product.productCode, color = Gold, fontSize = 11.sp); Text(stringResource(if (remaining == 0) com.colddelivery.app.R.string.zero_stock else if (remaining <= threshold) com.colddelivery.app.R.string.low_stock else com.colddelivery.app.R.string.available), color = if (remaining <= threshold) DeepRed else DeliveredGreen, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }; Text(product.productName, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold); Text(stringResource(com.colddelivery.app.R.string.stock_summary, remaining, batches.size, oldest?.let(::formatDay) ?: "—"), color = if (remaining == 0) DeepRed else ColdDeliveryColors.SecondaryText, fontSize = 10.sp) } } } } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HistoryScreen(onOpen: (Long) -> Unit = {}, vm: FeatureViewModel = hiltViewModel()) {
    var section by rememberSaveable { mutableStateOf("delivery") }
    if (section == "delivery") DeliveryHistoryContent(onOpen, vm) { section = "stock" } else StockBalanceContent(vm) { section = "delivery" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DeliveryHistoryContent(onOpen: (Long) -> Unit, vm: FeatureViewModel, onStockBalance: () -> Unit) {
    val deliveries by vm.deliveries.collectAsState(); val customers by vm.customers.collectAsState(); val products by vm.products.collectAsState(); var range by rememberSaveable { mutableStateOf("All") }; var dateText by rememberSaveable { mutableStateOf("") }; var customerId by rememberSaveable { mutableStateOf<Long?>(null) }; var productId by rememberSaveable { mutableStateOf<Long?>(null) }; var day by rememberSaveable { mutableStateOf<DeliveryDay?>(null) }; var customerMenu by remember { mutableStateOf(false) }; var productMenu by remember { mutableStateOf(false) }; var dayMenu by remember { mutableStateOf(false) }; var itemProducts by remember { mutableStateOf<Map<Long, List<Long>>>(emptyMap()) }
    LaunchedEffect(deliveries) { itemProducts = deliveries.associate { it.id to vm.items(it.id).map { item -> item.productId } } }
    val today = java.time.LocalDate.now().toEpochDay(); val exactDate = dateText.toLongOrNull(); val filtered = deliveries.filter { delivery -> val dateOk = when (range) { "Today" -> delivery.deliveryDate == today; "Week" -> delivery.deliveryDate in (today - 6)..today; "Month" -> delivery.deliveryDate in (today - 30)..today; "Date" -> exactDate == delivery.deliveryDate; else -> true }; dateOk && (customerId == null || customerId == delivery.customerId) && (productId == null || itemProducts[delivery.id].orEmpty().contains(productId)) && (day == null || customers.firstOrNull { it.id == delivery.customerId }?.deliveryDay == day) }
    PremiumPage {
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.history), stringResource(com.colddelivery.app.R.string.history_subtitle))
        HistorySectionTabs(selectedStock = false, onDelivery = {}, onStock = onStockBalance)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("All", "Today", "Week", "Month", "Date").forEach { value ->
                HistoryPill(value, range == value, Modifier.weight(1f)) { range = value }
            }
        }
        if (range == "Date") {
                HistoryFilterPill("▣  ${if (dateText.isBlank()) stringResource(com.colddelivery.app.R.string.history_select_date) else dateText}", selected = dateText.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                dateText = dateText.filter(Char::isDigit)
            }
            OutlinedTextField(dateText, { dateText = it.filter(Char::isDigit) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), label = { Text(stringResource(com.colddelivery.app.R.string.history_date_epoch), fontSize = 11.sp) }, singleLine = true, shape = RoundedCornerShape(16.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.weight(1f)) {
                HistoryFilterPill(customers.firstOrNull { it.id == customerId }?.name ?: stringResource(com.colddelivery.app.R.string.history_customer), customerId != null, Modifier.fillMaxWidth()) { customerMenu = true }
                DropdownMenu(customerMenu, { customerMenu = false }) { DropdownMenuItem(text = { Text(stringResource(com.colddelivery.app.R.string.history_all_customers)) }, onClick = { customerId = null; customerMenu = false }); customers.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { customerId = c.id; customerMenu = false }) } }
            }
            Box(Modifier.weight(1f)) {
                HistoryFilterPill(products.firstOrNull { it.id == productId }?.productName ?: stringResource(com.colddelivery.app.R.string.history_product), productId != null, Modifier.fillMaxWidth()) { productMenu = true }
                DropdownMenu(productMenu, { productMenu = false }) { DropdownMenuItem(text = { Text(stringResource(com.colddelivery.app.R.string.history_all_products)) }, onClick = { productId = null; productMenu = false }); products.forEach { p -> DropdownMenuItem(text = { Text("${p.productCode} ${p.productName}") }, onClick = { productId = p.id; productMenu = false }) } }
            }
            Box(Modifier.weight(1f)) {
                HistoryFilterPill(day?.name ?: stringResource(com.colddelivery.app.R.string.history_delivery_day), day != null, Modifier.fillMaxWidth()) { dayMenu = true }
                DropdownMenu(dayMenu, { dayMenu = false }) { DropdownMenuItem(text = { Text(stringResource(com.colddelivery.app.R.string.history_all_days)) }, onClick = { day = null; dayMenu = false }); DeliveryDay.values().forEach { d -> DropdownMenuItem(text = { Text(d.name) }, onClick = { day = d; dayMenu = false }) } }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HistorySummaryCard(stringResource(com.colddelivery.app.R.string.history_total_deliveries), filtered.size.toString(), Modifier.weight(1f))
            HistorySummaryCard(stringResource(com.colddelivery.app.R.string.history_total_cartons), filtered.sumOf { it.totalQty }.toString(), Modifier.weight(1f))
            HistorySummaryCard(stringResource(com.colddelivery.app.R.string.history_total_amount), formatMoney(filtered.sumOf { it.totalAmount }), Modifier.weight(1f), compact = true)
        }
        Text(stringResource(com.colddelivery.app.R.string.history_records), color = ColdDeliveryColors.Charcoal, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        if (filtered.isEmpty()) {
            ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("▣", color = Gold, fontSize = 28.sp); Text(stringResource(com.colddelivery.app.R.string.history_empty), color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.SemiBold); Text(stringResource(com.colddelivery.app.R.string.history_empty_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) } }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(filtered, key = { it.id }) { delivery ->
                    val customer = customers.firstOrNull { it.id == delivery.customerId }
                    ColdDeliveryCard(Modifier.fillMaxWidth().clickable { onOpen(delivery.id) }) {
                        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(14.dp), color = DeepRed.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Text("▣", color = DeepRed, fontSize = 21.sp) } }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(customer?.name ?: stringResource(com.colddelivery.app.R.string.delivery_label), fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(formatDay(delivery.deliveryDate), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp)
                                Text("${itemProducts[delivery.id].orEmpty().size} ${stringResource(com.colddelivery.app.R.string.history_items)}  •  ${delivery.totalQty} CTN", color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp)
                                Row(Modifier.padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    itemProducts[delivery.id].orEmpty().take(3).forEach { productIdForTag ->
                                        val product = products.firstOrNull { it.id == productIdForTag }
                                        Surface(shape = RoundedCornerShape(50), color = Gold.copy(alpha = .13f)) { Text(product?.productName ?: stringResource(com.colddelivery.app.R.string.product), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = ColdDeliveryColors.SecondaryText, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatMoney(delivery.totalAmount), color = ColdDeliveryColors.Charcoal, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(Modifier.height(5.dp)); StatusChip(delivery.status == DeliveryStatus.DELIVERED); Text("›", color = DeepRed, fontSize = 21.sp, modifier = Modifier.padding(top = 1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun HistorySectionTabs(selectedStock: Boolean, onDelivery: () -> Unit, onStock: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        HistoryPill(stringResource(com.colddelivery.app.R.string.history_delivery_history), !selectedStock, Modifier.weight(1f), onDelivery)
        HistoryPill(stringResource(com.colddelivery.app.R.string.history_stock_balance), selectedStock, Modifier.weight(1f), onStock)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun StockBalanceContent(vm: FeatureViewModel, onDelivery: () -> Unit) {
    val products by vm.products.collectAsState()
    var selectedDate by rememberSaveable { mutableStateOf(vm.today) }
    var query by rememberSaveable { mutableStateOf("") }
    var balances by remember { mutableStateOf<Map<Long, com.colddelivery.app.core.history.HistoricalStockBalance>>(emptyMap()) }
    var selectedProduct by remember { mutableStateOf<Long?>(null) }
    var pickerOpen by remember { mutableStateOf(false) }
    val visibleProducts = products.filter { query.isBlank() || it.productName.contains(query, true) || it.productCode.contains(query, true) }
    LaunchedEffect(products, selectedDate) {
        balances = products.associate { product -> product.id to vm.historicalStock(product.id, selectedDate) }
    }
    val date = LocalDate.ofEpochDay(selectedDate)
    if (selectedProduct != null) {
        StockDetailContent(vm, products.firstOrNull { it.id == selectedProduct }, selectedDate) { selectedProduct = null }
    } else {
        PremiumPage {
            PremiumPageTitle(stringResource(com.colddelivery.app.R.string.history_stock_balance), stringResource(com.colddelivery.app.R.string.history_subtitle))
            HistorySectionTabs(selectedStock = true, onDelivery = onDelivery, onStock = {})
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                HistoryPill(stringResource(com.colddelivery.app.R.string.history_previous_day), false, Modifier.weight(.30f)) { if (selectedDate > Long.MIN_VALUE) selectedDate-- }
                HistoryFilterPill(formatDay(selectedDate), true, Modifier.weight(1f)) { pickerOpen = true }
                HistoryPill(stringResource(com.colddelivery.app.R.string.history_next_day), false, Modifier.weight(.30f)) { if (selectedDate < vm.today) selectedDate++ }
            }
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().heightIn(min = 48.dp), label = { Text(stringResource(com.colddelivery.app.R.string.history_search_product), fontSize = 11.sp) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistorySummaryCard(stringResource(com.colddelivery.app.R.string.history_total_products), visibleProducts.size.toString(), Modifier.weight(1f))
                HistorySummaryCard(stringResource(com.colddelivery.app.R.string.history_total_stock), visibleProducts.sumOf { balances[it.id]?.closingStock ?: 0 }.toString() + " CTN", Modifier.weight(1f))
            }
            if (visibleProducts.isEmpty()) {
                ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("▣", color = Gold, fontSize = 28.sp); Text(stringResource(com.colddelivery.app.R.string.history_no_stock_records), color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.SemiBold) } }
            } else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(visibleProducts, key = { it.id }) { product ->
                    val balance = balances[product.id] ?: com.colddelivery.app.core.history.HistoricalStockBalance(product.id, 0, 0, 0)
                    ColdDeliveryCard(Modifier.fillMaxWidth().clickable { selectedProduct = product.id }) { Column(Modifier.padding(13.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(product.productCode, color = Gold, fontSize = 11.sp); Text(if (balance.closingStock == 0) "0 CTN" else "${balance.closingStock} CTN", color = if (balance.closingStock == 0) DeepRed else ColdDeliveryColors.Charcoal, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        Text(product.productName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(com.colddelivery.app.R.string.history_closing_stock), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp)
                        Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("${stringResource(com.colddelivery.app.R.string.history_opening_stock)} ${balance.openingStock} CTN", fontSize = 9.sp, color = ColdDeliveryColors.SecondaryText); Text("+${balance.stockIn} / -${balance.delivered}", fontSize = 9.sp, color = Gold) }
                    } }
                }
            }
        }
    }
    if (pickerOpen) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= LocalDate.ofEpochDay(vm.today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        })
        DatePickerDialog(onDismissRequest = { pickerOpen = false }, confirmButton = { TextButton({ pickerState.selectedDateMillis?.let { selectedDate = java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay() }; pickerOpen = false }) { Text(stringResource(com.colddelivery.app.R.string.save_changes)) } }, dismissButton = { TextButton({ pickerOpen = false }) { Text(stringResource(com.colddelivery.app.R.string.back)) } }) { DatePicker(state = pickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun StockDetailContent(vm: FeatureViewModel, product: ProductEntity?, date: Long, onBack: () -> Unit) {
    var movements by remember { mutableStateOf<Pair<List<StockBatchEntity>, List<com.colddelivery.app.data.local.dao.HistoricalDeliveryMovement>>?>(null) }
    var balance by remember { mutableStateOf<com.colddelivery.app.core.history.HistoricalStockBalance?>(null) }
    LaunchedEffect(product?.id, date) { if (product != null) { movements = vm.historicalMovements(product.id, date); balance = vm.historicalStock(product.id, date) } }
    PremiumPage {
        TextButton(onClick = onBack) { Text("‹  ${stringResource(com.colddelivery.app.R.string.back)}", color = DeepRed) }
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.history_stock_detail), product?.productName ?: stringResource(com.colddelivery.app.R.string.product))
        product?.let { p ->
            Text("${p.productCode}  •  ${formatDay(date)}", color = Gold, fontSize = 11.sp)
            val data = movements
            if (data == null) Text(stringResource(com.colddelivery.app.R.string.history_no_movement), color = ColdDeliveryColors.SecondaryText)
            else {
                val stockIn = data.first.sumOf { it.initialQty }; val delivered = data.second.sumOf { it.quantity }
                val opening = balance?.openingStock ?: 0
                ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("${stringResource(com.colddelivery.app.R.string.history_opening_stock)}  —  CTN", fontSize = 12.sp); Text("+ $stockIn CTN  ${stringResource(com.colddelivery.app.R.string.history_stock_in_detail)}", fontSize = 12.sp); Text("- $delivered CTN  ${stringResource(com.colddelivery.app.R.string.history_delivery_out)}", fontSize = 12.sp); Text("= ${opening + stockIn - delivered} CTN  ${stringResource(com.colddelivery.app.R.string.history_closing_stock)}", color = DeepRed, fontWeight = FontWeight.Bold) } }
                Text(stringResource(com.colddelivery.app.R.string.history_stock_in_detail), color = DeepRed, fontWeight = FontWeight.Bold)
                data.first.forEach { batch -> Text("${formatDay(batch.stockInDate)}  •  ${batch.initialQty} CTN", color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) }
                Text(stringResource(com.colddelivery.app.R.string.history_delivery_out), color = DeepRed, fontWeight = FontWeight.Bold)
                data.second.forEach { move -> Text("${move.customerName}  •  ${move.quantity} CTN", color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) }
            }
        }
    }
}
@Composable private fun HistoryPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(50), color = if (selected) DeepRed else ColdDeliveryColors.Cream, border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .28f)), shadowElevation = if (selected) 2.dp else 0.dp) {
        Box(Modifier.height(38.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text(text, color = if (selected) Color.White else ColdDeliveryColors.Charcoal, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1) }
    }
}

@Composable private fun HistoryFilterPill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(50), color = if (selected) Gold.copy(alpha = .16f) else ColdDeliveryColors.Cream, border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Gold.copy(alpha = .65f) else Gold.copy(alpha = .25f))) {
        Box(Modifier.height(34.dp).fillMaxWidth().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) { Text(text, color = if (selected) DeepRed else ColdDeliveryColors.SecondaryText, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    }
}

@Composable private fun HistorySummaryCard(label: String, value: String, modifier: Modifier = Modifier, compact: Boolean = false) {
    ColdDeliveryCard(modifier.height(82.dp)) { Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 11.dp), verticalArrangement = Arrangement.SpaceBetween) { Text(value.substringBefore(" MMK"), color = DeepRed, fontSize = if (compact) 13.sp else 22.sp, fontWeight = FontWeight.Bold, maxLines = 1); if (compact) Text(stringResource(com.colddelivery.app.R.string.mmk), color = Gold, fontSize = 10.sp) else Text(label, color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp, maxLines = 2) } }
}
