package com.lovelycatv.ai.shadowcat.app.activity.session.creator

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.base.BaseComponentActivity
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.composable.SessionCreatorComponent
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.viewmodel.SessionCreatorViewModel
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.exception.session.InvalidSessionException
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CurrencyTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CustomTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.TopAppBarActionButton
import com.lovelycatv.ai.shadowcat.app.ui.theme.ShadowCatTheme
import com.lovelycatv.ai.shadowcat.app.util.android.showToast
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.common.toExplicitObject
import com.lovelycatv.ai.shadowcat.app.util.common.toJSONString
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEventEnum
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionCreatorActivity : BaseComponentActivity<SessionCreatorActivity>() {
    companion object {
        const val SESSION_CREATOR_MODE_NEW = 0
        const val SESSION_CREATOR_MODE_EDIT = 1

        @JvmStatic
        fun toThisActivity(
            context: Context,
            @IntRange(from = 0, to = 1) mode: Int,
            data: SessionEntity? = null
        ) {
            context.startActivity(Intent(context, SessionCreatorActivity::class.java).apply {
                this.putExtra("mode", mode)
                data?.let {
                    this.putExtra("data", data.toJSONString())
                }
            })
        }
    }

    private val globalViewModel: GlobalViewModel = GlobalViewModel.instance
    private val viewModel: SessionCreatorViewModel by viewModels()
    private val component = SessionCreatorComponent(globalViewModel)

    private var mode = SESSION_CREATOR_MODE_NEW

    override fun doOnCreate(): @Composable () -> Unit {
        val intentExtras = intent.extras!!
        mode = intentExtras.getInt("mode")
        if (mode == SESSION_CREATOR_MODE_EDIT) {
            val sessionEntity: SessionEntity = (intentExtras.getString("data") ?: throw InvalidSessionException()).toExplicitObject()
            viewModel.setCurrentEditingEntity(sessionEntity)
            viewModel.setSessionName(sessionEntity.name)
            viewModel.models.observeForever { models ->
                if (!models.isNullOrEmpty()) {
                    viewModel.setSelectedModel(models.first { it.id == sessionEntity.modelId })
                }
            }
        }

        return {
            ShadowCatTheme {
                SessionCreatorView()
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun SessionCreatorView(viewModel: SessionCreatorViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val systemUiController = rememberSystemUiController()
        systemUiController.systemBarsDarkContentEnabled = false

        val title = context.getString(R.string.title_activity_session_creator)

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                CurrencyTopAppBar(
                    title = title,
                    theme = MaterialTheme,
                    preferences = CustomTopAppBar.Preferences(
                        usingDefaultReturnButton = true,
                        actions = {
                            TopAppBarActionButton(iconInt = R.drawable.ic_action_save) {
                                onSaveButtonClickEvent(coroutineScope, context, viewModel)
                            }
                        }
                    )
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                component.View(modifier = Modifier.padding(24.dp, 12.dp))
            }
        }
    }

    private fun onSaveButtonClickEvent(coroutineScope: CoroutineScope, context: Context, viewModel: SessionCreatorViewModel) {
        runAsync(coroutineScope) {
            val result = when (mode) {
                SESSION_CREATOR_MODE_NEW -> {
                    component.createSession(context, viewModel)
                }
                SESSION_CREATOR_MODE_EDIT -> {
                    component.saveSession(context, viewModel)
                }
                else -> {
                    false
                }
            }

            if (result) {
                getInstance().finishAfterTransition()
                GlobalEventEnum.MAIN_SESSION_LIST_REFRESH_NETWORK.post()
            } else {
                withContext(Dispatchers.Main) {
                    context.getString(R.string.activity_session_creator_dialog_session_create_failed)
                        .format(viewModel.sessionName.value)
                        .showToast(context)
                }
            }
        }
    }
}