package me.padi.jxh.app

import android.app.Application
import com.tencent.mmkv.MMKV
import me.padi.jxh.core.common.d
import me.padi.jxh.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}