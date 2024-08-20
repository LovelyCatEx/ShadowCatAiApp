package com.lovelycatv.ai.shadowcat.app.activity.main.page

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lovelycatv.ai.shadowcat.app.shadowcompose.fragment.ActivityBasedPage

class MainDynamicPage() : ActivityBasedPage() {
    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    override fun View() {
        Text(text = "Dynamic")
    }
}