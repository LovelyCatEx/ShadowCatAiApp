package com.lovelycatv.ai.shadowcat.app.database.general.mapper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionWithAccounts

@Dao
interface ServerConnectionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(serverConnection: ServerConnectionEntity): Long

    @Update
    fun update(serverConnection: ServerConnectionEntity)

    @Delete
    fun delete(serverConnection: ServerConnectionEntity)

    @Query("SELECT * FROM servers")
    fun getAllServers(): LiveData<List<ServerConnectionEntity>>

    @Query("SELECT * FROM servers WHERE `id` = :id")
    fun getServerById(id: Int): ServerConnectionEntity?

    @Query("SELECT * FROM servers WHERE address = :address AND port = :port AND chat_port = :chatPort")
    fun exists(address: String, port: Int, chatPort: Int): ServerConnectionEntity?

    @Transaction
    @Query("SELECT * FROM servers")
    fun getServersWithAccounts(): LiveData<List<ServerConnectionWithAccounts>>
}