package me.padi.jxh.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri


fun Context.startCalendar() {
    try {
        val calendarIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "content://com.android.calendar/time".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        this.startActivity(calendarIntent)
    } catch (e: Exception) {
        ToastUtil.showToast(this, "$e")
    }
}

// 传入网页URL打开
fun Context.startWebUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    } catch (e: Exception) {
        ToastUtil.showToast(this, "$e")
    }
}

//通过包名启动第三方应用
@SuppressLint("QueryPermissionsNeeded")
fun Context.startLaunchAPK(packageName: String) {
    try {
        val intent = this.packageManager.getLaunchIntentForPackage(packageName)
        this.startActivity(intent)
    } catch (e: Exception) {
        ToastUtil.showToast(this, "$e")
    }
}

//传入应用URL打开
fun Context.startAppUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_DEFAULT, url.toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    } catch (e: Exception) {
        ToastUtil.showToast(this, "$e")
    }
}

// 通过包名和Activity 启动 activity
fun Context.startActivityWithUri(
    packageName: String,
    activityName: String,
    uri: String? = null
) {
    try {
        val intent = Intent().apply {
            setClassName(packageName, activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            uri?.let { data = it.toUri() }
        }
        this.startActivity(intent)
    } catch (e: Exception) {
        ToastUtil.showToast(this, "$e")
    }
}