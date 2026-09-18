package com.colddelivery.app.ui.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.colddelivery.app.core.backup.BackupManager
import com.colddelivery.app.core.delivery.customersScheduledForToday
import com.colddelivery.app.core.delivery.deliveryDayFor
import com.colddelivery.app.data.local.entity.*
import com.colddelivery.app.ui.auth.AuthViewModel
import com.colddelivery.app.ui.components.*
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.Intent
import android.net.Uri

private fun money(v: Long) = "%,d MMK".format(v)
private fun day(v: Long) = LocalDate.ofEpochDay(v).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

@Composable private fun Field(value: String, change: (String) -> Unit, label: String, type: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier) {
    OutlinedTextField(value, change, modifier.fillMaxWidth(), label = { Text(label, fontSize = 12.sp) }, keyboardOptions = KeyboardOptions(keyboardType = type), singleLine = true, shape = com.colddelivery.app.ui.theme.ColdDeliveryShapes.Input)
}
@Composable private fun Select(label: String, value: String, menu: @Composable ColumnScope.() -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box { OutlinedButton({ open = true }, Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = com.colddelivery.app.ui.theme.ColdDeliveryShapes.Input) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp); Text(value, fontSize = 12.sp) } }; DropdownMenu(open, { open = false }) { Column { menu() } } }
}
@Composable private fun Label(text: String) { Text(text, color = DeepRed, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

@Composable private fun SectionHeading(text: String, modifier: Modifier = Modifier) { Text(text, modifier, color = DeepRed, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp) }

@Composable private fun CompactNumberField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, suffix: String = "CTN") {
    Surface(modifier, shape = RoundedCornerShape(13.dp), color = ColdDeliveryColors.Cream, border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .35f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value, { onValueChange(it.filter(Char::isDigit)) }, Modifier.width(55.dp), textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColdDeliveryColors.Charcoal), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Text(suffix, color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp)
        }
    }
}

@Composable private fun PriceField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(13.dp), color = ColdDeliveryColors.Cream, border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .38f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value, { onValueChange(it.filter(Char::isDigit)) }, Modifier.weight(1f), textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColdDeliveryColors.Charcoal), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Text(stringResource(com.colddelivery.app.R.string.mmk), color = Gold, fontSize = 10.sp)
        }
    }
}

@Composable fun PriceScreen(vm: FeatureViewModel = hiltViewModel()) {
    val products by vm.products.collectAsState(); val prices by vm.prices().collectAsState(initial = emptyList()); val values = remember { mutableStateMapOf<Long, String>() }; LaunchedEffect(prices) { prices.forEach { values[it.productId] = it.price.toString() } }
    PremiumPage {
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.todays_price), stringResource(com.colddelivery.app.R.string.todays_price_subtitle))
        ColdDeliveryCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text("▣", color = Gold, fontSize = 23.sp); Spacer(Modifier.width(10.dp)); Column { Text(day(vm.today), color = ColdDeliveryColors.Charcoal, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }, color = DeepRed, fontSize = 11.sp) } } }
        ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(stringResource(com.colddelivery.app.R.string.todays_price_info_title), color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(stringResource(com.colddelivery.app.R.string.todays_price_info), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp, lineHeight = 15.sp) } }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(com.colddelivery.app.R.string.product_code), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(stringResource(com.colddelivery.app.R.string.product_name), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(stringResource(com.colddelivery.app.R.string.price_mmk), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(products, key = { it.id }) { p -> ColdDeliveryCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.width(62.dp)) { Text(p.productCode, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold) }; Column(Modifier.weight(1f).padding(horizontal = 6.dp)) { Text(p.productName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2); Text(stringResource(com.colddelivery.app.R.string.carton), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp) }; PriceField(values[p.id] ?: "", { values[p.id] = it }, Modifier.width(128.dp)) } } } }
        PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_todays_prices), { vm.viewModelScopeLaunch { products.forEach { p -> values[p.id]?.toLongOrNull()?.let { vm.savePrice(p.id, vm.today, it) } } } }, Modifier.fillMaxWidth())
    }
}

@Composable fun NewDeliveryScreen(onSaved: (Long) -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val customers by vm.customers.collectAsState(); val products by vm.products.collectAsState(); val prices by vm.prices().collectAsState(initial = emptyList()); var error by remember { mutableStateOf<String?>(null) }; val draft = remember { NewDeliveryDraftState() }; val scope = rememberCoroutineScope(); var open by remember { mutableStateOf(false) }; val todayDate = LocalDate.ofEpochDay(vm.today); val todayDeliveryDay = deliveryDayFor(todayDate); val todayCustomers = customersScheduledForToday(customers, todayDate); val isClosedDay = todayDeliveryDay == null; val closedDayMessage = stringResource(com.colddelivery.app.R.string.closed_day); val noCustomersMessage = stringResource(com.colddelivery.app.R.string.no_customers_scheduled_today); val customer = todayCustomers.firstOrNull { it.id == draft.customerId }; val totals = draft.totals(prices.associate { it.productId to it.price }); val selectCustomerError = stringResource(com.colddelivery.app.R.string.select_customer_error); val addProductError = stringResource(com.colddelivery.app.R.string.add_product_error); val saveDeliveryFailed = stringResource(com.colddelivery.app.R.string.save_delivery_failed)
    Column(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState()).padding(horizontal = 17.dp, vertical = 10.dp).padding(bottom = 90.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.new_delivery), stringResource(com.colddelivery.app.R.string.new_delivery_subtitle))
        var customerOpen by remember { mutableStateOf(false) }
        Box {
            ColdDeliveryCard(Modifier.fillMaxWidth().clickable(enabled = !isClosedDay && todayCustomers.isNotEmpty()) { customerOpen = true }) { Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Surface(Modifier.size(42.dp), shape = RoundedCornerShape(14.dp), color = DeepRed.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Text("▣", color = DeepRed, fontSize = 22.sp) } }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(customer?.name ?: stringResource(if (isClosedDay) com.colddelivery.app.R.string.closed_day else com.colddelivery.app.R.string.select_customer), fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(customer?.phone ?: stringResource(if (isClosedDay || todayCustomers.isEmpty()) com.colddelivery.app.R.string.no_customers_scheduled_today else com.colddelivery.app.R.string.customer_details_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); customer?.let { Text(it.address, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp, maxLines = 1) } }; StatusChip(false); if (!isClosedDay && todayCustomers.isNotEmpty()) Text("›", color = Gold, fontSize = 21.sp, modifier = Modifier.padding(start = 4.dp)) } }
            DropdownMenu(customerOpen, { customerOpen = false }) { todayCustomers.forEach { c -> DropdownMenuItem(text = { Column { Text(c.name); Text(c.phone, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(c.address, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp, maxLines = 1) } }, onClick = { draft.selectCustomer(c.id); error = null; customerOpen = false }) } }
        }
        ColdDeliveryCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text("▣", color = Gold, fontSize = 22.sp); Spacer(Modifier.width(9.dp)); Column { Text(stringResource(com.colddelivery.app.R.string.delivery_date), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(day(vm.today), color = ColdDeliveryColors.Charcoal, fontSize = 14.sp, fontWeight = FontWeight.Bold) } } }
        SectionHeading(stringResource(com.colddelivery.app.R.string.delivery_items))
        error?.let { Text(it, color = DeepRed, fontSize = 11.sp) }
        if (draft.items.isEmpty()) ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("▣", color = Gold, fontSize = 24.sp); Text(stringResource(com.colddelivery.app.R.string.no_products_added), color = ColdDeliveryColors.Charcoal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(stringResource(com.colddelivery.app.R.string.add_products_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) } } else LazyColumn(Modifier.fillMaxWidth().heightIn(max = 235.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(draft.items, key = { it.productId }) { line -> val p = products.firstOrNull { it.id == line.productId }; val price = prices.firstOrNull { it.productId == line.productId }?.price ?: 0; ColdDeliveryCard { Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(p?.productName ?: stringResource(com.colddelivery.app.R.string.product), fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1); Text(p?.productCode ?: "", color = Gold, fontSize = 9.sp) }; Column(Modifier.width(78.dp)) { Text(stringResource(com.colddelivery.app.R.string.today_price), color = ColdDeliveryColors.SecondaryText, fontSize = 8.sp); Text(if (price > 0) money(price) else stringResource(com.colddelivery.app.R.string.price_missing), color = if (price > 0) ColdDeliveryColors.Charcoal else DeepRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }; Column(Modifier.width(88.dp), horizontalAlignment = Alignment.End) { Text(stringResource(com.colddelivery.app.R.string.amount), color = ColdDeliveryColors.SecondaryText, fontSize = 8.sp); Text(money(line.quantity.toLong() * price), color = DeepRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1) } }; Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(stringResource(com.colddelivery.app.R.string.qty), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp); Spacer(Modifier.width(8.dp)); CompactNumberField(line.quantityText, { v -> draft.updateQuantity(line.productId, v); error = null }, suffix = stringResource(com.colddelivery.app.R.string.ctn)); Spacer(Modifier.weight(1f)); IconButton(onClick = { draft.removeProduct(line.productId); error = null }, modifier = Modifier.size(34.dp)) { Text("×", color = DeepRed, fontSize = 21.sp) } } } } } }
        Box(Modifier.fillMaxWidth()) { PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.add_product), { open = true }, Modifier.fillMaxWidth()); DropdownMenu(open, { open = false }) { products.forEach { p -> DropdownMenuItem(text = { Text("${p.productCode}  ${p.productName}") }, enabled = draft.items.none { it.productId == p.id }, onClick = { draft.addProduct(p.id); error = null; open = false }) } } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard(stringResource(com.colddelivery.app.R.string.total_qty), "${totals.quantity} ${stringResource(com.colddelivery.app.R.string.ctn)}", Modifier.weight(1f)); MetricCard(stringResource(com.colddelivery.app.R.string.total_amount), money(totals.amount), Modifier.weight(1f)) }
        PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_delivery), { scope.launch { try { if (isClosedDay) error(closedDayMessage); if (todayCustomers.isEmpty()) error(noCustomersMessage); if (draft.customerId == 0L) error(selectCustomerError); if (draft.items.isEmpty()) error(addProductError); onSaved(vm.saveDelivery(draft.customerId, vm.today, draft.saveItems())) } catch (t: Throwable) { error = t.message ?: saveDeliveryFailed } } }, Modifier.fillMaxWidth())
    }
}

@Composable fun AddCustomerScreen(onSaved: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    var name by rememberSaveable { mutableStateOf("") }; var phone by rememberSaveable { mutableStateOf("") }; var address by rememberSaveable { mutableStateOf("") }; var note by rememberSaveable { mutableStateOf("") }; var selected by rememberSaveable { mutableStateOf(DeliveryDay.MONDAY) }; val scope = rememberCoroutineScope()
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.add_customer_title), stringResource(com.colddelivery.app.R.string.add_customer_subtitle)); ColdDeliveryCard { Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Field(name, { name = it }, stringResource(com.colddelivery.app.R.string.name_required)); Field(phone, { phone = it }, stringResource(com.colddelivery.app.R.string.phone), KeyboardType.Phone); Field(address, { address = it }, stringResource(com.colddelivery.app.R.string.address)); Select(stringResource(com.colddelivery.app.R.string.delivery_day), selected.name) { DeliveryDay.values().forEach { d -> DropdownMenuItem(text = { Text(d.name) }, onClick = { selected = d }) } }; Field(note, { note = it }, stringResource(com.colddelivery.app.R.string.note)) } }; Spacer(Modifier.weight(1f)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_customer), { scope.launch { if (name.isNotBlank()) { vm.addCustomer(CustomerEntity(name = name, phone = phone, address = address, deliveryDay = selected, note = note, createdAt = System.currentTimeMillis())); onSaved() } } }, Modifier.fillMaxWidth()) }
}

@Composable fun CustomerDetailScreen(customerId: Long, onSaved: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val customers by vm.customers.collectAsState(); val current = customers.firstOrNull { it.id == customerId } ?: return; var name by remember(current) { mutableStateOf(current.name) }; var phone by remember(current) { mutableStateOf(current.phone) }; var address by remember(current) { mutableStateOf(current.address) }; var note by remember(current) { mutableStateOf(current.note) }; var selected by remember(current) { mutableStateOf(current.deliveryDay) }; val scope = rememberCoroutineScope()
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.customer_detail), stringResource(com.colddelivery.app.R.string.customer_detail_subtitle)); ColdDeliveryCard { Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Field(name, { name = it }, stringResource(com.colddelivery.app.R.string.name)); Field(phone, { phone = it }, stringResource(com.colddelivery.app.R.string.phone), KeyboardType.Phone); Field(address, { address = it }, stringResource(com.colddelivery.app.R.string.address)); Select(stringResource(com.colddelivery.app.R.string.delivery_day), selected.name) { DeliveryDay.values().forEach { d -> DropdownMenuItem(text = { Text(d.name) }, onClick = { selected = d }) } }; Field(note, { note = it }, stringResource(com.colddelivery.app.R.string.note)) } }; Text(stringResource(com.colddelivery.app.R.string.sort_order, current.sortOrder), color = Gold, fontSize = 11.sp); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.move_up), { scope.launch { vm.moveCustomer(customerId, -1) } }, Modifier.weight(1f)); PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.move_down), { scope.launch { vm.moveCustomer(customerId, 1) } }, Modifier.weight(1f)) }; Spacer(Modifier.weight(1f)); TextButton({ scope.launch { vm.updateCustomer(current.copy(isActive = false)); onSaved() } }) { Text(stringResource(com.colddelivery.app.R.string.archive_customer), color = DeepRed, fontSize = 11.sp) }; PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_changes), { scope.launch { vm.updateCustomer(current.copy(name = name, phone = phone, address = address, note = note, deliveryDay = selected)); onSaved() } }, Modifier.fillMaxWidth()) }
}

@Composable fun AddProductScreen(onSaved: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var code by rememberSaveable { mutableStateOf("") }; var name by rememberSaveable { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope()
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.add_product_title), stringResource(com.colddelivery.app.R.string.add_product_subtitle)); ColdDeliveryCard { Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Field(code, { code = it }, stringResource(com.colddelivery.app.R.string.product_code_required)); Field(name, { name = it }, stringResource(com.colddelivery.app.R.string.product_name_required)); Text(stringResource(com.colddelivery.app.R.string.unit_carton), color = Gold, fontSize = 11.sp); error?.let { Text(it, color = DeepRed, fontSize = 11.sp) } } }; Spacer(Modifier.weight(1f)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_product), { scope.launch { try { if (code.isBlank() || name.isBlank()) error(context.getString(com.colddelivery.app.R.string.code_name_required)) else { vm.addProduct(ProductEntity(productCode = code, productName = name, createdAt = System.currentTimeMillis())); onSaved() } } catch (t: Throwable) { error = context.getString(com.colddelivery.app.R.string.product_code_unique) } } }, Modifier.fillMaxWidth()) }
}

@Composable fun StockInScreen(onSaved: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val products by vm.products.collectAsState(); var selected by remember { mutableStateOf<ProductEntity?>(null) }; var qty by rememberSaveable { mutableStateOf("") }; val scope = rememberCoroutineScope()
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.stock_in), stringResource(com.colddelivery.app.R.string.add_product_subtitle)); ColdDeliveryCard { Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Select(stringResource(com.colddelivery.app.R.string.select_product_label), selected?.productName ?: stringResource(com.colddelivery.app.R.string.select_product)) { products.forEach { p -> DropdownMenuItem(text = { Text(p.productName) }, onClick = { selected = p }) } }; Field(qty, { qty = it.filter(Char::isDigit) }, stringResource(com.colddelivery.app.R.string.quantity_ctn), KeyboardType.Number); Text(stringResource(com.colddelivery.app.R.string.stock_in_date, day(vm.today)), color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) } }; Spacer(Modifier.weight(1f)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_stock_in), { scope.launch { if (selected != null && qty.toIntOrNull() ?: 0 > 0) { vm.stockIn(StockBatchEntity(productId = selected!!.id, stockInDate = vm.today, initialQty = qty.toInt(), remainingQty = qty.toInt(), createdAt = System.currentTimeMillis())); onSaved() } } }, Modifier.fillMaxWidth()) }
}

@Composable fun ProductDetailScreen(productId: Long, vm: FeatureViewModel = hiltViewModel()) {
    val products by vm.products.collectAsState(); val product = products.firstOrNull { it.id == productId }; var total by remember { mutableStateOf(0) }; var batches by remember { mutableStateOf(emptyList<StockBatchEntity>()) }; LaunchedEffect(productId) { total = vm.stockTotal(productId); batches = vm.stockBatches(productId) }
    PremiumPage { PremiumPageTitle(product?.productName ?: stringResource(com.colddelivery.app.R.string.product), product?.productCode ?: ""); ColdDeliveryCard { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(stringResource(com.colddelivery.app.R.string.total_stock), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text("$total CTN", color = if (total == 0) DeepRed else DeliveredGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold) }; Text(stringResource(com.colddelivery.app.R.string.fifo_oldest_first), color = Gold, fontSize = 11.sp) } }; Text(stringResource(com.colddelivery.app.R.string.fifo_batches), color = DeepRed, fontWeight = FontWeight.Bold); LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(batches, key = { it.id }) { b -> ColdDeliveryCard { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(com.colddelivery.app.R.string.stock_in_date_value, day(b.stockInDate)), fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(stringResource(if (b.remainingQty == 0) com.colddelivery.app.R.string.consumed else com.colddelivery.app.R.string.active), color = if (b.remainingQty == 0) DeepRed else DeliveredGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }; Text(stringResource(com.colddelivery.app.R.string.batch_quantities, b.initialQty, b.remainingQty), color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) } } } } }
}

@Composable fun EditDeliveryScreen(deliveryId: Long, onSaved: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val products by vm.products.collectAsState(); var customerName by remember { mutableStateOf("") }; var lines by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope(); LaunchedEffect(deliveryId) { vm.delivery(deliveryId)?.let { d -> customerName = vm.customer(d.customerId)?.name ?: ""; lines = vm.items(deliveryId).map { it.productId to it.qty.toString() } } }
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.edit_delivery), stringResource(com.colddelivery.app.R.string.edit_delivery_subtitle)); ColdDeliveryCard { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(customerName, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(stringResource(com.colddelivery.app.R.string.delivery_number, deliveryId), color = Gold, fontSize = 11.sp) } }; error?.let { Text(it, color = DeepRed, fontSize = 11.sp) }; LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(lines, key = { it.first }) { line -> ColdDeliveryCard { Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) { Text(products.firstOrNull { it.id == line.first }?.productName ?: stringResource(com.colddelivery.app.R.string.product), Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold); Field(line.second, { v -> lines = lines.map { if (it.first == line.first) line.first to v.filter(Char::isDigit) else it } }, stringResource(com.colddelivery.app.R.string.ctn_short), KeyboardType.Number, Modifier.width(108.dp)); TextButton({ lines = lines.filterNot { it.first == line.first } }) { Text(stringResource(com.colddelivery.app.R.string.remove), color = DeepRed, fontSize = 10.sp) } } } } }; TextButton({ products.firstOrNull { p -> lines.none { it.first == p.id } }?.let { lines = lines + (it.id to "1") } }) { Text(stringResource(com.colddelivery.app.R.string.add_product), color = DeepRed, fontSize = 12.sp) }; PrimaryRedButton(stringResource(com.colddelivery.app.R.string.save_changes_delivery), { scope.launch { try { vm.edit(deliveryId, lines.map { it.first to (it.second.toIntOrNull() ?: 0) }); onSaved() } catch (t: Throwable) { error = t.message ?: context.getString(com.colddelivery.app.R.string.could_not_edit_delivery) } } }, Modifier.fillMaxWidth()) }
}

@Composable fun VoucherScreen(deliveryId: Long, onUndo: () -> Unit, onEdit: () -> Unit = {}, vm: FeatureViewModel = hiltViewModel()) {
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }; var delivery by remember { mutableStateOf<DeliveryEntity?>(null) }; var items by remember { mutableStateOf<List<DeliveryItemEntity>>(emptyList()) }; val products by vm.products.collectAsState(); val scope = rememberCoroutineScope(); LaunchedEffect(deliveryId) { vm.delivery(deliveryId)?.let { delivery = it; customer = vm.customer(it.customerId) }; items = vm.items(deliveryId) }; val totalQty = items.sumOf { it.qty }; val totalAmount = items.sumOf { it.amount }
    PremiumPage {
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.delivery_voucher), "#" + deliveryId)
        customer?.let { c -> ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(c.name, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(c.phone, color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp); Text(c.address, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp, maxLines = 2) }; StatusChip(delivery?.status == DeliveryStatus.DELIVERED) }; Text(delivery?.let { day(it.deliveryDate) } ?: "", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold) } } }
        SectionHeading(stringResource(com.colddelivery.app.R.string.item_details))
        ColdDeliveryCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(com.colddelivery.app.R.string.product), Modifier.weight(1f), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp); Text(stringResource(com.colddelivery.app.R.string.qty), Modifier.width(42.dp), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp); Text(stringResource(com.colddelivery.app.R.string.unit_price), Modifier.width(76.dp), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp); Text(stringResource(com.colddelivery.app.R.string.amount), Modifier.width(76.dp), color = ColdDeliveryColors.SecondaryText, fontSize = 9.sp) } }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) { items(items) { item -> ColdDeliveryCard { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(products.firstOrNull { it.id == item.productId }?.productName ?: stringResource(com.colddelivery.app.R.string.product), Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 2); Text("${item.qty}", Modifier.width(42.dp), fontSize = 11.sp); Text(money(item.unitPrice), Modifier.width(76.dp), fontSize = 10.sp); Text(money(item.amount), Modifier.width(76.dp), color = DeepRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) } } } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard(stringResource(com.colddelivery.app.R.string.total_qty), "$totalQty ${stringResource(com.colddelivery.app.R.string.ctn)}", Modifier.weight(1f)); MetricCard(stringResource(com.colddelivery.app.R.string.total_price), money(totalAmount), Modifier.weight(1f)) }
        ColdDeliveryCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(stringResource(com.colddelivery.app.R.string.note), color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp); Text(customer?.note?.takeIf { it.isNotBlank() } ?: stringResource(com.colddelivery.app.R.string.no_note), color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp) } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.edit), onEdit, Modifier.weight(1f)); PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.undo), { scope.launch { vm.undo(deliveryId); onUndo() } }, Modifier.weight(1f)) }
    }
}

@Composable private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) { ColdDeliveryCard(modifier.heightIn(min = 78.dp)) { Column(Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(label, color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); Text(value, color = DeepRed, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1) } } }

@Composable fun SettingsScreen(onLogin: () -> Unit, onBackup: () -> Unit = {}, vm: FeatureViewModel = hiltViewModel()) {
    val language by vm.language.collectAsState()
    val threshold by vm.lowStockThreshold.collectAsState()
    var input by remember(threshold) { mutableStateOf(threshold.toString()) }
    var developerOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    PremiumPage {
        PremiumPageTitle(stringResource(com.colddelivery.app.R.string.settings), stringResource(com.colddelivery.app.R.string.settings_subtitle))
        ColdDeliveryCard { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(stringResource(com.colddelivery.app.R.string.language), color = DeepRed, fontWeight = FontWeight.Bold); LanguageSegmentedControl(language, { vm.saveLanguage(it); LanguageManager.apply(context, it) }, Modifier.fillMaxWidth()); Text(stringResource(com.colddelivery.app.R.string.language_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) } }
        ColdDeliveryCard { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(stringResource(com.colddelivery.app.R.string.stock_alerts), color = DeepRed, fontWeight = FontWeight.Bold); Field(input, { input = it.filter(Char::isDigit) }, stringResource(com.colddelivery.app.R.string.low_stock_threshold), KeyboardType.Number); TextButton({ input.toIntOrNull()?.let(vm::saveThreshold) }) { Text(stringResource(com.colddelivery.app.R.string.save_threshold), color = DeepRed, fontSize = 11.sp) } } }
        ColdDeliveryCard { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(stringResource(com.colddelivery.app.R.string.backup_restore_section), color = DeepRed, fontWeight = FontWeight.Bold); Text(stringResource(com.colddelivery.app.R.string.backup_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.open_backup_restore), onBackup, Modifier.fillMaxWidth()) } }
        ColdDeliveryCard(Modifier.fillMaxWidth().clickable { developerOpen = true }) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(38.dp), shape = CircleShape, color = DeepRed.copy(alpha = .10f)) { Box(contentAlignment = Alignment.Center) { Text("♙", color = DeepRed, fontSize = 20.sp) } }
                Spacer(Modifier.width(10.dp))
                Column { Text(stringResource(com.colddelivery.app.R.string.developer), color = DeepRed, fontWeight = FontWeight.Bold); Text(stringResource(com.colddelivery.app.R.string.developer_profile), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) }
            }
        }
        ColdDeliveryCard { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(stringResource(com.colddelivery.app.R.string.app_version), color = ColdDeliveryColors.SecondaryText); Text("1.0", fontWeight = FontWeight.Bold) } }
        Spacer(Modifier.weight(1f))
        PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.logout), onLogin, Modifier.fillMaxWidth())
    }
    if (developerOpen) {
        Dialog(onDismissRequest = { developerOpen = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = ColdDeliveryColors.Ivory, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth(.88f)) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(com.colddelivery.app.R.string.developer_profile), color = DeepRed, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
                    IconButton(onClick = { developerOpen = false }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Default.Close, contentDescription = stringResource(com.colddelivery.app.R.string.close), tint = DeepRed) }
                    Image(painterResource(com.colddelivery.app.R.drawable.developer_profile), stringResource(com.colddelivery.app.R.string.developer_profile_image), Modifier.size(118.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Text("Zin Ko Lyn", color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(stringResource(com.colddelivery.app.R.string.app_developer), color = Gold, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:09699666060"))) }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(com.colddelivery.app.R.string.phone), color = ColdDeliveryColors.SecondaryText, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("09699666060", color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable fun BackupScreen(onBack: () -> Unit, vm: FeatureViewModel = hiltViewModel()) {
    val context = LocalContext.current; var status by remember { mutableStateOf<String?>(null) }; val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> if (uri != null) status = BackupManager(context, vm.databaseForBackup()).create(uri).fold({ context.getString(com.colddelivery.app.R.string.backup_created) }, { it.message ?: context.getString(com.colddelivery.app.R.string.backup_failed) }) }; val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) status = BackupManager(context, vm.databaseForBackup()).restore(uri).fold({ context.getString(com.colddelivery.app.R.string.restore_complete) }, { it.message ?: context.getString(com.colddelivery.app.R.string.restore_failed) }) }
    PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.backup_restore), stringResource(com.colddelivery.app.R.string.backup_subtitle)); ColdDeliveryCard { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(stringResource(com.colddelivery.app.R.string.backup_status), color = Gold, fontWeight = FontWeight.Bold); Text(status ?: stringResource(com.colddelivery.app.R.string.backup_none), color = ColdDeliveryColors.SecondaryText, fontSize = 11.sp); Text(stringResource(com.colddelivery.app.R.string.backup_portable_hint), color = ColdDeliveryColors.SecondaryText, fontSize = 10.sp) } }; PrimaryRedButton(stringResource(com.colddelivery.app.R.string.create_local_backup), { create.launch("cold_delivery_backup_" + System.currentTimeMillis() + ".zip") }, Modifier.fillMaxWidth()); PremiumOutlinedButton(stringResource(com.colddelivery.app.R.string.restore_local_backup), { restore.launch(arrayOf("application/zip")) }, Modifier.fillMaxWidth()); Text(stringResource(com.colddelivery.app.R.string.restore_restart_hint), color = DeepRed, fontSize = 10.sp); Spacer(Modifier.weight(1f)); TextButton(onBack) { Text(stringResource(com.colddelivery.app.R.string.back), color = DeepRed) } }
}

@Composable fun LoginScreen(onLoggedIn: () -> Unit, vm: FeatureViewModel = hiltViewModel()) { var username by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.app_name), stringResource(com.colddelivery.app.R.string.login_subtitle)); Field(username, { username = it }, stringResource(com.colddelivery.app.R.string.username)); Field(password, { password = it }, stringResource(com.colddelivery.app.R.string.password)); Spacer(Modifier.weight(1f)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.login), { if (username.isNotBlank() && password.isNotBlank()) onLoggedIn() }, Modifier.fillMaxWidth()) } }
@Composable fun AuthLoginScreen(onLoggedIn: () -> Unit, vm: AuthViewModel = hiltViewModel()) { var username by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var remember by rememberSaveable { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope(); PremiumPage { PremiumPageTitle(stringResource(com.colddelivery.app.R.string.app_name), stringResource(com.colddelivery.app.R.string.login_subtitle)); Field(username, { username = it }, stringResource(com.colddelivery.app.R.string.username)); Field(password, { password = it }, stringResource(com.colddelivery.app.R.string.password)); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(remember, { remember = it }); Text(stringResource(com.colddelivery.app.R.string.remember_me), fontSize = 12.sp) }; error?.let { Text(it, color = DeepRed) }; Spacer(Modifier.weight(1f)); PrimaryRedButton(stringResource(com.colddelivery.app.R.string.login), { scope.launch { if (vm.login(username, password, remember)) onLoggedIn() else error = vm.message.value } }, Modifier.fillMaxWidth()) } }
private fun FeatureViewModel.viewModelScopeLaunch(block: suspend () -> Unit) { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch { block() } }
