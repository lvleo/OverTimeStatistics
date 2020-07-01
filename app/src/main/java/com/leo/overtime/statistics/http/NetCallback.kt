package com.leo.overtime.statistics.http

import android.text.TextUtils
import androidx.lifecycle.LifecycleObserver
import com.leo.overtime.statistics.util.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetCallback<T>(private val listener: (result: T?) -> Unit) : Callback<T>, LifecycleObserver {


    override fun onResponse(call: Call<T>?, response: Response<T>?) {
        if (response!!.isSuccessful) {
            listener.invoke(response.body())
        } else {
            onError(response.errorBody()?.string())
        }
    }

    override fun onFailure(call: Call<T>?, t: Throwable?) {
        t?.printStackTrace()
        onError(t?.message)
    }


    /**
     * 错误处理
     */
    private fun onError(error: String?) {
        if (!TextUtils.isEmpty(error)) {
            if (error!!.contains("timeout", true)) {
                showToast("连接超时")
                return
            }
            if (error.contains("failed to connect to", true)) {
                showToast("连接失败")
                return
            }
            if (error.contains("Unable to resolve host", true)) {
                showToast("设备网络异常，请联系售后")
                return
            }
            showToast(error)
        }
    }

}
