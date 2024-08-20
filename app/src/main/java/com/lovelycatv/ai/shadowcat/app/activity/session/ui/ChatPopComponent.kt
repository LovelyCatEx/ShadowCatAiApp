package com.lovelycatv.ai.shadowcat.app.activity.session.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmytroshuba.dailytags.core.simple.SimpleMarkupParser
import com.dmytroshuba.dailytags.core.simple.render
import com.dmytroshuba.dailytags.markdown.rules.MarkdownRules
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity
import com.lovelycatv.ai.shadowcat.app.shadowcompose.imageview.AsyncImageView
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.showToast

class ChatPopComponent {
    @Composable
    fun View(
        modifier: Modifier = Modifier,
        item: MessageEntity,
        showingTools: Boolean = true,
        avatarUrl: String? = null,
        onClick: () -> Unit = fun () {},
        onLongPress: () -> Unit = fun () {},
        onConfirmWithdraw: () -> Unit = fun () {},
        onConfirmDelete: () -> Unit = fun () {}
    ) {
        val isAssistant = item.assistant

        Row(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        },
                        onLongPress = {
                            onLongPress()
                        }
                    )
                },
            horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End
        ) {
            Box(modifier = Modifier.padding(12.dp, 8.dp)) {
                Row {
                    if (isAssistant) {
                        Avatar(avatarUrl)
                        MessageBody(item, modifier = Modifier.weight(1f), showingTools = showingTools, TextAlign.Start, onConfirmWithdraw, onConfirmDelete)
                    } else {
                        MessageBody(item, modifier = Modifier.weight(1f), showingTools = showingTools, TextAlign.Start, onConfirmWithdraw, onConfirmDelete)
                        Avatar(avatarUrl)
                    }
                }
            }
        }
    }

    @Composable
    fun Avatar(avatarUrl: String?) {
        if (!avatarUrl.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(32.dp),
                color = Color.LightGray,
            ) {
                AsyncImageView(
                    imageUrl = avatarUrl
                )
            }
        }
    }

    @Composable
    fun MessageBody(
        item: MessageEntity,
        modifier: Modifier,
        showingTools: Boolean,
        textAlign: TextAlign,
        onWithdraw: () -> Unit,
        onDelete: () -> Unit
    ) {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        var message by remember { mutableStateOf(AnnotatedString("")) }

        val rules = MarkdownRules.toList()
        val parser = SimpleMarkupParser()
        message = parser
            .parse(item.message.trimEnd('\n'), rules)
            .render()
            .toAnnotatedString()

        Column(
            modifier = modifier.padding(12.dp, 4.dp)
        ) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = message,
                textAlign = textAlign,
                fontSize = 16.sp
            )

            if (showingTools) {
                Row(
                    Modifier.padding(top = 4.dp)
                ) {
                    MessageButton(
                        iconId = R.drawable.ic_action_content_copy,
                        description = context.getString(R.string.activity_session_view_message_button_copy)
                    ) {
                        clipboardManager.setText(message)
                        context.getString(R.string.activity_session_view_message_button_copy_tips).showToast(context)
                    }
                    MessageButton(
                        iconId = R.drawable.ic_action_outline_delete,
                        description = context.getString(R.string.activity_session_view_message_button_delete_local)
                    ) {
                        DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_button_delete_local_dialog_text))
                            .positive {
                                onDelete()
                            }.negative().show()
                    }
                    MessageButton(
                        iconId = R.drawable.ic_action_rotate_left,
                        description = context.getString(R.string.activity_session_view_message_button_withdraw)
                    ) {
                        DialogUtils.showTips(context, context.getString(R.string.activity_session_view_message_dialog_button_withdraw_text))
                            .positive {
                                onWithdraw()
                            }.negative().show()
                    }
                }
            }
        }
    }

    @Composable
    fun MessageButton(@DrawableRes iconId: Int, description: String, onClick: () -> Unit) {
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .size(24.dp)
                .clickable { onClick() },
        ) {
            Box(modifier = Modifier.padding(4.dp)) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = iconId),
                    contentDescription = description
                )
            }
        }

    }
}