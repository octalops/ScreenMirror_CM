package com.screenmirror.contractsdemo.utilities.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


object DataStore {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    private val DARK_MODE = booleanPreferencesKey("dark_mode")

    suspend fun setIsFirstTime(context: Context, isFirstTime: Boolean) {
        context.dataStore.edit { setting ->
            setting[IS_FIRST_TIME] = isFirstTime
        }
    }

    fun getIsFirstTime(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { setting ->
            setting[IS_FIRST_TIME] ?: true
        }
    }

    suspend fun setDarkMode(context: Context, isFirstTime: Boolean) {
        context.dataStore.edit { setting ->
            setting[DARK_MODE] = isFirstTime
        }
    }

    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { setting ->
            setting[DARK_MODE] ?: false
        }
    }
}