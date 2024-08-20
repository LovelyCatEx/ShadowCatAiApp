package com.lovelycatv.ai.shadowcat.app.activity.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginActivityViewModel : ViewModel() {
    val address = MutableLiveData("http://")
    val port = MutableLiveData("8080")
    val chatPort = MutableLiveData("8081")
    val username = MutableLiveData("")
    val password = MutableLiveData("")
}