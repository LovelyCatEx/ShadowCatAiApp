package com.lovelycatv.ai.shadowcat.app.activity.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.base.BaseComponentActivity
import com.lovelycatv.ai.shadowcat.app.activity.login.component.AccountSelectorComponent
import com.lovelycatv.ai.shadowcat.app.activity.login.viewmodel.LoginActivityViewModel
import com.lovelycatv.ai.shadowcat.app.activity.start.StartingActivity
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.database.general.ShadowCatGeneralDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionEntity
import com.lovelycatv.ai.shadowcat.app.net.response.NetworkResult
import com.lovelycatv.ai.shadowcat.app.net.response.remote.LoginResult
import com.lovelycatv.ai.shadowcat.app.net.retrofit.ShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.net.retrofit.asyncActions
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CurrencyTopAppBar
import com.lovelycatv.ai.shadowcat.app.shadowcompose.appbar.CustomTopAppBar
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.notCancelable
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.showToast
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class LoginActivity : BaseComponentActivity<LoginActivity>() {
    private lateinit var generalDatabase: ShadowCatGeneralDatabase

    override fun doOnCreate(): @Composable () -> Unit {
        generalDatabase = ShadowCatGeneralDatabase.getInstance(this)

        return {
            LoginActivityView()
        }
    }

    private var tLastPressKeyBackTimeMills = 0L
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - tLastPressKeyBackTimeMills > 1000) {
                getString(R.string.activity_main_toast_press_again_exit).showToast(this)
                tLastPressKeyBackTimeMills = System.currentTimeMillis()
            } else {
                finishAffinity()
            }

            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun LoginActivityView(viewModel: LoginActivityViewModel = viewModel()) {
        val context = LocalContext.current

        Scaffold(
            topBar = {
                CurrencyTopAppBar(
                    title = context.getString(R.string.title_activity_login),
                    theme = MaterialTheme,
                    preferences = CustomTopAppBar.Preferences(
                        onReturnButtonClick = {
                            finishAffinity()
                        }
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LoginPage()
            }
        }
    }

    @Composable
    fun LoginPage(modifier: Modifier = Modifier, viewModel: LoginActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val address by viewModel.address.observeAsState("")
        val port by viewModel.port.observeAsState("")
        val chatPort by viewModel.chatPort.observeAsState("")
        val username by viewModel.username.observeAsState("")
        val password by viewModel.password.observeAsState("")

        Column {
            Column(modifier = modifier.padding(24.dp, 0.dp)) {
                FormCategoryText(text = context.getString(R.string.activity_login_form_category_accounts))
            }

            AccountSelectorComponent().View { server, account ->
                viewModel.address.postValue(server.address)
                viewModel.port.postValue(server.port.toString())
                viewModel.chatPort.postValue(server.chatPort.toString())
                viewModel.username.postValue(account.username)
                viewModel.password.postValue(account.password)
            }

            Column(modifier = modifier.padding(24.dp, 0.dp)) {
                FormCategoryText(text = context.getString(R.string.activity_login_form_category_connection))
                with(context.getString(R.string.activity_login_form_text_field_host)) {
                    CustomLoginTextField(
                        label = this,
                        value = address
                    ) {
                        viewModel.address.postValue(it)
                    }
                }

                with(context.getString(R.string.activity_login_form_text_field_port)) {
                    CustomLoginTextField(
                        label = this,
                        value = port
                    ) {
                        viewModel.port.postValue(it)
                    }
                }

                with(context.getString(R.string.activity_login_form_text_field_chat_port)) {
                    CustomLoginTextField(
                        label = this,
                        value = chatPort
                    ) {
                        viewModel.chatPort.postValue(it)
                    }
                }

                FormCategoryText(text = context.getString(R.string.activity_login_form_category_account))

                with(context.getString(R.string.activity_login_form_text_field_username)) {
                    CustomLoginTextField(
                        label = this,
                        value = username
                    ) {
                        viewModel.username.postValue(it)
                    }
                }

                with(context.getString(R.string.activity_login_form_text_field_password)) {
                    CustomLoginTextField(
                        label = this,
                        value = password,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation()
                    ) {
                        viewModel.password.postValue(it)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            runAsync(coroutineScope) {
                                doLogin(
                                    context,
                                    ServerConnectionEntity(0, "", address, port.toInt(), chatPort.toInt()),
                                    AccountEntity(0, 0, 0, username, password, "", "", "")
                                )
                            }
                        },
                        contentPadding = PaddingValues(32.dp, 0.dp)
                    ) {
                        Text(text = context.getString(R.string.activity_login_form_button_login))
                    }
                }
            }
        }


    }

    @Composable
    private fun CustomLoginTextField(
        modifier: Modifier = Modifier,
        label: String,
        value: String,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        onValueChange: (String) -> Unit
    ) {
        TextField(
            modifier = modifier.fillMaxWidth(),
            placeholder = { Text(text = label) },
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(text = label) },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            maxLines = 1
        )
    }
    
    @Composable
    private fun FormCategoryText(modifier: Modifier = Modifier, text: String) {
        Text(
            modifier = modifier.padding(0.dp, 12.dp),
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontSize = fontSizeNormal()
        )
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun doLogin(context: Context, serverConnectionEntity: ServerConnectionEntity, accountEntity: AccountEntity) {
        val config = ConfigManager.getInstance().connectionConfig!!

        serverConnectionEntity.address = serverConnectionEntity.address.run {
            if (!this.startsWith("http://") && !this.startsWith("https://")) {
                "http://$this"
            } else {
                this
            }
        }

        val progressDialog = withContext(Dispatchers.Main) {
            DialogUtils.showProgress(
                context,
                context.getString(R.string.activity_login_dialog_progress_login)
            ).notCancelable().show()
        }

        val handleFailure = fun (data: NetworkResult<*>) {
            runOnUiThread {
                DialogUtils.showTips(getInstance(), "${data.code}: ${data.message}").positive { it.dismiss() }.show()
                progressDialog.dismiss()
            }
        }

        withContext(Dispatchers.IO) {
            try {
                ShadowCatServerApi("${serverConnectionEntity.address}:${serverConnectionEntity.port}").api.login(
                    accountEntity.username,
                    accountEntity.password
                ).asyncActions({ handleFailure(it) }) {
                    val loginResult = it.getExplicitData(LoginResult::class.java)!!

                    val serverId = with(
                        generalDatabase.serverConnectionDAO().exists(
                            serverConnectionEntity.address,
                            serverConnectionEntity.port,
                            serverConnectionEntity.chatPort
                        )
                    ) {
                        this?.id ?: generalDatabase.serverConnectionDAO()
                            .insert(serverConnectionEntity).toInt()
                    }

                    serverConnectionEntity.id = serverId
                    generalDatabase.serverConnectionDAO().update(serverConnectionEntity)

                    accountEntity.userId = loginResult.uid
                    accountEntity.serverId = serverId

                    val accountId = with(
                        generalDatabase.accountDAO().exists(serverId, accountEntity.username)
                    ) {
                        this?.id ?: generalDatabase.accountDAO().insert(accountEntity).toInt()
                    }

                    accountEntity.id = accountId

                    generalDatabase.accountDAO().update(accountEntity)

                    config.updateSettingItem(
                        GlobalConstants.SETTING_CONNECTED_SERVER_ID,
                        serverId.toString()
                    )
                    config.updateSettingItem(
                        GlobalConstants.SETTING_CONNECTED_ACCOUNT_ID,
                        accountId.toString()
                    )
                    config.updateSettingItem(
                        GlobalConstants.SETTING_CURRENT_TOKEN,
                        loginResult.token
                    )
                    config.updateSettingItem(
                        GlobalConstants.SETTING_CURRENT_USER_ID,
                        loginResult.uid.toString()
                    )

                    progressDialog.dismiss()

                    delay(200)
                    getInstance().startActivity(Intent(getInstance(), StartingActivity::class.java))
                }
            } catch (e: Exception) {
                handleFailure(NetworkResult.empty())
                e.printStackTrace()
            }
        }

    }
}