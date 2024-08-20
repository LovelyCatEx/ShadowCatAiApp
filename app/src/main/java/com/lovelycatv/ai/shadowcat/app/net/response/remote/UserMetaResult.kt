package com.lovelycatv.ai.shadowcat.app.net.response.remote

import com.alibaba.fastjson2.annotation.JSONField

class UserMetaResult(
    @JSONField(name = "userId")
    var userId: Long,
    @JSONField(name = "username")
    var username: String,
    @JSONField(name = "email")
    var email: String,
    @JSONField(name = "nickname")
    var nickname: String,
    @JSONField(name = "avatar")
    var avatar: String
) {
}