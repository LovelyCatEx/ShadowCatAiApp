package com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

class CustomTopAppBar {
    data class Preferences(
        val usingDefaultReturnButton: Boolean = true,
        val navigationButton: (@Composable () -> Unit)? = null,
        val actions: (@Composable () -> Unit)? = null,
        val onReturnButtonClick: (Context) -> Unit = fun (context: Context) {
            (context as? ComponentActivity)?.finishAfterTransition()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyTopAppBar(
    title: String,
    theme: MaterialTheme,
    preferences: CustomTopAppBar.Preferences
) {
    val context = LocalContext.current

    var appBarTitle by remember { mutableStateOf(title) }

    LaunchedEffect(title) {
        appBarTitle = title
    }

    TopAppBar(
        title = { Text(text = appBarTitle) },
        navigationIcon = {
            if (preferences.navigationButton == null && preferences.usingDefaultReturnButton) {
                IconButton(onClick = { preferences.onReturnButtonClick.invoke(context) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                preferences.navigationButton?.invoke()
            }
        },
        actions = {
            Row {
                preferences.actions?.invoke()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = theme.colorScheme.primary,
            titleContentColor = theme.colorScheme.onPrimary,
            actionIconContentColor = theme.colorScheme.onPrimary,
            navigationIconContentColor = theme.colorScheme.onPrimary
        )
    )
}

@Composable
fun TopAppBarActionButton(
    @DrawableRes iconInt: Int,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
    description: String? = null,
    onClick: (() -> Unit)? = fun () {}
) {
    IconButton(
        modifier = modifier,
        onClick = { onClick?.let { it() } }
    ) {
        Icon(painterResource(id = iconInt), contentDescription = description, tint = tint)
    }
}