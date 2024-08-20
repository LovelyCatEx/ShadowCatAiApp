package com.lovelycatv.ai.shadowcat.app.util.common

import com.alibaba.fastjson2.JSON

fun <T> T.toJSONString(): String = JSON.toJSONString(this)

inline fun <reified R> String.toExplicitObject(): R = JSON.parseObject(this, R::class.java)

fun <T> Iterable<T>.printLines(propSpecify: ((T) -> Any?)? = null) = this.forEach {
    println(propSpecify?.invoke(it) ?: JSON.toJSONString(it))
}