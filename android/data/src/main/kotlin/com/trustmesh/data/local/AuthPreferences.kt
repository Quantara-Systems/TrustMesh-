package com.trustmesh.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val sharedPrefs = EncryptedSharedPreferences.create(
        "trustmesh_secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAccessToken(): String? = sharedPrefs.getString("access_token", null)

    fun getRefreshToken(): String? = sharedPrefs.getString("refresh_token", null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPrefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun clearTokens() {
        sharedPrefs.edit().clear().apply()
    }

    fun isDarkTheme(): Boolean = sharedPrefs.getBoolean("dark_theme", false)

    fun setDarkTheme(isDark: Boolean) {
        sharedPrefs.edit().putBoolean("dark_theme", isDark).apply()
    }
}
