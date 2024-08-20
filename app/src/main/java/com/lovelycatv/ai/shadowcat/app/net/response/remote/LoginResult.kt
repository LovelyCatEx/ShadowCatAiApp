package com.lovelycatv.ai.shadowcat.app.net.response.remote

import com.alibaba.fastjson2.annotation.JSONField

data class LoginResult(
    @JSONField(name = "uid")
    var uid: Long,
    @JSONField(name = "token")
    var token: String
)
