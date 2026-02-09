package me.padi.jxh.core.ui// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import me.padi.jxh.BuildConfig
import me.padi.jxh.BuildConfig.BUILD_TIME
import me.padi.jxh.R
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun AboutPage(
    backStack: MutableList<NavKey>
) {
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "关于",
                scrollBehavior = topAppBarScrollBehavior,
            )
        },
        popupHost = {},
    ) { innerPadding ->
        AboutPage(
            padding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
            ),
            topAppBarScrollBehavior = topAppBarScrollBehavior,
        )
    }
}

@Composable
fun AboutPage(
    padding: PaddingValues,
    topAppBarScrollBehavior: ScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .then(Modifier.scrollEndHaptic())
            .overScrollVertical()
            .then(Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection))
            .fillMaxHeight(),
        contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            start = WindowInsets.displayCutout.asPaddingValues()
                .calculateLeftPadding(LayoutDirection.Ltr),
            end = WindowInsets.displayCutout.asPaddingValues()
                .calculateRightPadding(LayoutDirection.Ltr),
            bottom = padding.calculateBottomPadding() + 12.dp
        ),
        overscrollEffect = null,
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White),
                ) {
                    Image(
                        modifier = Modifier.size(80.dp),
                        painter = painterResource(R.drawable.icon),
                        contentDescription = null,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "江小航",
                    style = MiuixTheme.textStyles.title3.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface,
                )
            }

            val versionName = BuildConfig.VERSION_NAME
            val versionCode = BuildConfig.VERSION_CODE.toString()

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                text = "App Ver: $versionName ($versionCode)\nBuild Time: $BUILD_TIME",
                textAlign = TextAlign.Center,
            )
            Card(
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                SuperArrow(
                    title = "查看源码",
                    endActions = {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = "GitHub",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = { uriHandler.openUri("https://github.com/compose-miuix-ui/miuix") },
                )
                SuperArrow(
                    title = "加入群组",
                    endActions = {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = "QQ群",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = { uriHandler.openUri("https://qm.qq.com/q/ZddcigGeEE") },
                )
            }
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp),
            ) {
                SuperArrow(
                    title = "协议",
                    endActions = {
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = "Apache-2.0",
                            color = colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = {
                        uriHandler.openUri("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    },
                )
                SuperArrow(
                    title = "第三方协议",
                    onClick = {

                    },
                )
            }
        }
    }
}
