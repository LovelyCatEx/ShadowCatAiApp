package com.lovelycatv.ai.shadowcat.app.net.response.remote

import com.alibaba.fastjson2.annotation.JSONField

data class ModelResult(
    @JSONField(name = "id")
    var id: Long,
    @JSONField(name = "name")
    var name: String,
    @JSONField(name = "description")
    var description: String,
    @JSONField(name = "qualifiedName")
    var qualifiedName: String,
    @JSONField(name = "available")
    var available: Boolean,
    @JSONField(name = "supportImage")
    var supportImage: Boolean,
    @JSONField(name = "supportAudio")
    var supportAudio: Boolean,
    @JSONField(name = "supportVideo")
    var supportVideo: Boolean
) {
}