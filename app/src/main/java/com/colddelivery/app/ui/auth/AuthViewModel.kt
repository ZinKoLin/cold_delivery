package com.colddelivery.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colddelivery.app.core.auth.PasswordHasher
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.preferences.PreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class AuthViewModel @Inject constructor(private val db: ColdDeliveryDatabase, private val preferences: PreferencesStore) : ViewModel() {
    val rememberMe = preferences.rememberMe.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val language = preferences.language.stateIn(viewModelScope, SharingStarted.Eagerly, "en")
    private val _message = MutableStateFlow<String?>(null); val message = _message.asStateFlow()
    suspend fun login(username: String, password: String, remember: Boolean): Boolean { val user = db.userDao().get(); val valid = user != null && user.username == username && PasswordHasher.matches(password, user.passwordHash, user.passwordSalt, user.passwordIterations); if (valid) { preferences.setRememberMe(remember); _message.value = null } else _message.value = "Invalid username or password"; return valid }
    fun logout() { viewModelScope.launch { preferences.setRememberMe(false) } }
    fun saveLanguage(value: String) { viewModelScope.launch { preferences.setLanguage(value) } }
}
