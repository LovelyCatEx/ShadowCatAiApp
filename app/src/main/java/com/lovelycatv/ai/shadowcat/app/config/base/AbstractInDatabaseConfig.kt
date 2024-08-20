package com.lovelycatv.ai.shadowcat.app.config.base

import android.app.Activity
import com.lovelycatv.ai.shadowcat.app.database.general.ShadowCatGeneralDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.SettingEntity
import com.lovelycatv.ai.shadowcat.app.database.general.getSettingItem
import com.lovelycatv.ai.shadowcat.app.database.general.updateSettingItem

abstract class AbstractInDatabaseConfig<T>(
    val activity: Activity
) {

    fun getDatabase() = ShadowCatGeneralDatabase.getInstance(this.activity)

    fun getSettingItem(key: String): SettingEntity? {
        return getDatabase().getSettingItem(key)
    }

    fun updateSettingItem(key: String, value: String) {
        getDatabase().updateSettingItem(key, value)
    }

    abstract fun getSettings(): T
}