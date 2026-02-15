package me.padi.jxh.core.components


import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.padi.jxh.core.utils.FileUtil.downloadFile
import me.padi.jxh.core.utils.startWebUrl
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog

@Composable
fun DownloadDialog(
    showDialog: MutableState<Boolean>, url: String, fileName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SuperDialog(
        title = "下载附件", summary = "是否下载 $fileName", show = showDialog, onDismissRequest = {
            showDialog.value = false
        }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                text = "浏览器打开",
                onClick = {
                    scope.launch {
                        context.startWebUrl(url)
                    }
                    showDialog.value = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
            Spacer(Modifier.height(10.dp))
            TextButton(
                text = "下载",
                onClick = {
                    scope.launch {
                        downloadFile(
                            context = context,
                            url = url,
                            fileName = fileName,
                            targetDirectory = Environment.DIRECTORY_DOCUMENTS
                        )
                    }
                    showDialog.value = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
            Spacer(Modifier.height(10.dp))
            TextButton(
                text = "取消", onClick = {
                    showDialog.value = false
                }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}