package com.leo.overtime.statistics

import android.app.Application
import android.util.Log
import org.litepal.LitePal
import kotlin.properties.Delegates

class App : Application() {

    companion object {
        //方法1：
        var app: App by Delegates.notNull()

        //方法2：
        //        var app: App? = null

        //双重校验 单例模式
        //方法3：
//        val app: App by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            App()
//        }

    }

    override fun onCreate() {
        super.onCreate()

        //方法1 和 2：
        app = this
        LitePal.initialize(this)

    }
}