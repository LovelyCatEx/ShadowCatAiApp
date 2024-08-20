package com.lovelycatv.ai.shadowcat.app.util.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role


inline fun Modifier.securedClick(
    time: Int = 800,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    crossinline onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(value = 0L) }
    this.then(
        clickable(enabled, onClickLabel, role) {
            val currentTimeMillis = System.currentTimeMillis()
            if (currentTimeMillis - time >= lastClickTime) {
                onClick()
                lastClickTime = currentTimeMillis
            }
        }
    )
}

fun Modifier.longPress(onLongClick: (Offset) -> Unit): Modifier =
    then(
        pointerInput(this) {
            detectTapGestures(
                onLongPress = onLongClick
            )
        }
    )

class ComponentClickExtensions {
}