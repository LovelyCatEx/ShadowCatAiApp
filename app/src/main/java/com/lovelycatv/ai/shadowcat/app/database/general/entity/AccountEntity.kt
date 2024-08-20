package com.lovelycatv.ai.shadowcat.app.database.general.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo("user_id")
    var userId: Long,
    @ColumnInfo("server_id")
    var serverId: Int,
    var username: String,
    var password: String,
    var nickname: String,
    var email: String,
    var avatar: String
) {
    fun getAvatarUrl(serverConnectionEntity: ServerConnectionEntity): String {
        return "${serverConnectionEntity.address}:${serverConnectionEntity.port}/assets/${avatar}"
    }
}