package com.lovelycatv.ai.shadowcat.app.shadowcompose.fragment

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable

abstract class ActivityBasedPage {
    private lateinit var parentActivity: Activity
    fun setParentActivity(activity: Activity) {
        this.parentActivity = activity
    }

    @Composable
    abstract fun View()

    fun getContext() = this.parentActivity

    fun getString(resId: Int) = this.parentActivity.getString(resId)

    fun runOnUiThread(fx: (context: Context?) -> Unit) {
        parentActivity.runOnUiThread {
            fx(parentActivity)
        }
    }
}