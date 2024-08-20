package com.lovelycatv.ai.shadowcat.app.activity.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.login.viewmodel.AccountSelectorViewModel
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionEntity
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeLarge
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeSmall
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils

class AccountSelectorComponent {
    @Composable
    fun View(
        modifier: Modifier = Modifier,
        viewModel: AccountSelectorViewModel = viewModel(),
        onAccountSelected: (ServerConnectionEntity, AccountEntity) -> Unit = fun (_, _) {}
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        viewModel.initialize(context)

        val servers by viewModel.serversWithAccounts.observeAsState(emptyList())

        LazyColumn(modifier = modifier) {
            itemsIndexed(servers) { _, server ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable {
                            DialogUtils.singleChoice(
                                context,
                                context.getString(R.string.activity_login_dialog_switch_account),
                                server.accounts.map { it.username }.toTypedArray(),
                                0
                            ) { dialog, index ->
                                dialog.dismiss()
                                onAccountSelected(server.serverConnection, server.accounts[index])
                            }.show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = server.serverConnection.id.toString(),
                            fontSize = fontSizeLarge(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(24.dp, 0.dp)) {
                            Text(
                                text = server.serverConnection.address,
                                fontSize = fontSizeNormal(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${server.serverConnection.port}/${server.serverConnection.chatPort}",
                                fontSize = fontSizeSmall(),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }

    }
}