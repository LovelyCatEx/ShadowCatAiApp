package com.lovelycatv.ai.shadowcat.app.activity.main.page

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.main.viewmodel.MainActivityViewModel
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.SessionCreatorActivity
import com.lovelycatv.ai.shadowcat.app.activity.session.view.SessionViewActivity
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.net.retrofit.provideToken
import com.lovelycatv.ai.shadowcat.app.shadowcompose.fragment.ActivityBasedPage
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeSmall
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.runInTransactionAsync
import com.lovelycatv.ai.shadowcat.app.util.android.showToast
import com.lovelycatv.ai.shadowcat.app.util.common.runIfFalse
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEvent
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEventBus
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEventEnum
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.flushGlobalEvent
import com.lovelycatv.ai.shadowcat.app.viewmodel.getCurrentToken
import com.lovelycatv.ai.shadowcat.app.viewmodel.getCurrentUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainHomePage(
    private var globalViewModel: GlobalViewModel,
) : ActivityBasedPage() {

    @Composable
    override fun View() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SessionsInformationBar(Modifier.padding(24.dp, 12.dp))
            SessionList()
        }
    }

    private fun refreshSessions(context: Context, viewModel: MainActivityViewModel, fromRemote: Boolean) {
        Log.d("refreshSessions", fromRemote.toString())
        val db = getCurrentUserDatabase(context)
        viewModel.loadSessions(getCurrentToken(), db, fromRemote) { result ->
            runOnUiThread {
                result.runIfFalse {
                    context.getString(R.string.activity_main_page_main_sessions_fetch_failed).showToast(it!!)
                }
            }
        }
    }

    @SuppressLint("MutableCollectionMutableState")
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
    @Composable
    fun SessionList(viewModel: MainActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val sessions by viewModel.sessions.observeAsState(emptyList())

        LaunchedEffect(Unit) {
            if (sessions.isEmpty()) {
                refreshSessions(context, viewModel, true)
            } else {
                refreshSessions(context, viewModel, false)
            }
        }

        val db = getCurrentUserDatabase(context)
        val noMessageString = context.getString(R.string.activity_main_page_main_session_no_message)
        var displayedSessions by remember { mutableStateOf(listOf<SessionEntity.Display>()) }
        LaunchedEffect(sessions) {
            displayedSessions = sessions.map {
                val lastMessage = withContext(Dispatchers.IO) {
                    db.messageDAO().getLastMessageOfSession(it.id)
                }?.message ?: noMessageString
                SessionEntity.Display(it, lastMessage)
            }.toList()
        }

        // Global Event Listener
        val globalEvent by GlobalEventBus.instance.currentEvent.observeAsState(GlobalEvent.idle())
        LaunchedEffect(globalEvent) {
            when (globalEvent.event) {
                GlobalEventEnum.MAIN_SESSION_LIST_REFRESH_LOCAL -> {
                    refreshSessions(context, viewModel, false)
                }
                GlobalEventEnum.MAIN_SESSION_LIST_REFRESH_NETWORK -> refreshSessions(context, viewModel, true)
                else -> {}
            }
            flushGlobalEvent()
        }

        val sessionRefreshing by viewModel.isSessionRefreshing.observeAsState()
        val sessionRefreshState = rememberPullRefreshState(sessionRefreshing!!, { refreshSessions(context, viewModel, true) })
        Box(modifier = Modifier.pullRefresh(sessionRefreshState)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayedSessions) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    SessionViewActivity.toThisActivity(context, item.sessionEntity)
                                },
                                onLongClick = {
                                    onSessionItemLongClickEvent(
                                        coroutineScope,
                                        context,
                                        item.sessionEntity
                                    )
                                }
                            )

                    ) {
                        Box(modifier = Modifier.padding(24.dp, 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    painter = painterResource(id = R.drawable.ic_action_camera),
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = item.sessionEntity.name
                                )
                                Column(
                                    modifier = Modifier.padding(12.dp, 0.dp)
                                ) {
                                    Text(
                                        text = item.sessionEntity.name,
                                        fontSize = fontSizeNormal(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = item.mostRecentMessage,
                                        fontSize = fontSizeSmall(),
                                        color = MaterialTheme.colorScheme.secondary,
                                        lineHeight = fontSizeSmall(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                refreshing = sessionRefreshing!!,
                state = sessionRefreshState,
                contentColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.background
            )
        }
    }

    @Composable
    fun SessionsInformationBar(modifier: Modifier, viewModel: MainActivityViewModel = viewModel()) {
        val context = LocalContext.current

        val sessions by viewModel.sessions.observeAsState(emptyList())

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.activity_main_page_main_session_count).format(sessions.size),
                fontSize = fontSizeNormal(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        SessionCreatorActivity.toThisActivity(
                            context,
                            SessionCreatorActivity.SESSION_CREATOR_MODE_NEW
                        )
                    },
            ) {
                Image(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_action_add),
                    contentDescription = null
                )
            }
        }
    }

    private fun onSessionItemLongClickEvent(coroutineScope: CoroutineScope, context: Context, sessionEntity: SessionEntity) {
        DialogUtils.listSelector(
            context,
            context.getString(R.string.activity_main_page_main_session_actions).format(sessionEntity.name),
            arrayOf(
                context.getString(R.string.activity_main_page_main_session_action_edit),
                context.getString(R.string.activity_main_page_main_session_action_delete)
            )
        ) { dialog, index ->
            if (index == 0) {
                SessionCreatorActivity.toThisActivity(context, SessionCreatorActivity.SESSION_CREATOR_MODE_EDIT, sessionEntity)
            } else if (index == 1) {
                DialogUtils.showTips(
                    context,
                    context.getString(R.string.activity_main_page_main_session_action_confirm_delete),
                    context.getString(R.string.activity_main_page_main_session_dialog_action_delete)
                ).positive {
                    dialog.dismiss()
                    getShadowCatServerApi().provideToken { api, token ->
                        coroutineScope.launch(Dispatchers.IO) {
                            api.deleteSession(token, sessionEntity.id)
                            val db = getCurrentUserDatabase(context)
                            db.runInTransactionAsync(coroutineScope) {
                                it.sessionDAO().delete(sessionEntity)
                                it.messageDAO().deleteBySessionId(sessionEntity.id)
                            }
                        }
                    }
                }.negative {
                    dialog.dismiss()
                }.show()

            }
        }.show()
    }
}