package com.colddelivery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.colddelivery.app.ui.theme.ColdDeliveryTheme
import com.colddelivery.app.ui.screens.*
import com.colddelivery.app.ui.feature.*
import com.colddelivery.app.ui.auth.AuthViewModel
import com.colddelivery.app.ui.auth.PremiumLoginScreen
import com.colddelivery.app.ui.auth.PremiumAccountSetupScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.colddelivery.app.data.preferences.readSavedLanguage
import com.colddelivery.app.ui.feature.LanguageManager
import dagger.hilt.android.AndroidEntryPoint
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import com.colddelivery.app.core.auth.shouldShowFirstRunSetup
import androidx.compose.ui.graphics.toArgb

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: android.content.Context) { super.attachBaseContext(LanguageManager.wrap(newBase, readSavedLanguage(newBase))) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor = ColdDeliveryColors.DeepRed.toArgb(); window.decorView.systemUiVisibility = 0; setContent { ColdDeliveryTheme { Surface { AuthGate() } } } }
}

@Composable private fun AuthGate(vm: AuthViewModel = hiltViewModel()) { val remembered by vm.rememberMe.collectAsState(); val user by vm.user.collectAsState(); var loggedIn by rememberSaveable { mutableStateOf(false) }; var setupComplete by rememberSaveable { mutableStateOf(false) }; val setupRequired = shouldShowFirstRunSetup(BuildConfig.DEBUG, user != null) && !setupComplete; when { remembered || loggedIn -> AppNavigation(onLogout = { vm.logout(); loggedIn = false }); setupRequired -> PremiumAccountSetupScreen(onCreated = { setupComplete = true }, vm = vm); else -> PremiumLoginScreen(onLoggedIn = { loggedIn = true }, vm = vm) } }

@Composable private fun AppNavigation(onLogout: () -> Unit = {}) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "home") {
        composable("home") { ShellScreen("Home", nav) { HomeMockupScreen(onNewDelivery = { nav.navigate("new_delivery") }, onPrice = { nav.navigate("prices") }) } }
        composable("customers") { ShellScreen("Customers", nav) { CustomersScreen(onAdd = { nav.navigate("add_customer") }, onOpen = { nav.navigate("customer/$it") }) } }
        composable("stock") { ShellScreen("Stock", nav) { StockScreen(onAddProduct = { nav.navigate("add_product") }, onStockIn = { nav.navigate("stock_in") }, onOpen = { nav.navigate("product/$it") }, onEdit = { nav.navigate("product_edit/$it") }, onTickets = { nav.navigate("stock_tickets") }) } }
        composable("history") { ShellScreen("History", nav) { HistoryScreen(onOpen = { nav.navigate("voucher/$it") }) } }
        composable("settings") { ShellScreen("Settings", nav) { SettingsScreen(onLogin = onLogout, onBackup = { nav.navigate("backup") }) } }
        composable("prices") { ShellScreen("Today's Price", nav) { PriceScreen() } }
        composable("new_delivery") { ShellScreen("New Delivery", nav) { NewDeliveryScreen(onSaved = { nav.navigate("voucher/$it") }) } }
        composable("add_customer") { ShellScreen("Add Customer", nav) { AddCustomerScreen(onSaved = { nav.popBackStack() }) } }
        composable("customer/{customerId}") { backStack -> ShellScreen("Customer Detail", nav) { CustomerDetailScreen(backStack.arguments?.getString("customerId")?.toLongOrNull() ?: 0L, onSaved = { nav.popBackStack() }) } }
        composable("add_product") { ShellScreen("Add Product", nav) { AddProductScreen(onSaved = { nav.popBackStack() }) } }
        composable("stock_in") { ShellScreen("Stock In Ticket", nav) { StockInScreen(onSaved = { nav.popBackStack() }) } }
        composable("stock_tickets") { ShellScreen("Stock In Tickets", nav) { StockInTicketsScreen(onOpen = { nav.navigate("stock_ticket/$it") }) } }
        composable("stock_ticket/{ticketId}") { backStack -> ShellScreen("Stock In Ticket", nav) { StockInTicketDetailScreen(backStack.arguments?.getString("ticketId")?.toLongOrNull() ?: 0L, onBack = { nav.popBackStack() }) } }
        composable("voucher/{deliveryId}") { backStack -> val id = backStack.arguments?.getString("deliveryId")?.toLongOrNull() ?: 0L; ShellScreen("Voucher", nav) { VoucherScreen(id, onUndo = { nav.popBackStack() }, onEdit = { nav.navigate("edit_delivery/$id") }) } }
        composable("edit_delivery/{deliveryId}") { backStack -> ShellScreen("Edit Delivery", nav) { EditDeliveryScreen(backStack.arguments?.getString("deliveryId")?.toLongOrNull() ?: 0L, onSaved = { nav.popBackStack() }) } }
        composable("product/{productId}") { backStack -> ShellScreen("Product Detail", nav) { ProductDetailScreen(backStack.arguments?.getString("productId")?.toLongOrNull() ?: 0L) } }
        composable("product_edit/{productId}") { backStack -> ShellScreen("Edit Product", nav) { ProductEditScreen(backStack.arguments?.getString("productId")?.toLongOrNull() ?: 0L, onSaved = { nav.popBackStack() }) } }
        composable("login") { ShellScreen("Login", nav) { LoginScreen(onLoggedIn = { nav.navigate("home") { popUpTo("login") { inclusive = true } } }) } }
        composable("backup") { ShellScreen("Backup", nav) { BackupScreen(onBack = { nav.popBackStack() }) } }
    }
}
