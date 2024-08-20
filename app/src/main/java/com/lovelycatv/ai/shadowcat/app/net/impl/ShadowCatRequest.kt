package com.lovelycatv.ai.shadowcat.app.net.impl

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.lovelycatv.ai.shadowcat.app.net.NetworkResponse
import com.lovelycatv.ai.shadowcat.app.net.response.NetworkResult

class ShadowCatRequest : OkHttpNetworkRequest() {
    fun <T> getExplicit(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        onFailed: (NetworkResult<T>) -> Unit,
        callback: (response: NetworkResult<T>) -> Unit
    ) {
        originGetExplicit<T>(url, headers, params, true) {
            if (it.code == 200) {
                callback(it)
            } else {
                onFailed(it)
            }
        }
    }

    fun <T> postExplicit(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        onFailed: (NetworkResult<T>) -> Unit,
        callback: (response: NetworkResult<T>) -> Unit
    ) {
        originPostExplicit<T>(url, headers, params, true) {
            if (it.code == 200) {
                callback(it)
            } else {
                onFailed(it)
            }
        }
    }

    fun <T> originGetExplicit(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResult<T>) -> Unit
    ): NetworkResult<T>? {
        val typeReference = object : TypeReference<NetworkResult<T>>() {}

        val fx = fun (response: NetworkResponse): NetworkResult<T> {
            return JSON.parseObject(response.responseBody, typeReference)
        }

        if (async) {
            super.get(url, headers, params, async) {
                asyncCallback(fx(it))
            }
            return null
        } else {
            val response = super.get(url, headers, params, async) {}
            return fx(response!!)
        }
    }

    fun <T> originPostExplicit(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResult<T>) -> Unit
    ): NetworkResult<T>? {
        val typeReference = object : TypeReference<NetworkResult<T>>() {}

        val fx = fun (response: NetworkResponse): NetworkResult<T> {
            return JSON.parseObject(response.responseBody, typeReference)
        }

        if (async) {
            super.post(url, headers, params, async) {
                asyncCallback(fx(it))
            }
            return null
        } else {
            val response = super.post(url, headers, params, async) {}
            return fx(response!!)
        }
    }
}