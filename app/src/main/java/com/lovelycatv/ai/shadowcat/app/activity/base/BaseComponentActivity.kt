package com.lovelycatv.ai.shadowcat.app.activity.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lovelycatv.ai.shadowcat.app.ui.theme.ShadowCatTheme

val GlobalTheme: @Composable (content: @Composable () -> Unit) -> Unit = {
    ShadowCatTheme(
        darkTheme = isSystemInDarkTheme(),
        dynamicColor = true
    ) {
        it()
    }
}

abstract class BaseComponentActivity<A : ComponentActivity> : ComponentActivity() {
    private lateinit var instance: A

    // System Bars Dark Content Status
    private val _systemBarsDarkContentEnabled = MutableLiveData(false)

    fun setDarkContentEnabled(enabled: Boolean) {
        this._systemBarsDarkContentEnabled.postValue(enabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateInstance()

        enableEdgeToEdge()

        setContent {
            GlobalTheme {
                val systemUiController = rememberSystemUiController()

                val systemBarsDarkContentEnabled by _systemBarsDarkContentEnabled.observeAsState()
                LaunchedEffect(systemBarsDarkContentEnabled) {
                    systemUiController.systemBarsDarkContentEnabled = systemBarsDarkContentEnabled ?: false
                }

                doOnCreate().invoke()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateInstance() {
        instance = this as A
    }

    fun getInstance() = this.instance

    abstract fun doOnCreate(): @Composable () -> Unit
}