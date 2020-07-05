package com.leo.overtime.statistics.util

import android.content.Context
import android.content.SharedPreferences
import com.leo.overtime.statistics.App.Companion.app

val prefs: SharedPreferences by lazy {
    app.getSharedPreferences(
        "PREFS_OVERTIME",
        Context.MODE_PRIVATE
    )
}

/**
 * true: 完成；false: 未完成
 */
fun isFinish(): Boolean {
    return getBoolean("Finish", false)
}

/**
 * ClockPackageName
 * eg: com.android.alarmclock/com.meizu.flyme.alarmclock.DeskClock
 */
fun saveClockPackageName(name: String) {
    saveString("ClockPackageName", name)
}

fun getClockPackageName(): String {
    return getString("ClockPackageName")
}

fun saveString(key: String, value: String) {
    prefs.edit().putString(key, value).apply()
}

fun getString(key: String): String {
    return prefs.getString(key, "")!!
}

fun saveBoolean(key: String, value: Boolean) {
    prefs.edit().putBoolean(key, value).apply()
}

fun getBoolean(key: String, defValue: Boolean): Boolean {
    return prefs.getBoolean(key, defValue)
}