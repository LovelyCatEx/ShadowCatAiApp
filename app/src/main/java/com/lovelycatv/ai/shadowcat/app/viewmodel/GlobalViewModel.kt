package com.lovelycatv.ai.shadowcat.app.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.database.func.ShadowCatDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.net.response.remote.LoginResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.UserMetaResult
import com.lovelycatv.ai.shadowcat.app.net.retrofit.ShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import kotlinx.coroutines.delay

suspend fun autoRefreshCurrentUserStatus(): Boolean {
    val viewModel = GlobalViewModel.instance

    var result = false

    result = viewModel.refreshToken()
    if (result) {
        delay(200)
        result = viewModel.refreshUserMeta()
    }

    return result
}

fun getCurrentUserDatabase(context: Context): ShadowCatDatabase {
    val viewModel = GlobalViewModel.instance
    return ShadowCatDatabase.getInstance(context, viewModel.userMeta.value?.userId ?: 0)
}

fun getCurrentUser(): AccountEntity {
    return GlobalViewModel.instance.userMeta.value!!
}

fun getCurrentToken(): String {
    return GlobalViewModel.instance.token.value ?: ""
}

class GlobalViewModel : ViewModel() {
    companion object {
        @JvmStatic
        val instance = GlobalViewModel()
    }

    private val _userMeta = MutableLiveData<AccountEntity>()
    val userMeta: LiveData<AccountEntity> get() = _userMeta

    fun setUserMeta(newData: AccountEntity) {
        this._userMeta.value = newData
    }

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    fun setToken(newData: String) {
        this._token.postValue(newData)
    }

    fun initialize(context: Context) {
        // Local from local database
        try {
            val connectionConfig = ConfigManager.getInstance().connectionConfig!!
            _userMeta.postValue(connectionConfig.getSettings().currentConnectedAccount)
            _token.postValue(connectionConfig.getToken())
        } catch (e: Exception) {
            DialogUtils.showTips(context, "An error occurred when try to initialize the primary status of application. \n ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Update current token from remote server
     */
    suspend fun refreshToken(): Boolean {
        if (ConfigManager.getInstance().connectionConfig == null) {
            return false
        }
        val config = ConfigManager.getInstance().connectionConfig!!
        val settings = config.getSettings()
        val api = ShadowCatServerApi(settings.getBaseUrl()).api

        return with(settings.currentConnectedAccount) {
            try {
                val response = api.login(this.username, this.password)
                if (response.isSuccessful) {
                    val result = response.body()!!.getExplicitData(LoginResult::class.java)!!
                    config.updateSettingItem(GlobalConstants.SETTING_CURRENT_TOKEN, result.token)
                    config.updateSettingItem(GlobalConstants.SETTING_CURRENT_USER_ID, result.uid.toString())

                    // Token observers may request the userId, update userId firstly
                    setUserMeta(AccountEntity(0, result.uid, 0, this.username, "", "", "", ""))

                    setToken(result.token)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.d("RefreshToken", "An error occurred when try to refresh token.", e)
                false
            }
        }
    }

    suspend fun refreshUserMeta(): Boolean {
        val config = ConfigManager.getInstance().connectionConfig!!
        val settings = config.getSettings()
        val api = ShadowCatServerApi(settings.getBaseUrl()).api
        try {
            val result = api.getUserMeta(this.token.value ?: "")
            if (result.isSuccessful) {
                val response = result.body()!!.getExplicitData(UserMetaResult::class.java)!!
                val accountEntity = config.getDatabase().accountDAO().getAccountById(settings.currentConnectedAccount.id)!!
                with(accountEntity) {
                    this.userId = response.userId
                    this.username = response.username
                    this.email = response.email
                    this.nickname = response.nickname
                    this.avatar = response.avatar
                    config.getDatabase().accountDAO().update(this)
                    _userMeta.postValue(accountEntity)
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.d("RefreshUserMeta", "An error occurred when try to refresh user meta.", e)
            return false
        }
    }

    /* For Netty Client */
    val imClientInitialized = MutableLiveData(false)

    var imStreamingListenerKey = ""
}