package com.lovelycatv.ai.shadowcat.app.util.android

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun spToDp(context: Context, sp: Float): Dp {
    val resources = context.resources
    val scaledDensity = resources.displayMetrics.scaledDensity
    val density = resources.displayMetrics.density

    // Convert sp to px first
    val px = sp * scaledDensity

    // Convert px to dp
    return (px / density).dp
}