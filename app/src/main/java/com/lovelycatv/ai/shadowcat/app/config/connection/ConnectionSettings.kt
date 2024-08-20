package com.lovelycatv.ai.shadowcat.app.config.connection

import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionEntity

class ConnectionSettings(
    var currentConnectedServer: ServerConnectionEntity,
    var currentConnectedAccount: AccountEntity
) {
    fun getBaseUrl(): String = "${currentConnectedServer.address}:${currentConnectedServer.port}"
}