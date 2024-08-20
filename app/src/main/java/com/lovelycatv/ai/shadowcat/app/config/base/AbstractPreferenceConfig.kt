package com.lovelycatv.ai.shadowcat.app.config.base

import android.app.Activity
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

abstract class AbstractPreferenceConfig(
    val activity: Activity
) {
    private var sharedPreferences: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
}