package com.colddelivery.app.ui.feature

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageManager {
    fun wrap(context: Context, language: String): Context { val config = Configuration(context.resources.configuration); val locale = if (language == "my") Locale("my", "MM") else Locale.ENGLISH; if (Build.VERSION.SDK_INT >= 24) config.setLocales(android.os.LocaleList(locale)) else @Suppress("DEPRECATION") config.locale = locale; return context.createConfigurationContext(config) }
    fun apply(context: Context, language: String) { val config = Configuration(context.resources.configuration); val locale = if (language == "my") Locale("my", "MM") else Locale.ENGLISH; if (Build.VERSION.SDK_INT >= 24) config.setLocales(android.os.LocaleList(locale)) else @Suppress("DEPRECATION") config.locale = locale; @Suppress("DEPRECATION") context.resources.updateConfiguration(config, context.resources.displayMetrics) }
}
