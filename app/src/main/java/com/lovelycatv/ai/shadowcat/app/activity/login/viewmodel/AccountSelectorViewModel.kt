package com.lovelycatv.ai.shadowcat.app.activity.login.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.lovelycatv.ai.shadowcat.app.database.general.ShadowCatGeneralDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionWithAccounts

class AccountSelectorViewModel : ViewModel() {
    lateinit var serversWithAccounts: LiveData<List<ServerConnectionWithAccounts>>

    fun initialize(context: Context) {
        val db = ShadowCatGeneralDatabase.getInstance(context)
        serversWithAccounts = db.serverConnectionDAO().getServersWithAccounts()
    }
}