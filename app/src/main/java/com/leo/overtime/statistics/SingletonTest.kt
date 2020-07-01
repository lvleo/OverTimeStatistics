package com.leo.overtime.statistics

import android.util.Log

object SingletonTest {
    private val tags = SingletonTest::class.java.simpleName

    init {
        Log.e(tags, ": ======init=======")
    }

    fun printHi() {
        Log.e(tags, "printHi: ======Hello======")
    }
}