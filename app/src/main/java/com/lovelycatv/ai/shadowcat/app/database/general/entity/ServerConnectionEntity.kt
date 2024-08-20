package com.lovelycatv.ai.shadowcat.app.database.general.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("servers")
data class ServerConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var address: String,
    var port: Int,
    @ColumnInfo(name = "chat_port")
    var chatPort: Int
) {
}