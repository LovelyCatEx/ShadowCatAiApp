package com.lovelycatv.ai.shadowcat.app.util.android

import android.content.Context
import android.widget.Toast

fun CharSequence?.showToast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, this, duration).show()
}

fun CharSequence?.showLongToast(context: Context) {
    showToast(context, Toast.LENGTH_LONG)
}