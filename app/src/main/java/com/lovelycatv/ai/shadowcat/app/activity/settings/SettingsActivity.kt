package com.lovelycatv.ai.shadowcat.app.activity.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.base.BaseComponentActivity
import com.lovelycatv.ai.shadowcat.app.activity.login.LoginActivity
import com.lovelycatv.ai.shadowcat.app.activity.login.component.AccountSelectorComponent
import com.lovelycatv.ai.shadowcat.app.activity.settings.viewmodel.SettingsActivityViewModel
import com.lovelycatv.ai.shadowcat.app.database.general.ShadowCatGeneralDatabase
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CurrencyTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CustomTopAppBar
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeSmall
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import kotlinx.coroutines.delay

class SettingsActivity : BaseComponentActivity<SettingsActivity>() {
    private val viewModel: SettingsActivityViewModel by viewModels()
    private val accountSelectorComponent = AccountSelectorComponent()

    override fun doOnCreate(): @Composable () -> Unit {

        return {
            SettingsActivityView()
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    private fun SettingsActivityView(
        viewModel: SettingsActivityViewModel = viewModel()
    ) {
        val context = LocalContext.current

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                CurrencyTopAppBar(
                    title = context.getString(R.string.title_activity_settings),
                    theme = MaterialTheme,
                    preferences = CustomTopAppBar.Preferences()
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                SettingsCategoryTitle(text = context.getString(R.string.activity_settings_category_accounts))

                accountSelectorComponent.View { server, account ->
                    DialogUtils.showTips(
                        context,
                        context.getString(R.string.activity_settings_dialog_switch_account_dialog_title),
                        context.getString(R.string.activity_settings_dialog_switch_account_dialog_text).format(account.username)
                    ).positive {
                        ShadowCatGeneralDatabase.getInstance(context).changeAccount(account)
                    }.negative().show()
                }

                SettingsCategoryTitle(text = context.getString(R.string.activity_settings_category_actions))
                SettingsItem(
                    title = context.getString(R.string.activity_settings_action_logout),
                    icon = R.drawable.ic_action_logout,
                    onClick = {
                        DialogUtils.showTips(
                            context,
                            context.getString(R.string.activity_settings_action_logout),
                            context.getString(R.string.activity_settings_dialog_action_logout)
                        ).positive {
                            runAsync {
                                ShadowCatGeneralDatabase.getInstance(context).logout()
                                delay(200)
                                getInstance().startActivity(Intent(getInstance(), LoginActivity::class.java))
                            }
                        }.negative().show()
                    }
                )

                SettingsCategoryTitle(text = context.getString(R.string.activity_settings_category_about))

                SettingsItem(
                    title = context.getString(R.string.activity_settings_about_app_version_title),
                    summary = "%s Build %s".format(GlobalConstants.APP_VERSION, GlobalConstants.APP_VERSION_CODE),
                    icon = R.drawable.ic_action_app
                )
                SettingsItem(
                    title = context.getString(R.string.activity_settings_about_author_title),
                    summary = GlobalConstants.APP_AUTHOR,
                    icon = R.drawable.ic_action_person,
                    onClick = {
                        DialogUtils.showTips(
                            context,
                            GlobalConstants.APP_AUTHOR,
                            context.getString(R.string.activity_settings_about_dialog_to_authors_page)
                        ).positive(R.string.activity_settings_about_dialog_to_authors_page_confirm) {
                            getInstance().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConstants.APP_AUTHOR_URL)))
                        }.negative(R.string.activity_settings_about_dialog_to_authors_page_cancel).show()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.padding(24.dp, 12.dp),
        text = text,
        fontSize = fontSizeNormal(),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    @DrawableRes icon: Int? = null,
    iconTint: Color  = MaterialTheme.colorScheme.secondary,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .clickable { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier.padding(24.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconTint
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    fontSize = fontSizeNormal(),
                    color = MaterialTheme.colorScheme.primary
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        fontSize = fontSizeSmall(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
