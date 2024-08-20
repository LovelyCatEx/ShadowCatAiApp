package com.lovelycatv.ai.shadowcat.app.database.func.mapper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity

@Dao
interface SessionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg sessions: SessionEntity)

    @Update
    fun update(session: SessionEntity)

    @Delete
    fun delete(session: SessionEntity)

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions")
    fun getAllSessionsLive(): LiveData<List<SessionEntity>>

    @Query("DELETE FROM sessions")
    fun clearTable()
}