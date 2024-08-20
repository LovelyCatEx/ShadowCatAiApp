package com.lovelycatv.ai.shadowcat.app.database.general.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("settings")
data class SettingEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var key: String,
    var value: String
) {
    fun stringValue() = this.value

    fun toInt() = this.value.toInt()

    fun toLong() = this.value.toLong()

    fun toBoolean() = this.value.toBoolean()
}