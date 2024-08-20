package com.lovelycatv.ai.shadowcat.app.net.retrofit

import android.util.Log
import com.alibaba.fastjson2.JSON
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.config.connection.ConnectionConfig
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity
import com.lovelycatv.ai.shadowcat.app.exception.NetworkResultNotBeDecodedProperlyException
import com.lovelycatv.ai.shadowcat.app.net.response.NetworkResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.LoginResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.ModelResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.MySessionsResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.UserMetaResult
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

inline fun <T> Call<T>.enqueue(
    crossinline onError: (call: Call<T>, throwable: Throwable) -> Unit,
    crossinline onSuccess: (result: T) -> Unit
) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(p0: Call<T>, p1: Response<T>) {
            val result = p1.body()
            if (result != null) {
                onSuccess(result)
            } else {
                onError(p0, NetworkResultNotBeDecodedProperlyException("Return of url: ${p0.request().url} is null"))
            }
        }

        override fun onFailure(p0: Call<T>, p1: Throwable) {
            onError(p0, p1)
        }
    })
}

/**
 * For NetworkResult<*> use
 *
 * @param T The T in NetworkResult<T>
 * @param onError onError
 * @param onFailure onFailure
 * @param onSuccess onSuccess
 */
inline fun <T> Call<NetworkResult<T>>.enhancedEnqueue(
    crossinline onError: (call: Call<NetworkResult<T>>, throwable: Throwable) -> Unit,
    crossinline onFailure: (result: NetworkResult<T>) -> Unit,
    crossinline onSuccess: (result: NetworkResult<T>) -> Unit
) {
    this.enqueue({ call, throwable -> onError(call, throwable) }) {
        if (it.code == 200) {
            onSuccess(it)
        } else {
            onFailure(it)
        }
    }
}

inline fun <T> Call<NetworkResult<T>>.enhancedEnqueue(
    crossinline onFailure: (result: NetworkResult<T>) -> Unit,
    crossinline onSuccess: (result: NetworkResult<T>) -> Unit
) {
    this.enhancedEnqueue({ _, error ->
        run {
            error.printStackTrace()
            onFailure(NetworkResult(500, "Internal Server Error", null))
        }
    }, onFailure, onSuccess)
}

inline fun <T> Response<NetworkResult<T>>.actions(
    crossinline onFailure: (result: NetworkResult<T>) -> Unit,
    crossinline onSuccess: (result: NetworkResult<T>) -> Unit
) {
    println("Retrofit Remote => ${JSON.toJSONString(this.body())}")
    if (this.isSuccessful) {
        if (this.body() == null) {
            onFailure(NetworkResult(this.code(), this.message(), null))
        } else {
            with(this.body()!!) {
                if (this.code == 200) {
                    onSuccess(this)
                } else {
                    onFailure(this)
                }
            }
        }
    } else {
        onFailure(NetworkResult(this.code(), this.message(), null))
    }
}

suspend inline fun <T, R> Response<NetworkResult<T>>.asyncActions(
    crossinline onFailure: suspend (result: NetworkResult<T>) -> R,
    crossinline onSuccess: suspend (result: NetworkResult<T>) -> R
): R {
    println("Retrofit Remote => ${JSON.toJSONString(this.body())}")
    if (this.isSuccessful) {
        if (this.body() == null) {
            return onFailure(NetworkResult(this.code(), this.message(), null))
        } else {
            with(this.body()!!) {
                if (this.code == 200) {
                    Log.d("AsyncActions", JSON.toJSONString(this))
                    return onSuccess(this)
                } else {
                    return onFailure(this)
                }
            }
        }
    } else {
        return onFailure(NetworkResult(this.code(), this.message(), null))
    }
}

interface IShadowCatServerApi {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<NetworkResult<LoginResult>>

    @GET("model/list")
    suspend fun getModels(@Header("Authorization") token: String): Response<NetworkResult<ModelResult>>

    @GET("session/mine")
    suspend fun getSessions(@Header("Authorization") token: String): Response<NetworkResult<MySessionsResult>>

    @FormUrlEncoded
    @POST("session/create")
    suspend fun createSession(
        @Header("Authorization") token: String,
        @Field("name") sessionName: String,
        @Field("modelId") modelId: Long,
    ): Response<NetworkResult<String>>

    @FormUrlEncoded
    @POST("session/update")
    suspend fun updateSession(
        @Header("Authorization") token: String,
        @Field("sessionId") sessionId: String,
        @Field("name") sessionName: String,
        @Field("modelId") modelId: Long,
    ): Response<NetworkResult<String>>

    @FormUrlEncoded
    @POST("session/branch")
    suspend fun createSessionBranch(
        @Header("Authorization") token: String,
        @Field("sessionId") sessionId: String,
        @Field("before") before: Long,
        @Field("name") sessionName: String,
        @Field("modelId") modelId: Long,
    ): Response<NetworkResult<String>>

    @GET("message/session")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Query("sessionId") sessionId: String,
        @Query("datetime") datetime: Long,
        @Query("direction") direction: Boolean,
    ): Response<NetworkResult<MessageEntity>>

    @GET("user/meta")
    suspend fun getUserMeta(@Header("Authorization") token: String): Response<NetworkResult<UserMetaResult>>

    @FormUrlEncoded
    @POST("user/nickname")
    suspend fun updateNickname(
        @Header("Authorization") token: String,
        @Field("nickname") nickname: String
    ): Response<NetworkResult<String>>

    @Multipart
    @POST("user/avatar")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<NetworkResult<String>>

    @FormUrlEncoded
    @POST("session/delete")
    suspend fun deleteSession(
        @Header("Authorization") token: String,
        @Field("sessionId") sessionId: String
    ): Response<NetworkResult<*>>

    @FormUrlEncoded
    @POST("message/withdraw")
    suspend fun withdrawMessage(
        @Header("Authorization") token: String,
        @Field("sessionId") sessionId: String,
        @Field("messageId") messageId: Long
    ): Response<NetworkResult<String>>
}

fun getShadowCatServerApi(): Pair<ConnectionConfig, IShadowCatServerApi> {
    val config = ConfigManager.getInstance().connectionConfig!!
    val settings = config.getSettings()
    val api = ShadowCatServerApi(settings.getBaseUrl()).api
    return Pair(config, api)
}

fun Pair<ConnectionConfig, IShadowCatServerApi>.provideToken(fx: (api: IShadowCatServerApi, token: String) -> Unit) {
    fx(this.second, this.first.getToken())
}