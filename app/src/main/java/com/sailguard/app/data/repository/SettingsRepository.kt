package com.sailguard.app.data.repository

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("sailguard_settings", Context.MODE_PRIVATE)

    var connectaLinkCode: String?
        get() = prefs.getString(KEY_LINK_CODE, null)
        set(value) = prefs.edit().putString(KEY_LINK_CODE, value).apply()

    companion object {
        private const val KEY_LINK_CODE = "connecta_link_code"
    }
}
