package com.willowtree.vocable.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.koin.core.KoinComponent
import org.koin.core.get

class VocableSharedPreferences : KoinComponent {

    companion object {
        private const val PREFERENCES_NAME =
            "com.willowtree.vocable.utils.vocable-encrypted-preferences"
        private const val KEY_MY_SAYINGS = "KEY_MY_SAYINGS"
        private const val KEY_HEAD_TRACKING_ENABLED = "KEY_HEAD_TRACKING_ENABLED"
    }

    private val encryptedPrefs: EncryptedSharedPreferences by lazy {
        val context = get<Context>()
        EncryptedSharedPreferences.create(
            PREFERENCES_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun getMySayings(): List<String> {
        encryptedPrefs.getStringSet(KEY_MY_SAYINGS, setOf())?.let {
            return it.toList()
        }
        return listOf()
    }

    fun addSaying(saying: String) {
        encryptedPrefs.getStringSet(KEY_MY_SAYINGS, setOf())?.let { sayings ->
            sayings.add(saying)
            encryptedPrefs.edit().putStringSet(KEY_MY_SAYINGS, sayings).apply()
        }
    }

    fun setHeadTrackingEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_HEAD_TRACKING_ENABLED, enabled).apply()
    }

    fun getHeadTrackingEnabled(): Boolean =
        encryptedPrefs.getBoolean(KEY_HEAD_TRACKING_ENABLED, true)
}