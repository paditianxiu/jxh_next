package me.padi.jxh.core.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.kevinnzou.web.rememberWebViewStateWithHTMLData
import me.padi.jxh.R
import me.padi.jxh.Screen
import me.padi.jxh.core.common.NewsStyle
import me.padi.jxh.core.common.NewsStyle.HORIZONTAL_MARGIN
import me.padi.jxh.core.common.WebViewScript
import me.padi.jxh.core.components.DownloadDialog
import me.padi.jxh.core.components.WebView
import me.padi.jxh.core.model.NewsViewModel
import me.padi.jxh.data.repository.AttachmentEntity
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.LocalWindowBottomSheetState
import top.yukonga.miuix.kmp.extra.WindowBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NewsDetailPage(url: String, backStack: MutableList<NavKey>) {
    val navigator = rememberWebViewNavigator()
    val snackBarHostState = remember { SnackbarHostState() }
    val viewModel: NewsViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.getNewsDetail(url)
    }

    val uiState by viewModel.uiState.collectAsState()

    val imgUrl = remember { mutableStateOf("") }

    val showImageSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = "新闻", navigationIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 16.dp), onClick = {
                        backStack.removeAt(backStack.lastIndex)
                    }) {
                    Icon(MiuixIcons.Back, contentDescription = "返回")
                }
            })
        }) { paddingValues ->

        WindowBottomSheet(
            show = showImageSheet,
            title = "图片查看器",
            onDismissRequest = { showImageSheet.value = false }) {
            val dismiss = LocalWindowBottomSheetState.current
            Column {
                GlideZoomAsyncImage(
                    model = imgUrl.value,
                    contentDescription = "图片查看器",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "关闭",
                    onClick = { dismiss?.invoke() })
                Spacer(Modifier.height(16.dp))
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                if (url.contains("jhzyedu.cn")) {
                    TittleContent(
                        title = uiState.newsArticle?.title ?: "",
                        publishDate = uiState.newsArticle?.publishDate ?: "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)

                    )
                    WebView(
                        url = url,
                        webViewState = rememberWebViewStateWithHTMLData(
                            data = NewsHTML.HTML.format(
                                NewsStyle.get(
                                    fontSize = 17,
                                    lineHeight = 1.0F,
                                    letterSpacing = 0.5F,
                                    textMargin = HORIZONTAL_MARGIN,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
                                    textBold = false,
                                    textAlign = "start",
                                    boldTextColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                                    subheadBold = false,
                                    subheadUpperCase = false,
                                    imgMargin = HORIZONTAL_MARGIN,
                                    imgBorderRadius = 4,
                                    imgDisplayMode = "block",
                                    linkTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
                                    codeTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
                                    codeBgColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
                                    tableMargin = 0,
                                    selectionTextColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                                    selectionBgColor = MaterialTheme.colorScheme.primaryContainer.toArgb(),
                                    signatureColor = Color.Gray.toArgb()
                                ), url, uiState.newsArticle?.articleContent, WebViewScript.get(true)

                            ), baseUrl = url
                        ),
                        onError = {

                        },
                        onFinished = {

                        },
                        onImageClick = {
                            imgUrl.value = it
                            showImageSheet.value = true
                        },
                        isShowLinearProgressIndicator = true,
                        navigator = navigator,
                        snackBarHostState = snackBarHostState
                    )
                } else {
                    WebView(
                        url = url,
                        webViewState = rememberWebViewState(url),
                        navigator = navigator,
                        snackBarHostState = snackBarHostState
                    )
                }
            }
            item {
                uiState.newsArticle?.attachment.let { attachments ->
                    if (attachments?.isNotEmpty() == true) {
                        AttachmentContent(
                            attachments = attachments,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = { url, title ->
                                backStack.add(Screen.PdfReader(title, url, backStack))
                            })
                    }
                }
            }

        }
    }
}

@Composable
fun TittleContent(
    title: String, publishDate: String, modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = publishDate, style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun AttachmentContent(
    attachments: List<AttachmentEntity>,
    modifier: Modifier,
    onClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        attachments.forEach { attachment ->
            if (attachment.fileName.isNotEmpty() || attachment.url.isNotEmpty()) {
                Card {
                    val showDownloadDialog = remember { mutableStateOf(false) }
                    BasicComponent(startAction = {
                        Image(
                            painter = painterResource(
                                id = when (attachment.fileType) {
                                    "pdf" -> R.drawable.ic_pdf
                                    "doc", "docx" -> R.drawable.ic_doc
                                    "xls", "xlsx" -> R.drawable.ic_xls
                                    "ppt", "pptx" -> R.drawable.ic_ppt
                                    "mp3", "wav" -> R.drawable.ic_music
                                    "mp4", "avi", "mkv" -> R.drawable.ic_video
                                    "zip", "rar", "7z" -> R.drawable.ic_zip
                                    "jpg", "jpeg", "png", "gif" -> R.drawable.ic_img
                                    "csv" -> R.drawable.ic_csv
                                    "psd" -> R.drawable.ic_psd
                                    else -> R.drawable.folder_24px
                                }
                            ),
                            contentDescription = "file",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(36.dp)
                        )
                    }, title = attachment.fileName, onClick = {
                        if (attachment.fileType == "pdf" || attachment.isNeedOnlineView) onClick(
                            Uri.encode(attachment.url), attachment.fileName
                        )
                        else showDownloadDialog.value = true
                    })
                    DownloadDialog(
                        showDialog = showDownloadDialog,
                        fileName = attachment.fileName,
                        url = attachment.url
                    )
                }
            }
        }
    }
}


object NewsHTML {

    const val HTML: String = """
<!DOCTYPE html>
<html dir="auto">
<head>
    <meta name="viewport" content="initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no, width=device-width, viewport-fit=cover" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type"/>
    <style type="text/css">
        %s
    </style>
    <base href="%s" />
</head>
<body>
<main>
    <article>
        %s
    </article>
</main>
<script>
%s
</script>
</body>
</html>
"""

}