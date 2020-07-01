package com.leo.overtime.statistics.http

import com.leo.overtime.statistics.http.RetrofitUtil.Companion.net
import retrofit2.Call
import retrofit2.http.*


val api = net.create(Api::class.java)

interface Api {

    // http://tool.bitefu.net/jiari/?apikey=123456&d=20200501
    @GET("?apikey=123456&")
    fun getDayStatus(@Query("d") dateStr: String): Call<Int>

}