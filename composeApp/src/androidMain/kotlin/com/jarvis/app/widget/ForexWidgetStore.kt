package com.jarvis.app.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared DataStore cache for forex widget data.
 * App writes here on every 30s refresh; widget reads from here (instant, no network).
 * Worker also writes here as fallback when app is not in foreground.
 */
private val Context.forexDataStore by preferencesDataStore(name = "forex_widget_cache")

data class ForexWidgetData(
    val totalPnl: Double = 0.0,
    val count: Int = 0,
    val lastUpdated: Long = 0L,
    val pairsSummary: String = ""
)

object ForexWidgetStore {

    private val KEY_TOTAL_PNL = doublePreferencesKey("total_pnl")
    private val KEY_COUNT = intPreferencesKey("count")
    private val KEY_UPDATED = longPreferencesKey("last_updated")
    private val KEY_PAIRS = stringPreferencesKey("pairs_summary")

    fun getFlow(context: Context): Flow<ForexWidgetData> {
        return context.forexDataStore.data.map { prefs: Preferences ->
            ForexWidgetData(
                totalPnl = prefs[KEY_TOTAL_PNL] ?: 0.0,
                count = prefs[KEY_COUNT] ?: 0,
                lastUpdated = prefs[KEY_UPDATED] ?: 0L,
                pairsSummary = prefs[KEY_PAIRS] ?: ""
            )
        }
    }

    suspend fun writeCache(
        context: Context,
        totalPnl: Double,
        count: Int,
        pairsSummary: String
    ) {
        context.forexDataStore.edit { prefs ->
            prefs[KEY_TOTAL_PNL] = totalPnl
            prefs[KEY_COUNT] = count
            prefs[KEY_UPDATED] = System.currentTimeMillis()
            prefs[KEY_PAIRS] = pairsSummary
        }
    }
}
