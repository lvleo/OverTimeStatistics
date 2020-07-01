package com.leo.overtime.statistics.http

data class Result<T>(
        val code: Int,
        val msg: String,
        val data: T
)