package com.lovelycatv.ai.shadowcat.app.database.general.mapper

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lovelycatv.ai.shadowcat.app.database.general.entity.SettingEntity

@Dao
interface SettingDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg setting: SettingEntity)

    @Update
    fun update(setting: SettingEntity)

    @Delete
    fun delete(setting: SettingEntity)

    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getSettingItem(key:String): SettingEntity?
}