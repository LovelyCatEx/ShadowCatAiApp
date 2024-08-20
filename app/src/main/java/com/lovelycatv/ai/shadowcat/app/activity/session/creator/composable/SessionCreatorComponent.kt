package com.lovelycatv.ai.shadowcat.app.activity.session.creator.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.session.creator.viewmodel.SessionCreatorViewModel
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.net.retrofit.asyncActions
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.stringYes
import com.lovelycatv.ai.shadowcat.app.util.common.searchIndex
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionCreatorComponent(
    private val globalViewModel: GlobalViewModel = GlobalViewModel.instance
) {
    @Composable
    fun View(
        modifier: Modifier = Modifier,
        viewModel: SessionCreatorViewModel = viewModel(),
        before: (@Composable () -> Unit)? = null
    ) {
        val context = LocalContext.current

        val sessionName by viewModel.sessionName.observeAsState("")

        Column {
            Column(modifier = modifier) {
                before?.invoke()

                Text(
                    text = context.getString(R.string.activity_session_creator_title_edit_session_name),
                    fontSize = fontSizeNormal(),
                    color = MaterialTheme.colorScheme.primary
                )
                TextField   (
                    modifier = Modifier.fillMaxWidth(),
                    value = sessionName,
                    onValueChange = { viewModel.setSessionName(it) },
                    textStyle = TextStyle(
                        fontSize = 16.sp
                    )
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = context.getString(R.string.activity_session_creator_title_model_selection),
                    fontSize = fontSizeNormal(),
                    color = MaterialTheme.colorScheme.primary)
            }

            ModelListView()
        }
    }

    @Composable
    fun ModelListView(viewModel: SessionCreatorViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            viewModel.fetchModels(globalViewModel.token.value ?: "")
        }

        val models by viewModel.models.observeAsState(emptyList())

        val selectedModel by viewModel.selectedModel.observeAsState()
        var selectedModelIndex by remember { mutableIntStateOf(-1) }

        LaunchedEffect(selectedModel) {
            selectedModelIndex = models.searchIndex {
                it.id == (selectedModel?.id ?: -1)
            }
        }

        LazyColumn {
            itemsIndexed(models) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 0.dp)
                        .background(
                            if (selectedModelIndex == index)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                Color.Transparent
                        )
                        .clickable {
                            // Show details of the model
                            DialogUtils
                                .showTips(
                                    context,
                                    item.name,
                                    context
                                        .getString(R.string.activity_session_creator_dialog_model_selection)
                                        .format(item.description)
                                )
                                .positive(context.stringYes()) {
                                    selectedModelIndex = index
                                    viewModel.setSelectedModel(item)
                                    it.dismiss()
                                }
                                .negative()
                                .show()
                        },
                ) {
                    Box(modifier = Modifier.padding(24.dp, 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically  ) {
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                painter = painterResource(id = R.drawable.ic_action_camera),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = item.name
                            )
                            Column(
                                modifier = Modifier.padding(12.dp, 0.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 16.sp,
                                    color = if (selectedModelIndex == index) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = if (selectedModelIndex == index) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary,
                                    lineHeight = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private suspend fun sessionCreatorCheck(context: Context, viewModel: SessionCreatorViewModel): Boolean {
        return withContext(Dispatchers.Main) {
            if (viewModel.sessionName.value.isNullOrBlank()) {
                DialogUtils.showTips(context, context.getString(R.string.activity_session_creator_dialog_session_name_empty)).positive().show()
                false
            } else if (viewModel.selectedModel.value == null) {
                DialogUtils.showTips(context, context.getString(R.string.activity_session_creator_dialog_model_empty)).positive().show()
                false
            } else true
        }
    }

    suspend fun createSessionBranch(
        context: Context,
        originalSession: SessionEntity,
        beforeWhen: Long = 0L,
        viewModel: SessionCreatorViewModel
    ): Boolean? {
        if (!sessionCreatorCheck(context, viewModel)) {
            return null
        }

        val api = getShadowCatServerApi().second
        try {
            val result = api.createSessionBranch(
                globalViewModel.token.value ?: "",
                originalSession.id,
                beforeWhen,
                viewModel.sessionName.value ?: "",
                viewModel.selectedModel.value?.id ?: 0
            ).asyncActions({ false }) {
                true
            }

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    suspend fun createSession(context: Context, viewModel: SessionCreatorViewModel): Boolean {
        if (!sessionCreatorCheck(context, viewModel)) {
            return false
        }

        return with(getShadowCatServerApi()) {
            this.second.createSession(
                globalViewModel.token.value ?: "",
                viewModel.sessionName.value ?: "",
                viewModel.selectedModel.value?.id ?: 0
            ).asyncActions({ false }) { true }
        }
    }

    suspend fun saveSession(context: Context, viewModel: SessionCreatorViewModel): Boolean {
        if (!sessionCreatorCheck(context, viewModel)) {
            return false
        }

        return with(getShadowCatServerApi()) {
            this.second.updateSession(
                globalViewModel.token.value ?: "",
                viewModel.editingSessionEntity.value!!.id,
                viewModel.sessionName.value ?: "",
                viewModel.selectedModel.value?.id ?: 0
            ).asyncActions({ false }) { true }
        }
    }
}