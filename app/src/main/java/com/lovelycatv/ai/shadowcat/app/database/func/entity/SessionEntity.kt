package com.lovelycatv.ai.shadowcat.app.database.func.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alibaba.fastjson2.annotation.JSONField

@Entity("sessions")
data class SessionEntity (
    @PrimaryKey
    @JSONField(name = "id")
    var id: String,
    @JSONField(name = "uid")
    var uid: Long,
    @ColumnInfo(name = "model_id")
    @JSONField(name = "modelId")
    var modelId: Long,
    @JSONField(name = "name")
    var name: String
) {
    companion object {
        @JvmStatic
        fun primaryKeyOnly(sessionId: String): SessionEntity {
            return SessionEntity(sessionId, 0, 0, "")
        }
    }

    data class Display(
        val sessionEntity: SessionEntity,
        var mostRecentMessage: String
    )
}