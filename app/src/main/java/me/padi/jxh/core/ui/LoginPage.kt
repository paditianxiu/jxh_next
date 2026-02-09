package me.padi.jxh.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.delay
import me.padi.jxh.R
import me.padi.jxh.core.model.LoginViewModel
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.WindowDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun LoginPage(onNavHome: () -> Unit) {
    var username by remember { mutableStateOf(MMKV.defaultMMKV().decodeString("username") ?: "") }
    var password by remember { mutableStateOf(MMKV.defaultMMKV().decodeString("password") ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollBehavior = MiuixScrollBehavior()
    val viewModel: LoginViewModel = koinViewModel()
    val loginState by viewModel.loginState.collectAsState()

    val showError = remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState.isSuccess()) {
            MMKV.defaultMMKV().encode(
                "username", username
            )
            MMKV.defaultMMKV().encode(
                "password", password
            )
            delay(300)
            onNavHome()
            viewModel.resetState()
        }

        showError.value = loginState.isError()
    }


    val hasCheckedInitialLogin by viewModel.hasCheckedInitialLogin.collectAsState()

    LaunchedEffect(Unit) {
        if (!hasCheckedInitialLogin) {
            if (viewModel.performInitialLoginCheck {}) {
                viewModel.setLoading()
                delay(300)
                onNavHome()
                viewModel.resetState()
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior, title = "江小航", largeTitle = "登录", actions = {
                    IconButton(onClick = { }) {
                        Icon(MiuixIcons.More, contentDescription = "更多")
                    }
                    Spacer(Modifier.width(4.dp))
                })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(16.dp),
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding())
        ) {
            item {
                WindowDialog(
                    title = "提示",
                    summary = loginState.getErrorOrNull(),
                    show = showError,
                    onDismissRequest = { showError.value = false }) {}


                SmallTitle(
                    text = "江小航", insideMargin = PaddingValues(4.dp, 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "江小航",
                        modifier = Modifier.size(100.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.primaryVariant
                    ), insideMargin = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "温馨提示",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "默认密码为身份证后6位或ZFsoft12!或ZFsoft1.",
                        color = MiuixTheme.colorScheme.onPrimaryVariant,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
                SmallTitle(
                    text = "登录", insideMargin = PaddingValues(4.dp, 16.dp)
                )
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "学号",
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "密码",
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Rename,
                                tint = if (passwordVisible) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    })
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.login(
                            username, password
                        )
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(),
                    enabled = !loginState.isLoading()
                ) {
                    AnimatedVisibility(
                        visible = loginState.isLoading()
                    ) {
                        InfiniteProgressIndicator(
                            color = MiuixTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 8.dp),
                            size = 20.dp,
                        )
                    }
                    Text("登录", color = MiuixTheme.colorScheme.onPrimary)
                }
//                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    modifier = Modifier.fillMaxWidth(),
//                    onClick = {
//                        onNavHome()
//                    },
//                ) {
//                    Text("进入主页")
//                }
            }
        }
    }
}