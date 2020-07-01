package com.leo.overtime.statistics.bean

import org.litepal.crud.LitePalSupport

/**
 * 节假日
 */
class HolidayBean : LitePalSupport() {

    val id: Int = 0

    // 0:工作日 (包含节假日调休,即周六周日上班);
    // 1:休息日 (包含非周末且非节假日当天)
    // 2:法定节假日 (3倍工资,即节假日当天)
    val type: Int = 0
    
    val year: Int = 0
    val date: String = ""
    //节假日名称
    val name: String = ""
    val extra: String = ""

}
