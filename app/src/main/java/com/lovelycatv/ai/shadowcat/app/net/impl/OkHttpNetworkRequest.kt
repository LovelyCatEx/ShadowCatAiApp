package com.lovelycatv.ai.shadowcat.app.net.impl

import com.lovelycatv.ai.shadowcat.app.net.base.NetworkRequest
import com.lovelycatv.ai.shadowcat.app.net.NetworkResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

open class OkHttpNetworkRequest : NetworkRequest() {
    private val okHttpClient = OkHttpClient().newBuilder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun get(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResponse) -> Unit
    ): NetworkResponse? {
        var paramsStr = ""
        params?.forEach {
            paramsStr += "&${it.key}=${it.value}"
        }
        paramsStr = paramsStr.substring(1)

        val request = with(Request.Builder().url("$url?$paramsStr")) {
            headers?.forEach {
                this.addHeader(it.key, it.value)
            }
            this.get()
            this.build()
        }

        return callRequestAsyncOrNot(request, async, asyncCallback)
    }

    override fun post(
        url: String,
        headers: Map<String, String>?,
        params: Map<String, String>?,
        async: Boolean,
        asyncCallback: (response: NetworkResponse) -> Unit
    ): NetworkResponse? {
        val requestBody = with(FormBody.Builder()) {
            params?.forEach {
                this.add(it.key, it.value)
            }
            this.build()
        }
        val request = with(Request.Builder().url(url)) {
            headers?.forEach {
                this.addHeader(it.key, it.value)
            }
            this.post(requestBody)
            this.build()
        }

        return callRequestAsyncOrNot(request, async, asyncCallback)
    }

    private fun callRequestAsyncOrNot(request: Request, async: Boolean, asyncCallback: (response: NetworkResponse) -> Unit): NetworkResponse? {
        val call = okHttpClient.newCall(request)
        return if (!async) {
            NetworkResponse(call.execute().body?.string())
        } else {
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    asyncCallback(okHttpResponseToGeneralResponse(response))
                }
            })
            return null
        }
    }

    private fun okHttpResponseToGeneralResponse(response: Response): NetworkResponse {
        return NetworkResponse(response.body?.string())
    }

}