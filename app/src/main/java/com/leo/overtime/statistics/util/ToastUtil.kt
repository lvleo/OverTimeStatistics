package com.leo.overtime.statistics.util

import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import com.leo.overtime.statistics.App.Companion.app


val toast: Toast by lazy { Toast.makeText(app, "", Toast.LENGTH_SHORT) }

fun showToast(msg: String, duration: Int) {
    toast.setText(msg)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.duration = duration
    toast.show()
}

fun showToast(msg: String) {
    showToast(msg, Toast.LENGTH_SHORT)
}

fun showToast(@StringRes msg: Int) {
    showToast(
        app!!.getString(msg),
        Toast.LENGTH_SHORT
    )
}

fun showToast(@StringRes msg: Int, duration: Int) {
    showToast(app!!.getString(msg), duration)
}