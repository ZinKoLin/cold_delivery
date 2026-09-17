package com.colddelivery.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colddelivery.app.core.auth.PasswordHasher
import com.colddelivery.app.data.local.database.ColdDeliveryDatabase
import com.colddelivery.app.data.local.entity.UserEntity
import com.colddelivery.app.data.preferences.PreferencesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel class AuthViewModel @Inject constructor(private val db: ColdDeliveryDatabase, private val preferences: PreferencesStore) : ViewModel() {
    val user = db.userDao().observe().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val rememberMe = preferences.rememberMe.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val language = preferences.language.stateIn(viewModelScope, SharingStarted.Eagerly, "en")
    private val _message = MutableStateFlow<String?>(null); val message = _message.asStateFlow()
    suspend fun createAccount(password: String): Boolean {
        if (db.userDao().get() != null) return false
        val stored = PasswordHasher.create(password)
        db.userDao().upsert(UserEntity(username = "admin", passwordHash = stored.hash, passwordSalt = stored.salt, passwordIterations = stored.iterations))
        return true
    }
    suspend fun login(username: String, password: String, remember: Boolean): Boolean { val user = db.userDao().get(); val valid = user != null && user.username == username && PasswordHasher.matches(password, user.passwordHash, user.passwordSalt, user.passwordIterations); if (valid) { preferences.setRememberMe(remember); _message.value = null } else _message.value = "Invalid username or password"; return valid }
    fun logout() { viewModelScope.launch { preferences.setRememberMe(false) } }
    fun saveLanguage(value: String) { viewModelScope.launch { preferences.setLanguage(value) } }
}