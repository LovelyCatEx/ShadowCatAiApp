package com.lovelycatv.ai.shadowcat.app.net.base

import com.lovelycatv.ai.shadowcat.app.net.NetworkResponse

abstract class NetworkRequest {
    abstract fun get(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResponse) -> Unit
    ): NetworkResponse?

    abstract fun post(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResponse) -> Unit
    ): NetworkResponse?
}