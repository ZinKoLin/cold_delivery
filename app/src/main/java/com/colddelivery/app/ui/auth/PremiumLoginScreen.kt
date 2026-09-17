package com.colddelivery.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colddelivery.app.ui.components.*
import com.colddelivery.app.core.auth.PasswordSetupError
import com.colddelivery.app.core.auth.validatePasswordSetup
import com.colddelivery.app.ui.feature.LanguageManager
import com.colddelivery.app.ui.theme.ColdDeliveryColors
import com.colddelivery.app.ui.theme.ColdDeliveryShapes
import kotlinx.coroutines.launch

@Composable fun PremiumLoginScreen(onLoggedIn: () -> Unit, vm: AuthViewModel) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(true) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val language by vm.language.collectAsState()
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Gold.copy(alpha = .35f), focusedContainerColor = ColdDeliveryColors.Cream, unfocusedContainerColor = ColdDeliveryColors.Cream)

    Column(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(205.dp).background(Brush.verticalGradient(listOf(ColdDeliveryColors.DarkRed, DeepRed)))) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                PagodaLineArt(Modifier.size(70.dp, 82.dp), opacity = .93f)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(com.colddelivery.app.R.string.app_name), color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp)
                Text(stringResource(com.colddelivery.app.R.string.login_subtitle), color = Beige, fontFamily = FontFamily.Serif, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(7.dp))
                Text("❈", color = Gold, fontSize = 18.sp)
            }
            Text("❈", color = Gold.copy(alpha = .38f), fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 8.dp))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LanguageSegmentedControl(language, { vm.saveLanguage(it); LanguageManager.apply(context, it) }, Modifier.padding(bottom = 25.dp))
            Text(stringResource(com.colddelivery.app.R.string.welcome_to_cold_delivery), color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 39.sp)
            Text(stringResource(com.colddelivery.app.R.string.sign_in_manage), color = ColdDeliveryColors.SecondaryText, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(12.dp))
            MyanmarDecorativeDivider()
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth().heightIn(min = 64.dp), label = { Text(stringResource(com.colddelivery.app.R.string.username)) }, leadingIcon = { Icon(Icons.Default.Person, null, tint = ColdDeliveryColors.SecondaryText) }, shape = ColdDeliveryShapes.Input, colors = fieldColors, singleLine = true)
            Spacer(Modifier.height(13.dp))
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().heightIn(min = 64.dp), label = { Text(stringResource(com.colddelivery.app.R.string.password)) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColdDeliveryColors.SecondaryText) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, stringResource(if (passwordVisible) com.colddelivery.app.R.string.hide_password else com.colddelivery.app.R.string.show_password), tint = ColdDeliveryColors.SecondaryText) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), shape = ColdDeliveryShapes.Input, colors = fieldColors, singleLine = true)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Checkbox(remember, { remember = it }, colors = CheckboxDefaults.colors(checkedColor = DeepRed)); Text(stringResource(com.colddelivery.app.R.string.remember_me), color = ColdDeliveryColors.Charcoal, fontSize = 14.sp, lineHeight = 20.sp) }
            error?.let { Text(it, color = DeepRed, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) }
            Spacer(Modifier.height(22.dp))
            PrimaryRedButton(stringResource(com.colddelivery.app.R.string.login), { scope.launch { if (vm.login(username, password, remember)) onLoggedIn() else error = vm.message.value } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(30.dp))
            MyanmarDecorativeDivider()
            Text(stringResource(com.colddelivery.app.R.string.footer_tagline), color = ColdDeliveryColors.SecondaryText, fontFamily = FontFamily.Serif, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}


@Composable fun PremiumAccountSetupScreen(onCreated: () -> Unit, vm: AuthViewModel) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val language by vm.language.collectAsState()
    val validation = validatePasswordSetup(password, confirmation)
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Gold.copy(alpha = .35f), focusedContainerColor = ColdDeliveryColors.Cream, unfocusedContainerColor = ColdDeliveryColors.Cream)
    Column(Modifier.fillMaxSize().background(Ivory).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(205.dp).background(Brush.verticalGradient(listOf(ColdDeliveryColors.DarkRed, DeepRed)))) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                PagodaLineArt(Modifier.size(70.dp, 82.dp), opacity = .93f)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(com.colddelivery.app.R.string.app_name), color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp)
                Text(stringResource(com.colddelivery.app.R.string.login_subtitle), color = Beige, fontFamily = FontFamily.Serif, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(7.dp)); Text("❈", color = Gold, fontSize = 18.sp)
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LanguageSegmentedControl(language, { vm.saveLanguage(it); LanguageManager.apply(context, it) }, Modifier.padding(bottom = 25.dp))
            Text(stringResource(com.colddelivery.app.R.string.first_time_setup), color = ColdDeliveryColors.Charcoal, fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 39.sp)
            Text(stringResource(com.colddelivery.app.R.string.create_login_password), color = ColdDeliveryColors.SecondaryText, fontSize = 14.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(20.dp)); MyanmarDecorativeDivider(); Spacer(Modifier.height(20.dp))
            OutlinedTextField("admin", {}, Modifier.fillMaxWidth().heightIn(min = 64.dp), label = { Text(stringResource(com.colddelivery.app.R.string.username)) }, leadingIcon = { Icon(Icons.Default.Person, null, tint = ColdDeliveryColors.SecondaryText) }, shape = ColdDeliveryShapes.Input, colors = fieldColors, singleLine = true, readOnly = true)
            Spacer(Modifier.height(13.dp))
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().heightIn(min = 64.dp), label = { Text(stringResource(com.colddelivery.app.R.string.create_password)) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColdDeliveryColors.SecondaryText) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, stringResource(if (passwordVisible) com.colddelivery.app.R.string.hide_password else com.colddelivery.app.R.string.show_password), tint = ColdDeliveryColors.SecondaryText) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), shape = ColdDeliveryShapes.Input, colors = fieldColors, singleLine = true)
            Spacer(Modifier.height(13.dp))
            OutlinedTextField(confirmation, { confirmation = it }, Modifier.fillMaxWidth().heightIn(min = 64.dp), label = { Text(stringResource(com.colddelivery.app.R.string.confirm_password)) }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = ColdDeliveryColors.SecondaryText) }, trailingIcon = { IconButton(onClick = { confirmationVisible = !confirmationVisible }) { Icon(if (confirmationVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, stringResource(if (confirmationVisible) com.colddelivery.app.R.string.hide_password else com.colddelivery.app.R.string.show_password), tint = ColdDeliveryColors.SecondaryText) } }, visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(), shape = ColdDeliveryShapes.Input, colors = fieldColors, singleLine = true)
            if (attempted && validation != null) Text(stringResource(when (validation) { PasswordSetupError.EMPTY -> com.colddelivery.app.R.string.password_required; PasswordSetupError.TOO_SHORT -> com.colddelivery.app.R.string.password_min_length; PasswordSetupError.MISMATCH -> com.colddelivery.app.R.string.passwords_do_not_match }), color = DeepRed, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Spacer(Modifier.height(22.dp))
            PrimaryRedButton(stringResource(com.colddelivery.app.R.string.create_account), { attempted = true; if (validation == null) scope.launch { if (vm.createAccount(password)) onCreated() } }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(30.dp)); MyanmarDecorativeDivider(); Text(stringResource(com.colddelivery.app.R.string.footer_tagline), color = ColdDeliveryColors.SecondaryText, fontFamily = FontFamily.Serif, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}