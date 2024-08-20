package com.lovelycatv.ai.shadowcat.app.database.general.mapper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity

@Dao
interface AccountDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: AccountEntity): Long

    @Update
    fun update(account: AccountEntity)

    @Delete
    fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE server_id = :serverId")
    fun getAccountsByServerId(serverId: Int): LiveData<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Int): AccountEntity?

    @Query("SELECT * FROM accounts WHERE server_id = :serverId AND username = :username")
    fun exists(serverId: Int, username: String): AccountEntity?
}