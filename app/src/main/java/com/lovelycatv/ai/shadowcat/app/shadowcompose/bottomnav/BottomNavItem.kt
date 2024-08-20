package com.lovelycatv.ai.shadowcat.app.shadowcompose.bottomnav

import androidx.annotation.DrawableRes

data class BottomNavItem(
    var name: String,
    @DrawableRes var iconRes: Int,
    var routeName: String
)