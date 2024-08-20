package com.lovelycatv.ai.shadowcat.app.activity.start

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.login.LoginActivity
import com.lovelycatv.ai.shadowcat.app.activity.main.MainActivity
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.exception.ConfigManagerNotInitializedException
import com.lovelycatv.ai.shadowcat.app.ui.theme.ShadowCatTheme
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.notCancelable
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.common.ForceDelay
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import com.lovelycatv.ai.shadowcat.app.viewmodel.autoRefreshCurrentUserStatus

class StartingActivity : ComponentActivity() {
    private val globalViewModel: GlobalViewModel = GlobalViewModel.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ConfigManager
        ConfigManager.getInstance().init(this)
        if (!ConfigManager.getInstance().isInitialized()) {
            throw ConfigManagerNotInitializedException("ConfigManager does not initialize properly")
        }

        globalViewModel.initialize(this)

        enableEdgeToEdge()
        setContent {
            StartingActivityView()
        }
    }


    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun StartingActivityView() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val configManager = ConfigManager.getInstance()

        LaunchedEffect(Unit) {
            try {
                val connectionSettings = configManager.connectionConfig!!.getSettings()

                ForceDelay<Boolean>(1000) { refreshStatus, _ ->
                    // Finish logged user information refreshing
                    if (refreshStatus != false) {
                        // Turn to MainActivity
                        context.startActivity(Intent(context, MainActivity::class.java))
                    } else {
                        DialogUtils.showTips(context, context.getString(R.string.activity_starting_dialog_sync_failed_message))
                            .positive(getString(R.string.activity_starting_dialog_sync_failed_message_button_login)) {
                                it.dismiss()
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            }
                            .negative(getString(R.string.activity_starting_dialog_sync_failed_message_button_ignore)) {
                                context.startActivity(Intent(context, MainActivity::class.java))
                            }
                            .notCancelable()
                            .show()
                    }
                }.start {
                    autoRefreshCurrentUserStatus()
                }
            } catch (e: Exception) {
                // Not connect to a server yet
                // Going to LoginActivity
                e.printStackTrace()
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }

        ShadowCatTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = context.getString(R.string.app_name),
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(modifier = Modifier.padding(24.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                Text(
                    modifier = Modifier,
                    text = context.getString(R.string.activity_starting_text_sync_message),
                    fontSize = fontSizeNormal(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}