package me.padi.jxh.core.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MapPage(backStack: MutableList<NavKey>) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = "江航地图", navigationIcon = {
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        backStack.removeAt(backStack.lastIndex)
                    }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            })
        },
    ) { paddingValues ->
        GlideZoomAsyncImage(
            model = "https://jxh.karpov.cn/public/map.jpg",
            contentDescription = "江航地图",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}