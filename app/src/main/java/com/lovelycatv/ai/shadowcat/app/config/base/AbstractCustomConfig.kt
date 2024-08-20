package com.lovelycatv.ai.shadowcat.app.config.base

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

abstract class AbstractCustomConfig(
    val activity: Activity,
    val configName: String
) {
    private var sharedPreferences: SharedPreferences = this.activity.getSharedPreferences(this.configName, Context.MODE_PRIVATE)

    fun getConfig(): SharedPreferences = this.sharedPreferences
}