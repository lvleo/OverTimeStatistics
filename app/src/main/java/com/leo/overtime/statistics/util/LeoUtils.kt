package com.leo.overtime.statistics.util

import android.content.Context
import android.content.pm.ApplicationInfo



/**
 * 获取手机 “闹铃” 或 “时钟” 应用的包名
 */
fun getClockPackageInfo(context: Context): String {
    //系统安装所有软件
    val allPackageInfo = context.packageManager.getInstalledPackages(0)
    if (allPackageInfo.isEmpty()) {
        return ""
    }
    for (packageInfo in allPackageInfo) {
        if (isSystemApp(packageInfo.applicationInfo) && isClockApp(packageInfo.packageName)) {
            return packageInfo.packageName
        }
    }
    return ""
}

/**
 * 是否是系统应用
 */
 fun isSystemApp(applicationInfo: ApplicationInfo?): Boolean {
    var isSystemApp = false
    if (applicationInfo != null) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            || applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        ) {
            isSystemApp = true
        }
    }
    return isSystemApp
}


/**
 * 是否是闹钟应用
 */
 fun isClockApp(packageName: String?): Boolean {
    var isClockApp = false
    if (packageName != null && packageName.contains("clock") && !packageName.contains("widget")) {
        isClockApp = true
    }
    return isClockApp
}