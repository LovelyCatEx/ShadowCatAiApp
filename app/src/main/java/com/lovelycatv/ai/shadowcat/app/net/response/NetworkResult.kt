package com.lovelycatv.ai.shadowcat.app.net.response

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject

data class NetworkResult<T>(
    var code: Int,
    var message: String,
    var data: String?
) {
    fun toJSONString() = JSON.toJSONString(this)

    fun getExplicitData(clazz: Class<T>): T? {
        return if (this.data == null) {
            null
        } else {
            JSON.parseObject(this.data!!, clazz)
        }
    }

    fun getExplicitArrayData(clazz: Class<T>): MutableList<T>? {
        return if (this.data == null) {
            null
        } else {
            mutableListOf<T>().apply {
                val arr = JSONArray.parseArray(data!!)
                for (i in 0 until arr.size) {
                    val o = arr.getJSONObject(i)
                    this.add(JSON.parseObject(o.toJSONString(), clazz))
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun <T> fromJSONString(str: String): NetworkResult<*> {
            val result = NetworkResult<T>(0, "", null)
            with(JSONObject.parseObject(str)) {
                result.code = this.getInteger("code")
                result.message = this.getString("message")
                result.data = this.getString("data")
            }
            return result
        }

        @JvmStatic
        fun empty() = NetworkResult<Any?>(408, "I'm a teapot", null)
    }
}