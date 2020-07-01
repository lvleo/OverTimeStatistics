package com.leo.overtime.statistics.http

import com.leo.overtime.statistics.BuildConfig
import okhttp3.Headers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @Author : yongxu.lv
 * @Date : 2019/08/30 14:03
 * @Desc : RetrofitUtil 工具类
 */
class RetrofitUtil {

    val TIMEOUT = 90L //超时时间

    val BASE_URL = "http://tool.bitefu.net/jiari/"

    companion object {
        val net: RetrofitUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitUtil()
        }
    }


    fun <T> create(clazz: Class<T>): T {
        return getRetrofit().create(clazz)
    }

    /**
     * 请求头
     */
    private fun headers(): Headers {
        val builder = Headers.Builder()
        builder.add("Token", "123456")
        return builder.build()
    }


    private fun getRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
//        client.addInterceptor(HeaderInterceptor(headers()))

        // 设置超时
        client.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        client.readTimeout(TIMEOUT, TimeUnit.SECONDS)
        client.writeTimeout(TIMEOUT, TimeUnit.SECONDS)

        val builder = Retrofit.Builder()

        if (BuildConfig.DEBUG) {
            client.addInterceptor(LoggerInterceptor())
        } else {
        }

        builder.baseUrl(BASE_URL)

        builder.addConverterFactory(GsonConverterFactory.create())
        builder.client(client.build())
        return builder.build()
    }


}
