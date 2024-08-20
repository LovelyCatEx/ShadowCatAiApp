package com.lovelycatv.ai.shadowcat.app.util.android.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class StringProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = listOf("").asSequence()
}