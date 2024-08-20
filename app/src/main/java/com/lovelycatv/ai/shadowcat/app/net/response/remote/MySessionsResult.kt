package com.lovelycatv.ai.shadowcat.app.net.response.remote

import com.alibaba.fastjson2.annotation.JSONField
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity

data class MySessionsResult(
    @JSONField(name = "id")
    var id: String,
    @JSONField(name = "uid")
    var uid: Long,
    @JSONField(name = "modelId")
    var modelId: Long,
    @JSONField(name = "name")
    var name: String
) {
    fun toSessionEntity(): SessionEntity {
        return SessionEntity(id, uid, modelId, name)
    }
}