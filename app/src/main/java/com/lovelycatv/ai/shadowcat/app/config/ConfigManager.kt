package com.lovelycatv.ai.shadowcat.app.config

import android.app.Activity
import com.lovelycatv.ai.shadowcat.app.config.connection.ConnectionConfig

class ConfigManager {
    private var initialized = false
    var connectionConfig: ConnectionConfig? = null

    fun init(activity: Activity) {
        connectionConfig = ConnectionConfig(activity)
        this.initialized = true
    }

    fun isInitialized() = initialized

    companion object {
        @JvmStatic
        private val configManager = ConfigManager()

        @JvmStatic
        fun getInstance(): ConfigManager {
            return configManager
        }
    }
}