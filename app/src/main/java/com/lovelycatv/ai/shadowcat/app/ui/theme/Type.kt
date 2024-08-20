package com.lovelycatv.ai.shadowcat.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

fun TextStyle.clone() = TextStyle(
    color = this.color,
    fontSize = this.fontSize,
    fontWeight = this.fontWeight,
    fontStyle = this.fontStyle,
    fontSynthesis = this.fontSynthesis,
    fontFamily = this.fontFamily,
    fontFeatureSettings = this.fontFeatureSettings,
    letterSpacing = this.letterSpacing,
    baselineShift = this.baselineShift,
    textGeometricTransform = this.textGeometricTransform,
    localeList = this.localeList,
    background = this.background,
    textDecoration = this.textDecoration,
    shadow = this.shadow,
    textAlign = this.textAlign,
    textDirection = this.textDirection,
    lineHeight = this.lineHeight,
    textIndent = this.textIndent
)

fun fontSizeLpp() = 28.sp
fun fontSizeLp() = 24.sp
fun fontSizeLarge() = 20.sp
fun fontSizeNormal() = 16.sp
fun fontSizeSmall() = 12.sp
