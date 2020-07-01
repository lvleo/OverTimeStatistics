package com.leo.overtime.statistics.bean

import org.litepal.crud.LitePalSupport

class ScheduleBean : LitePalSupport() {

    var id: Long = 0
    var timeOn: Long = 0
    var timeOff: Long = 0
    var timeDay: Long = 0
    var timeMonth: Long = 0
    var timeYear: Long = 0

    //当天有效加班 （0.5小时累计,不满0.5不计入）
    var hours: Double = 0.0

    // 节假日3倍，平时1倍
    var multiple: Double = 1.0

}