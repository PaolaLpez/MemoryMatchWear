package mx.utng.memorymatch.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("memory_match_prefs")

class BestTimeDataSource(context: Context) {
    private val dataStore = context.dataStore
    private object Keys {
        val BEST_TIME = longPreferencesKey("best_time_seconds")
    }

    suspend fun getBestTime(): Long =
        dataStore.data.map { it[Keys.BEST_TIME] ?: Long.MAX_VALUE }.first()

    suspend fun saveBestTime(seconds: Long) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BEST_TIME] ?: Long.MAX_VALUE
            if (seconds < current) prefs[Keys.BEST_TIME] = seconds
        }
    }
}