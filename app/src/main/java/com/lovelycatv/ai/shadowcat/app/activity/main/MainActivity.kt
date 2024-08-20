package com.lovelycatv.ai.shadowcat.app.activity.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.activity.main.page.MainHomePage
import com.lovelycatv.ai.shadowcat.app.activity.main.viewmodel.MainActivityViewModel
import com.lovelycatv.ai.shadowcat.app.activity.settings.SettingsActivity
import com.lovelycatv.ai.shadowcat.app.config.ConfigManager
import com.lovelycatv.ai.shadowcat.app.config.connection.ConnectionSettings
import com.lovelycatv.ai.shadowcat.app.database.general.ShadowCatGeneralDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.im.ShadowCatNettyClient
import com.lovelycatv.ai.shadowcat.app.net.retrofit.asyncActions
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import com.lovelycatv.ai.shadowcat.app.shadowcompose.bottomnav.BottomNavItem
import com.lovelycatv.ai.shadowcat.app.shadowcompose.imageview.AsyncImageView
import com.lovelycatv.ai.shadowcat.app.shadowcompose.imageview.FullScreenImageView
import com.lovelycatv.ai.shadowcat.app.ui.theme.MdDeepOrange
import com.lovelycatv.ai.shadowcat.app.ui.theme.MdGreen
import com.lovelycatv.ai.shadowcat.app.ui.theme.ShadowCatTheme
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeLarge
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeNormal
import com.lovelycatv.ai.shadowcat.app.ui.theme.fontSizeSmall
import com.lovelycatv.ai.shadowcat.app.util.android.DialogUtils
import com.lovelycatv.ai.shadowcat.app.util.android.UriUtils
import com.lovelycatv.ai.shadowcat.app.util.android.negative
import com.lovelycatv.ai.shadowcat.app.util.android.positive
import com.lovelycatv.ai.shadowcat.app.util.android.showTips
import com.lovelycatv.ai.shadowcat.app.util.android.showToast
import com.lovelycatv.ai.shadowcat.app.util.common.asRequestBody
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.common.runIfFalse
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel
import com.lovelycatv.ai.shadowcat.app.viewmodel.autoRefreshCurrentUserStatus
import com.lovelycatv.ai.shadowcat.app.viewmodel.eventbus.GlobalEventBus
import com.lovelycatv.ai.shadowcat.app.viewmodel.getCurrentToken
import com.lovelycatv.ai.shadowcat.app.viewmodel.im.InstantMessageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class MainActivity : ComponentActivity() {
    companion object {
        @JvmStatic
        var instance: MainActivity? = null

        @JvmStatic
        var inRealRuntime = false
    }
    private val globalViewModel = GlobalViewModel.instance
    private val imViewModel = InstantMessageViewModel.instance
    private val viewModel: MainActivityViewModel by viewModels()
    private val globalEventBus = GlobalEventBus.instance

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (requestCode == 1) {
            if (uri != null) {
                Log.d("PickMedia", uri.path.toString())

                val targetFilePath = UriUtils.handleUriToPath(instance!!, data)
                if (targetFilePath != null) {
                    val targetFile = File(targetFilePath)
                    val requestFile: RequestBody = targetFile.asRequestBody()
                    val filePart = MultipartBody.Part.createFormData("file", targetFile.name, requestFile)

                    runAsync {
                        val generalDatabase = ShadowCatGeneralDatabase.getInstance(instance!!)
                        val result = getShadowCatServerApi().second.uploadFile(getCurrentToken(), filePart).asyncActions({
                            ""
                        }) {
                            it.data ?: ""
                        }
                        generalDatabase.updateAvatarOfCurrentUser(result, globalViewModel)
                    }
                } else {
                    Log.d("PickMedia", "Could not open file")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        inRealRuntime = true
        enableEdgeToEdge()
        setContent {
            MainActivityComposableView()
        }
    }

    private var tLastPressKeyBackTimeMills = 0L
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewModel.isAvatarInFullScreen.value != false) {
                viewModel.isAvatarInFullScreen.postValue(false)
            } else {
                if (System.currentTimeMillis() - tLastPressKeyBackTimeMills > 1000) {
                    getString(R.string.activity_main_toast_press_again_exit).showToast(this)
                    tLastPressKeyBackTimeMills = System.currentTimeMillis()
                } else {
                    finishAffinity()
                }
            }

            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    @OptIn(ExperimentalPagerApi::class)
    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun MainActivityComposableView(viewModel: MainActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // This function will actually run once when the app is launched
        val imClientInitialized by globalViewModel.imClientInitialized.observeAsState(false)
        LaunchedEffect(imClientInitialized) {
            if (imClientInitialized) {
                ShadowCatNettyClient.instance!!.connected.observeForever { connected ->
                    imViewModel.setImClientConnectionStatus(connected)
                }
            }
        }

        val userMeta by globalViewModel.userMeta.observeAsState(null)
        LaunchedEffect(userMeta) {
            if (userMeta == null || imClientInitialized) {
                return@LaunchedEffect
            }
            with(getShadowCatServerApi()) {
                val settings = this.first.getSettings()

                // Init shadow cat client
                initNettyClient(coroutineScope, settings)
            }
        }

        val bottomNavMenus = listOf(
            BottomNavItem(context.getString(R.string.activity_main_nav_bottom_home), R.drawable.ic_action_email, context.getString(R.string.activity_main_nav_bottom_home))
        )
        var currentSelectedItemIndex by remember { mutableIntStateOf(0) }

        val pagerState = rememberPagerState(initialPage = currentSelectedItemIndex)
        
        ShadowCatTheme {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    MainToolBar()
                },
                bottomBar = {
                    /*NavigationBar {
                        bottomNavMenus.forEachIndexed { index, navItem ->
                            NavigationBarItem(
                                selected = index == currentSelectedItemIndex,
                                onClick = {
                                    // Fix: This method must run on main thread, unless it will produce:
                                    // Exception: Detected multithreaded access to SnapshotStateObserver
                                    runOnMain (coroutineScope) {
                                        currentSelectedItemIndex = index
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                icon = {
                                    Icon(painter = painterResource(id = navItem.iconRes), contentDescription = navItem.name)
                                },
                                label = {
                                    Text(text = navItem.name)
                                }
                            )
                        }
                    }*/
                }
            ) { innerPadding ->
                Column(Modifier.padding(innerPadding)) {
                    SearchBar()
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        count = 1,
                        state = pagerState
                    ) { page ->
                        when(page) {
                            0 -> if (inRealRuntime) {
                                MainHomePage(globalViewModel).apply {
                                    this.setParentActivity(instance!!)
                                }.View()
                            } else {
                                MainHomePage(GlobalViewModel()).View()
                            }
                            // 1 -> MainDynamicPage().View()
                        }
                    }
                }

                val isAvatarInFullScreen by viewModel.isAvatarInFullScreen.observeAsState(false)
                val userMeta by globalViewModel.userMeta.observeAsState()

                if (isAvatarInFullScreen) {
                    FullScreenImageView(
                        imageUrl = userMeta?.getAvatarUrl(
                            ConfigManager.getInstance().connectionConfig!!.getSettings().currentConnectedServer
                        ) ?: "",
                        placeholder = painterResource(id = R.drawable.akarin),
                        onImageClick = {
                            viewModel.isAvatarInFullScreen.postValue(false)
                        }
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Button(onClick = {
                                val gallery = Intent(Intent.ACTION_PICK)
                                gallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/")
                                startActivityForResult(gallery, 1)
                            }) {
                                Text(text = context.getString(R.string.activity_main_avatar_edit))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MainToolBar(viewModel: MainActivityViewModel = viewModel()) {
        val context = LocalContext.current
        val statusBarHeightDp = LocalDensity.current.run {
            WindowInsets.statusBars.getTop(this).toDp()
        }
        val userMeta by globalViewModel.userMeta.observeAsState()

        val imConnected by imViewModel.imClientConnected.observeAsState()
        LaunchedEffect(imConnected) {
            imConnected?.runIfFalse {
                DialogUtils.showTips(context, getString(R.string.activity_main_dialog_status_offline)).positive().show()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp + statusBarHeightDp,
                    bottom = 24.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImageView(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                imageUrl = userMeta?.getAvatarUrl(
                    ConfigManager.getInstance().connectionConfig!!.getSettings().currentConnectedServer
                ) ?: "",
                description = "Avatar",
                placeholder = painterResource(id = R.drawable.akarin),
                onImageClick = {
                    onAvatarClickEvent(context, viewModel)
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp, 0.dp)
            ) {
                Text(
                    modifier = Modifier.clickable {
                        onNicknameClickEvent(context, userMeta!!)
                    },
                    text = userMeta?.nickname ?: "",
                    fontSize = fontSizeLarge(),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = context.getString(
                        if (imConnected!!)
                            R.string.activity_main_status_online
                        else
                            R.string.activity_main_status_offline
                    ),
                    fontSize = fontSizeSmall(),
                    color = if (imConnected!!) MdGreen else MdDeepOrange
                )
            }
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(32.dp),
                color = MaterialTheme.colorScheme.secondary,
                onClick = {
                    with(context) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                }
            ) {
                Image(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.ic_action_settings),
                    contentDescription = "Notification"
                )
            }
        }
    }

    @Composable
    fun SearchBar() {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var searchText by remember { mutableStateOf("") }
            BasicTextField(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .weight(1f),
                value = searchText,
                onValueChange = { searchText = it },
                textStyle = TextStyle(
                    fontSize = fontSizeNormal(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                if (searchText.isEmpty()) {
                    Text(
                        text = context.getString(R.string.activity_main_search_placeholder),
                        fontSize = fontSizeNormal(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                it()
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable {
                        runOnUiThread {
                            DialogUtils.showTips(context, context.getString(R.string.text_coming_soon))
                                .positive().show()
                        }
                    }
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_action_arrow_forward),
                    tint = Color.White,
                    contentDescription = "Search"
                )
            }
        }
    }

    private fun initNettyClient(coroutineScope: CoroutineScope, settings: ConnectionSettings) {
        ShadowCatNettyClient.instance = ShadowCatNettyClient(
            settings,
            3,
            { callback ->
                imViewModel.onReceivedCallbackMessageFromChatServer(callback)
            },
            { sessionId, streamId, data, isNewStream ->
                imViewModel.onReceivedStreamingData(isNewStream, streamId, sessionId, 0, data, false)
            }
        ) { sessionId, streamId, messageId, fullData ->
            imViewModel.onReceivedStreamingData(false, streamId, sessionId, messageId, fullData, true)
        }.also {
            coroutineScope.launch {
                it.connect()
            }
        }

        globalViewModel.imClientInitialized.postValue(true)
    }
    
    private fun onAvatarClickEvent(context: Context, viewModel: MainActivityViewModel) {
        viewModel.isAvatarInFullScreen.postValue(true)
    }

    private fun onNicknameClickEvent(context: Context, userMeta: AccountEntity) {
        DialogUtils.showInput(
            context,
            context.getString(R.string.activity_main_dialog_nickname_edit_title),
            context.getString(R.string.activity_main_dialog_nickname_edit_text),
            userMeta.nickname
        ) { dialog, text ->
            dialog.dismiss()
            runAsync {
                getShadowCatServerApi().second.updateNickname(getCurrentToken(), text).asyncActions({
                    context.getString(R.string.activity_main_dialog_nickname_edit_failed).showTips(context)
                }) {
                    withContext(Dispatchers.Main) {
                        autoRefreshCurrentUserStatus()
                    }
                }
            }
        }.negative().show()
    }
}