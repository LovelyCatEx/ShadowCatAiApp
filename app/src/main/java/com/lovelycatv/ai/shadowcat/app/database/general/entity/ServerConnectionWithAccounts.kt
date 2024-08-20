package com.lovelycatv.ai.shadowcat.app.database.general.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ServerConnectionWithAccounts(
    @Embedded
    val serverConnection: ServerConnectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "server_id"
    )
    val accounts: List<AccountEntity>
) {

}