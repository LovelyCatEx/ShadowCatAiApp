package com.lovelycatv.ai.shadowcat.app.activity.session.view

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alibaba.fastjson2.JSON
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.base.BaseComponentActivity
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.composable.SessionCreatorComponent
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.viewmodel.SessionCreatorViewModel
import com.lovelycatv.ai.shadowcat.app.activity.session.ui.ChatPopComponent
import com.lovelycatv.ai.shadowcat.app.activity.session.view.viewmodel.SessionViewActivityViewModel
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.database.func.ShadowCatDatabase
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.exception.session.InvalidSessionException
import com.lovelycatv.ai.shadowcat.app.im.ShadowCatNettyClient
import com.lovelycatv.ai.shadowcat.app.im.message.func.CallBackMessage
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CurrencyTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CustomTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.TopAppBarActionButton
import com.lovelycatv.ai.shadowcat.app.ui.theme.ShadowCatTheme
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeLarge
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.notCancelable
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.showToast
import com.lovelycatv.ai.shadowcat.app.util.android.spToDp
import com.lovelycatv.ai.shadowcat.app.util.android.stringCancel
import com.lovelycatv.ai.shadowcat.app.util.android.stringConfirm
import com.lovelycatv.ai.shadowcat.app.util.android.stringContinue
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.common.runAsyncIfNotNull
import com.lovelycatv.ai.shadowcat.app.util.common.runIfFalse
import com.lovelycatv.ai.shadowcat.app.util.common.runIfNotNull
import com.lovelycatv.ai.shadowcat.app.util.common.runOnMain
import com.lovelycatv.ai.shadowcat.app.util.common.searchIndex
import com.lovelycatv.ai.shadowcat.app.util.common.toExplicitObject
import com.lovelycatv.ai.shadowcat.app.util.common.toJSONString
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEventEnum
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.post
import com.lovelycatv.ai.shadowcat.app.viewmodel.getCurrentUserDatabase
import com.lovelycatv.ai.shadowcat.app.viewmodel.im.InstantMessageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SessionViewActivity : BaseComponentActivity<SessionViewActivity>() {
    companion object {
        @JvmStatic
        fun toThisActivity(context: Context, data: SessionEntity) {
            context.startActivity(Intent(context, SessionViewActivity::class.java).apply {
                this.putExtra("data", data.toJSONString())
            })
        }
    }

    private val myListenerKey = UUID.randomUUID().toString()
    private val imViewModel = InstantMessageViewModel.instance
    private val globalViewModel = GlobalViewModel.instance

    private val viewModel: SessionViewActivityViewModel by viewModels()

    private var client: ShadowCatNettyClient? = ShadowCatNettyClient.instance

    private lateinit var userDatabase: ShadowCatDatabase

    override fun doOnCreate(): @Composable () -> Unit {
        globalViewModel.imStreamingListenerKey = myListenerKey

        with(intent.extras?.getString("data")) {
            if (this != null) {
                viewModel.setCurrentSession(this.toExplicitObject())
            } else {
                getString(R.string.activity_session_view_invalid_session_data).showToast(getInstance())
                finishAfterTransition()
                throw InvalidSessionException()
            }
        }

        userDatabase = getCurrentUserDatabase(this)

        return {
            ShadowCatTheme {
                SessionView()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        GlobalEventEnum.MAIN_SESSION_LIST_REFRESH_LOCAL.post()
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Preview(showBackground = true)
    @Composable
    fun SessionView(viewModel: SessionViewActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val currentSession by viewModel.currentSession.observeAsState(null)

        val messageListState = rememberLazyListState()
        val messages by viewModel.messages.observeAsState(emptyList())

        // Messages list scroll listener
        LaunchedEffect(messageListState.isScrollInProgress) {
            if (viewModel.canMessageListScrollToIndex.value != false) {
                viewModel.canMessageListScrollToIndex.postValue(false)
            }
        }

        LaunchedEffect(Unit) {
            viewModel.loadMessages(
                System.currentTimeMillis(),
                userDatabase,
                true,
                failure = {
                    runOnUiThread {
                        DialogUtils.showTips(
                            context,
                            context.getString(R.string.activity_session_view_dialog_history_message_fetch_failed)
                        ).notCancelable().positive().show()
                    }
                },
                noMoreMessages = {
                    runOnUiThread {
                        DialogUtils.showTips(context, context.getString(R.string.activity_session_view_no_messages))
                            .positive().show()
                    }
                }
            ) { _, _ ->
                /**
                 * When message list changed, scroll to the most recent one
                 */
                runOnMain(coroutineScope) {
                    if (messages.isNotEmpty()) {
                        messageListScrollTo(coroutineScope, messageListState, messages.size)
                    }
                }
            }
        }

        var currentShowingAssistantMessage by remember { mutableStateOf("") }

        /**
         * Callback streaming data from server will be handled below
         */
        imViewModel._imStreamingDataPack.observe { streamingPack ->
            if (globalViewModel.imStreamingListenerKey != myListenerKey) {
                return@observe
            }
            globalViewModel.imStreamingListenerKey = myListenerKey

            Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "streamingPack: ${JSON.toJSONString(streamingPack)}")

            if (streamingPack.isNewStream) {
                // New Stream
                currentShowingAssistantMessage = streamingPack.data

                viewModel.canMessageListScrollToIndex.postValue(true)
            } else if (!streamingPack.completed) {
                // Streaming
                currentShowingAssistantMessage += streamingPack.data
            } else {
                // Received completed message from remote server, now we get the messageId of the assistant message and full message
                Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "Streaming completed, streamId: ${streamingPack.streamId}")
                imViewModel.canSendMessage.postValue(true)

                val assistantMessage = MessageEntity.forAssistant(streamingPack.messageId, streamingPack.sessionId, streamingPack.data)

                currentShowingAssistantMessage = ""

                Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "New assistant message will be save to local database, [${JSON.toJSONString(assistantMessage)}]")

                viewModel.addMessage(assistantMessage, sort = false)

                runAsync(coroutineScope) {
                    userDatabase.messageDAO().insert(assistantMessage)
                }
            }

            /**
             * When received streamingPack, current message list should scroll to the last one
             */
            if (messages.isNotEmpty()) {
                messageListScrollTo(coroutineScope, messageListState, messages.size - 1)
            }
        }

        /**
         * Callback message from server will be handled below
         */
        val callbackMessage by imViewModel.imCallbackMessage.observeAsState()
        LaunchedEffect(callbackMessage) {
            // Fix: Recursive loop in callback message
            // Filter out the IDLE message because after this function imCallbackMessage will be set to IDLE
            if (callbackMessage == null || callbackMessage!!.code == CallBackMessage.CODE_IDLE) {
                return@LaunchedEffect
            }

            when(callbackMessage!!.code) {
                CallBackMessage.CODE_SESSION_BUSY -> {
                    Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "Remote server is busy now")
                    imViewModel.canSendMessage.postValue(true)
                    DialogUtils.showTips(context, context.getString(R.string.activity_session_view_dialog_server_busy))
                        .positive().show()
                }
                CallBackMessage.CODE_SESSION_INVALID -> {
                    Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "Remote server could not find this session: ${JSON.toJSONString(getCurrentSession())}")
                    imViewModel.canSendMessage.postValue(true)
                    DialogUtils.showTips(context, context.getString(R.string.activity_session_view_dialog_message_session_invalid))
                        .positive().show()
                }
                CallBackMessage.CODE_SESSION_MESSAGE_RECEIVED -> {
                    Log.d(GlobalConstants.LOG_TAG_SESSION_NETWORK, "Remote server received your message, messageId: ${callbackMessage!!.message}")
                    currentShowingAssistantMessage = context.getString(R.string.activity_session_view_remote_server_received_message)
                    imViewModel.canSendMessage.postValue(false)
                    if (tMessageGoingToBeSent == null) {
                        Log.w(GlobalConstants.LOG_TAG_SESSION_VIEW_ACTIVITY, "Could not save the message just sent because the message is null")
                        DialogUtils.showTips(context, context.getString(R.string.activity_session_view_dialog_message_is_null))
                            .positive().show()
                    } else {
                        tMessageGoingToBeSent!!.messageId = callbackMessage!!.message.toLong()
                        Log.d(GlobalConstants.LOG_TAG_SESSION_VIEW_ACTIVITY, "Message just sent will be save to local database, [${JSON.toJSONString(tMessageGoingToBeSent)}]")
                        viewModel.addMessage(tMessageGoingToBeSent!!)
                        runAsync {
                            userDatabase.messageDAO().insert(tMessageGoingToBeSent!!)
                        }

                        if (messages.isNotEmpty()) {
                            messageListScrollTo(coroutineScope, messageListState, messages.size - 1)
                        }
                    }
                }
            }

            // Clear callback state to prevent other handler receive this message again
            imViewModel.onReceivedCallbackMessageFromChatServer(CallBackMessage.idle())
        }

        val messageListRefreshing by viewModel.isMessageListRefreshing.observeAsState(false)
        val messageListRefreshState = rememberPullRefreshState(messageListRefreshing, {
            // When the Pull Refresh triggered, this function will be called
            viewModel.messageFetchFlag.value.runAsyncIfNotNull(coroutineScope)  {
                viewModel.loadMessages(
                    it,
                    userDatabase,
                    false,
                    failure = {
                        runOnUiThread {
                            DialogUtils.showTips(
                                context,
                                context.getString(R.string.activity_session_view_dialog_history_message_fetch_failed)
                            ).notCancelable().positive().show()
                        }
                    },
                    noMoreMessages = {
                        runOnUiThread {
                            context.getString(R.string.activity_session_view_toast_no_more_history_messages).showToast(context)
                        }
                    }
                ) { originalTimeFlag, _ ->
                    runOnMain(coroutineScope) {
                        if (messages.isNotEmpty()) {
                            // When earlier history messages be added to the list, scroll to the last one (original top),
                            // because new messages will be added to the top of the list, but user still need to keep in the original top
                            val originalTopIndex = messages.searchIndex { it.datetime == originalTimeFlag }
                            if (originalTopIndex != -1) {
                                messageListScrollTo(coroutineScope, messageListState, originalTopIndex - 1)
                            }
                        }
                    }
                }
            }
        })

        val userMeta by globalViewModel.userMeta.observeAsState()

        Scaffold(
            topBar = {
                CurrencyTopAppBar(
                    title = currentSession?.name ?: "",
                    theme = MaterialTheme,
                    preferences = CustomTopAppBar.Preferences(
                        actions = {
                            TopAppBarActionButton(
                                iconInt = R.drawable.ic_action_menu
                            ) { openActionsMenu(context) }
                        }
                    )
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier
                    .pullRefresh(messageListRefreshState)
                    .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                        state = messageListState
                    ) {
                        itemsIndexed(messages.toList(), key = { index, item -> index }) { index, item ->
                            ChatPopComponent().View(
                                modifier = Modifier,
                                item = item,
                                avatarUrl = if(item.assistant) null else userMeta?.getAvatarUrl(ConfigManager.getInstance().connectionConfig!!.getSettings().currentConnectedServer),
                                onConfirmDelete = {
                                    deleteLocalMessage(context, item)
                                },
                                onConfirmWithdraw = {
                                    withdrawMessage(context, item)
                                },
                                onLongPress = {
                                    onMessageLongPressed(context, item)
                                }
                            )
                        }

                        item {
                            // Streaming data preview
                            if (currentShowingAssistantMessage.isNotBlank()) {
                                ChatPopComponent().View(
                                    modifier = Modifier,
                                    MessageEntity.messageOnly(true, currentShowingAssistantMessage),
                                    showingTools = false
                                )
                            }
                        }
                    }

                    PullRefreshIndicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        refreshing = messageListRefreshing,
                        state = messageListRefreshState,
                        contentColor = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.background
                    )
                }

                BottomBar()

                SessionBranchCreator()
            }
        }
    }
    
    @Composable
    private fun BottomBar(viewModel: SessionViewActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val canSendMessage by imViewModel.canSendMessage.observeAsState()

        var messageInputHeight by remember { mutableStateOf(48.dp) }

        val inputMessage by viewModel.inputMessage.observeAsState("")
        LaunchedEffect(inputMessage) {
            messageInputHeight = 48.dp + ((inputMessage.split("\n").size - 1) * spToDp(context, fontSizeNormal().value) * 1.25f)
        }

        val imClientConnected by imViewModel.imClientConnected.observeAsState()
        LaunchedEffect(imClientConnected) {
            imClientConnected?.runIfFalse {
                DialogUtils.showTips(context, context.getString(R.string.activity_session_view_dialog_disconnected))
                    .positive().show()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (imClientConnected != false)
                        Color.Transparent
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (imClientConnected != false) {
                    OutlinedTextField(
                        modifier = Modifier
                            .height(messageInputHeight)
                            .weight(1f),
                        value = inputMessage,
                        onValueChange = { viewModel.setInputMessage(it)},
                        textStyle = TextStyle(
                            fontSize = fontSizeNormal(),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start
                        )
                    )

                    Button(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            onSendButtonClicked(context, inputMessage)
                        }) {
                        Text(
                            text = if (canSendMessage!!)
                                context.getString(R.string.activity_session_view_button_send)
                            else
                                context.getString(R.string.activity_session_view_button_waiting),
                        )
                    }
                } else {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = context.getString(R.string.activity_session_view_bottom_bar_disconnected),
                        fontSize = fontSizeNormal(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }


            }
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SessionBranchCreator(
        viewModel: SessionViewActivityViewModel = viewModel(),
        sessionCreatorViewModel: SessionCreatorViewModel = viewModel()
    ) {
        val context = LocalContext.current

        val sheetState = rememberModalBottomSheetState()
        val coroutineScope = rememberCoroutineScope()
        val showBottomSheet by viewModel.isSessionBranchCreateVisible.observeAsState()

        val selectedBranchEndpoint by viewModel.selectedBranchEndpoint.observeAsState(MessageEntity())

        val component = SessionCreatorComponent(globalViewModel)

        if (showBottomSheet != false) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.isSessionBranchCreateVisible.postValue(false)
                },
                sheetState = sheetState
            ) {
                Column {
                    component.View(modifier = Modifier.padding(24.dp, 12.dp)) {
                        Text(
                            modifier = Modifier.padding(bottom = 12.dp),
                            text = context.getString(R.string.activity_session_view_message_drawer_branch_creator),
                            fontSize = fontSizeLarge(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            modifier = Modifier.padding(end = 12.dp),
                            onClick = {
                                runOnMain(coroutineScope) { sheetState.hide() }.invokeOnCompletion {
                                    viewModel.isSessionBranchCreateVisible.postValue(false)
                                }
                            }) {
                            Text(text = context.stringCancel())
                        }

                        Button(
                            onClick = {
                                runAsync(coroutineScope) {
                                    val result = component.createSessionBranch(
                                        context,
                                        getCurrentSession(),
                                        selectedBranchEndpoint.datetime,
                                        sessionCreatorViewModel
                                    )
                                    withContext(Dispatchers.Main) {
                                        result.runIfNotNull {
                                            if (it) {
                                                DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_dialog_branch_create_success))
                                                    .positive().show()
                                            } else {
                                                DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_dialog_branch_create_failed))
                                                    .positive().show()
                                            }
                                            viewModel.isSessionBranchCreateVisible.postValue(false)
                                        }
                                    }
                                }
                            }) {
                                Text(text = context.stringConfirm())
                            }
                    }

                }
            }
        }
    }

    // When remote server received message and callback to client,
    // this variable will be insert to local database and show in message list.
    private var tMessageGoingToBeSent: MessageEntity? = null
    private fun onSendButtonClicked(context: Context, inputMessage: String) {
        if (imViewModel.canSendMessage.value == null || !imViewModel.canSendMessage.value!!) {
            return
        }

        if (inputMessage.isEmpty()) {
            DialogUtils.showTips(
                getInstance(),
                context.getString(R.string.activity_session_view_dialog_message_sent_empty)
            ).positive().create().show()
            return
        }

        try {
            sendMessage(inputMessage)
        } catch (e: Exception) {
            DialogUtils.showTips(context, context.getString(R.string.activity_session_view_dialog_message_sent_failed))
                .positive().notCancelable().show()
            e.printStackTrace()
            return
        }

        // Wait for response from remote server...
        viewModel.setInputMessage("")

        tMessageGoingToBeSent = MessageEntity.forUser(0, getCurrentSession().id, inputMessage)
    }

    /**
     * Sent message to remote server
     *
     * @param message Message to be sent
     */
    private fun sendMessage(message: String) {
        if (client == null || !client!!.isConnected()) {
            return
        }

        viewModel.canMessageListScrollToIndex.postValue(true)

        client!!.sendMessage(getCurrentSession().id, message)
    }

    /**
     * When user clicked menu button in AppBar, this function will be called.
     *
     * @param context Context
     */
    private fun openActionsMenu(context: Context) {
        /**
         * When user clicked clear history button, this function will be called
         */
        val fxDeleteHistory = fun () {
            DialogUtils.showTips(
                context,
                context.getString(R.string.activity_session_view_dialog_action_clear_history_tips)
            ).positive(stringCancel()).negative(stringConfirm()) {
                viewModel.currentSession.value.runIfNotNull {
                    userDatabase.clearLocalHistoryMessages(it.id)
                    (context as? ComponentActivity)?.finishAfterTransition()
                    GlobalEventEnum.MAIN_SESSION_LIST_REFRESH_LOCAL.post()
                }
            }.show()
        }

        /**
         * When user clicked statistics button, this function will be called
         */
        val fxShowStatistics = fun () {
            runAsync {
                val result = withContext(Dispatchers.IO) {
                    val messages = userDatabase.messageDAO().getMessagesBySession(getCurrentSession().id)

                    var assistantCount = 0L
                    var userCount = 0L
                    var assistantMessageLength = 0L
                    var userMessageLength = 0L
                    messages.forEach {
                        if (it.assistant) {
                            assistantCount += 1
                            assistantMessageLength += it.message.length
                        } else {
                            userCount += 1
                            userMessageLength += it.message.length
                        }
                    }

                    arrayOf(assistantCount, userCount, assistantMessageLength, userMessageLength, assistantMessageLength + userMessageLength)
                }

                var text = context.getString(R.string.activity_session_view_dialog_action_statistics_text)
                text.apply {
                    text = this.format(result[0], result[2], result[1], result[3], result[4])
                }

                withContext(Dispatchers.Main) {
                    DialogUtils.showTips(
                        context,
                        context.getString(R.string.activity_session_view_dialog_action_statistics_title),
                        text
                    ).positive().show()
                }
            }
        }

        DialogUtils.listSelector(
            context,
            context.getString(R.string.activity_session_view_dialog_actions_title).format(viewModel.currentSession.value?.name ?: "?"),
            arrayOf(
                context.getString(R.string.activity_session_view_dialog_action_clear_history),
                context.getString(R.string.activity_session_view_dialog_action_statistics)
            )
        ) { dialog, index ->
            dialog.dismiss()
                if (index == 0) {
                    fxDeleteHistory()
                } else if (index == 1) {
                    fxShowStatistics()
                }
        }.negative().show()
    }

    /**
     * When the delete action of a message has been confirmed, this function will be called
     *
     * @param context Context
     * @param messageEntity MessageEntity to be deleted in local
     */
    private fun deleteLocalMessage(context: Context, messageEntity: MessageEntity) {
        viewModel.deleteMessage(messageEntity)
        runAsync {
            userDatabase.messageDAO().delete(messageEntity)
        }
    }

    private fun withdrawMessage(context: Context, messageEntity: MessageEntity) {
        runAsync {
            viewModel.withdrawMessage(messageEntity, {
                runOnMain {
                    DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_withdraw_failed))
                        .positive().show()
                }
            }) {
                runAsync {
                    userDatabase.messageDAO().delete(messageEntity)

                }
                runOnMain {
                    DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_withdraw_succeed))
                        .positive().show()
                }
            }
        }
    }

    private fun onMessageLongPressed(context: Context, messageEntity: MessageEntity) {
        DialogUtils.listSelector(
            context,
            context.getString(R.string.activity_session_view_message_dialog_long_press_actions_title),
            arrayOf(
                context.getString(R.string.activity_session_view_message_dialog_long_press_action_create_branch
            )
        )) { dialog, index ->
            dialog.dismiss()
            // Set the selected branch endpoint
            viewModel.selectedBranchEndpoint.postValue(messageEntity)
            createNewBranchDialog(context, messageEntity)
        }.show()
    }

    /**
     * Dialog of Create a New Branch
     *
     * @param context
     * @param messageEntity
     */
    private fun createNewBranchDialog(context: Context, messageEntity: MessageEntity) {
        DialogUtils.showTips(
            context,
            context.getString(R.string.activity_session_view_message_dialog_long_press_action_create_branch),
            context.getString(R.string.activity_session_view_message_dialog_long_press_action_create_branch_text)
        ).positive(stringContinue()) {
            viewModel.isSessionBranchCreateVisible.postValue(true)
        }.negative().show()
    }

    /**
     * When you need to scroll the message list to a specific index, use this function
     * But the effect of this will be restricted by the LiveData canMessageListScrollToIndex in VM
     *
     * @param coroutineScope CoroutineScope
     * @param state LazyListState
     * @param index Target index
     */
    private fun messageListScrollTo(coroutineScope: CoroutineScope, state: LazyListState, index: Int) {
        if (viewModel.canMessageListScrollToIndex.value != false && index >= 0) {
            runOnMain(coroutineScope) {
                state.scrollToItem(index)
            }
        }
    }

    /**
     * Get current SessionEntity,
     * usually it will not be null because this activity could not be started when it is null
     *
     * @return SessionEntity
     */
    private fun getCurrentSession(): SessionEntity = viewModel.currentSession.value!!
}

