package com.lovelycatv.ai.shadowcat.app.database.func.mapper

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity

@Dao
interface MessageDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg messages: MessageEntity)

    @Update
    fun update(message: MessageEntity)

    @Delete
    fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: String)

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY datetime DESC")
    fun getMessagesBySession(sessionId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND datetime < :datetime ORDER BY datetime DESC LIMIT 24")
    fun getMessagesBeforeBySessionBefore(sessionId: String, datetime: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND datetime > :datetime ORDER BY datetime DESC LIMIT 24")
    fun getMessagesBeforeBySessionAfter(sessionId: String, datetime: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY datetime DESC LIMIT 1")
    fun getLastMessageOfSession(sessionId: String): MessageEntity?
}