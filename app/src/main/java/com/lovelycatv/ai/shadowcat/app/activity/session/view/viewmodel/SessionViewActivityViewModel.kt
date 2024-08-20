package com.lovelycatv.ai.shadowcat.app.activity.session.view.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovelycatv.ai.shadowcat.app.database.func.ShadowCatDatabase
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.net.response.NetworkResult
import com.lovelycatv.ai.shadowcat.app.net.retrofit.asyncActions
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.android.runInTransactionAsync
import com.lovelycatv.ai.shadowcat.app.util.common.runIfFalse
import com.lovelycatv.ai.shadowcat.app.viewmodel.getCurrentToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionViewActivityViewModel : ViewModel() {
    private val _currentSession = MutableLiveData<SessionEntity>()
    val currentSession: LiveData<SessionEntity> get() = _currentSession

    fun setCurrentSession(session: SessionEntity) {
        _currentSession.value = session
    }


    private val _messageFetchFlag = MutableLiveData<Long>(0)
    val messageFetchFlag: LiveData<Long> get() = _messageFetchFlag

    fun setMessageFetchFlag(newTimePoint: Long) {
        _messageFetchFlag.value = newTimePoint
    }

    private val _messages = MutableLiveData<List<MessageEntity>>(emptyList())
    val messages: LiveData<List<MessageEntity>> get() = _messages

    fun setMessages(messages: List<MessageEntity>, sort: Boolean) {
        val sortedMessages = if (sort) messages.sortedBy { it.datetime } else messages
        _messages.postValue(sortedMessages.also {
            if (it.isNotEmpty()) {
                Log.d("MessageFetchFlag", "Updated to ${it[0].datetime}")
                _messageFetchFlag.postValue(it[0].datetime)
            }
        })
    }

    fun addMessage(message: MessageEntity, sort: Boolean = true) {
        addMessages(listOf(message), sort)
    }

    private fun addMessages(messages: List<MessageEntity>, sort: Boolean = true) {
        if (messages.isEmpty()) {
            return
        }
        setMessages(_messages.value!!.toMutableList().apply {
            messages.forEach {
                this.add(it)
            }
        }, sort)
    }

    fun deleteMessage(message: MessageEntity) {
        val newMessageList = _messages.value!!.toMutableList().apply {
            this.remove(this.filter { it.id == message.id }[0])
        }
        setMessages(newMessageList, false)
    }

    suspend fun withdrawMessage(
        message: MessageEntity,
        failure: () -> Unit = fun () {},
        success: () -> Unit = fun () {}
    ) {
        withContext(Dispatchers.IO) {
            with(getShadowCatServerApi()) {
                try {
                    this.second.withdrawMessage(
                        getCurrentToken(),
                        currentSession.value!!.id,
                        message.messageId
                    ).asyncActions({ failure() }) {
                        // Delete in local
                        deleteMessage(message)
                        success()
                    }
                } catch (e: Exception) {
                    failure()
                    e.printStackTrace()
                }
            }
        }

    }

    val isMessageListRefreshing = MutableLiveData(false)
    /**
     * Get history messages from server,
     * When you need to load more history, set the datetime to the last message of current showing
     *
     * @param globalViewModel GlobalViewModel
     * @param datetime Start point
     * @param db ShadowCatDatabase
     */
    suspend fun loadMessages(
        datetime: Long,
        db: ShadowCatDatabase,
        fetchMostRecent: Boolean,
        failure: (NetworkResult<*>?) -> Unit = fun (_: NetworkResult<*>?) {},
        noMoreMessages: () -> Unit = fun () {},
        afterFinished: (originalTimeFlag: Long, newTimeFlag: Long) -> Unit = fun (_: Long, _: Long) {}
    ) {
        if (isMessageListRefreshing.value != false) {
            return
        }

        this.canMessageListScrollToIndex.postValue(true)
        this.isMessageListRefreshing.postValue(true)

        val originalTimeFlag = messageFetchFlag.value!!

        withContext(Dispatchers.IO) {
            val newMessagesFromLocal = db.messageDAO().getMessagesBeforeBySessionBefore(
                currentSession.value?.id ?: "",
                datetime
            )

            // This variable will controller whether network request should be sent below
            var blockNetworkRequest = false

            // Fetch messages before or after the newMessageFetchTimeFlag
            val fetchDirection: Boolean

            val newMessageFetchTimeFlag = if (newMessagesFromLocal.isEmpty()) {
                // Fetch from remote server
                fetchDirection = true
                datetime
            } else {
                // Add current local messages to UI
                addMessages(newMessagesFromLocal)

                // Not empty, fetch new messages from latest time
                // If param fetchMostRecent is false,
                // it means we are getting earlier messages and they were cached in local before,
                // so this action will be canceled below
                fetchDirection = false
                if (!fetchMostRecent) {
                    blockNetworkRequest = true
                }
                newMessagesFromLocal.first().datetime
            }

            blockNetworkRequest.runIfFalse {
                val messageFromNetwork = mutableListOf<MessageEntity>()
                try {
                    with(getShadowCatServerApi()) {
                        this.second.getMessages(
                            getCurrentToken(),
                            currentSession.value?.id ?: "",
                            newMessageFetchTimeFlag,
                            fetchDirection
                        ).asyncActions({
                            isMessageListRefreshing.postValue(false)
                            failure(it)
                        }) {
                            val arr = it.getExplicitArrayData(MessageEntity::class.java)!!
                            if (arr.isEmpty()) {
                                // Fetch earlier messages only happens when the param fetchMostRecent is false
                                // And function noMoreMessages() only happens when fetching earlier message
                                if (!fetchMostRecent) {
                                    noMoreMessages()
                                }
                            } else {
                                db.runInTransactionAsync(viewModelScope) {
                                    arr.forEach { entity ->
                                        db.messageDAO().insert(entity.apply {
                                            // Put MessageId from server to local Id property
                                            this.messageId = this.id
                                            // AutoIncrease
                                            this.id = 0
                                        })
                                    }
                                }
                                messageFromNetwork.addAll(arr)
                            }

                            null
                        }
                    }
                } catch (e: Exception) {
                    failure(null)
                    Log.w(GlobalConstants.LOG_TAG_SESSION_NETWORK, e)
                }

                // This function will auto update the messageFetchFlag
                addMessages(messageFromNetwork)
            }

            isMessageListRefreshing.postValue(false)

            afterFinished(originalTimeFlag, messageFetchFlag.value!!)
        }
    }

    private val _inputMessage = MutableLiveData("")
    val inputMessage: LiveData<String> get() = _inputMessage

    fun setInputMessage(message: String) {
        _inputMessage.value = message
    }

    /** This variable is used to decide whether to scroll to the bottom or top of the message list
     * When assistant is generating, if user scroll the list and hope not to scroll to the bottom again, just set this value to false
     * because when new streaming message is coming, the list will scroll to the bottom automatically.
     * But when a new assistant streaming message pack is received, function loadMessages() be called, messaged has been sent, this value should be set to true again.
     */
    val canMessageListScrollToIndex = MutableLiveData(true)

    val isSessionBranchCreateVisible = MutableLiveData(false)
    val selectedBranchEndpoint = MutableLiveData(MessageEntity())
}