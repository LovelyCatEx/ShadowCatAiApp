package com.lovelycatv.ai.shadowcat.app.activity.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovelycatv.ai.shadowcat.app.database.func.ShadowCatDatabase
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.net.response.remote.MySessionsResult
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.util.common.ForceDelay
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.android.runInTransactionAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() {
    val isAvatarInFullScreen = MutableLiveData(false)

    private val _sessions = MutableLiveData<List<SessionEntity>>()
    val sessions: LiveData<List<SessionEntity>> get() = _sessions

    val isSessionRefreshing = MutableLiveData(false)

    fun setSessions(newData: List<SessionEntity>) {
        this._sessions.value = newData
    }

    fun loadSessions(token: String, db: ShadowCatDatabase, fromRemote: Boolean, finish: (success: Boolean) -> Unit) {
        if (isSessionRefreshing.value!!) {
            return
        }
        isSessionRefreshing.postValue(true)
        // Clear current sessions
        _sessions.postValue(emptyList())
        runAsync(viewModelScope) {
            delay(100)
            // Get from local firstly
            _sessions.postValue(db.sessionDAO().getAllSessions())

            ForceDelay<Any?>(500) { _, _ ->
                isSessionRefreshing.postValue(false)
            }.start {
                if (fromRemote) {
                    finish(fetchSessions(token, db))
                } else {
                    finish(true)
                }
                null
            }
        }
    }

    private suspend fun fetchSessions(token: String, db: ShadowCatDatabase): Boolean {
        return withContext(Dispatchers.IO) {
            val pair = getShadowCatServerApi()
            val api = pair.second

            try {
                val response = api.getSessions(token)
                if (response.isSuccessful) {
                    val rawSessions = response.body()!!.getExplicitArrayData(MySessionsResult::class.java)
                    if (rawSessions != null) {
                        val sessions = rawSessions.map { s -> s.toSessionEntity() }
                        _sessions.postValue(sessions)
                        withContext(Dispatchers.IO) {
                            val originalSessions = db.sessionDAO().getAllSessions()

                            db.runInTransactionAsync {
                                it.sessionDAO().clearTable()
                                val toBeDeleted = originalSessions.filter { it.id !in sessions.map { it.id }.toSet() }

                                sessions.forEach { session ->
                                    it.sessionDAO().insert(session)
                                }

                                toBeDeleted.forEach { session ->
                                    it.messageDAO().deleteBySessionId(session.id)
                                }
                            }
                        }
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    }
}