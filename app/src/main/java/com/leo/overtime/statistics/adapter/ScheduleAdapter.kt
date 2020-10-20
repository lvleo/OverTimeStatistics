package com.leo.overtime.statistics.adapter

import android.graphics.Color
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.leo.overtime.statistics.R
import com.leo.overtime.statistics.bean.ScheduleBean
import kotlinx.android.synthetic.main.item_history_overtime.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author : yongxu.lv
 * @Date : 2019/08/30 14:03
 * @Desc : 历史上下班打卡记录数据适配器
 */
class ScheduleAdapter(data: List<ScheduleBean>) :
    BaseQuickAdapter<ScheduleBean, BaseViewHolder>(R.layout.item_history_overtime, data) {

//    private val tags = DataAdapter::class.java.simpleName

    private val sdfDay = SimpleDateFormat("yyyy-MM-dd E", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private var stampToday = sdfDay.parse(sdfDay.format(Date()))!!.time

    //22:30:00以后打卡下班有10元宵夜补贴
    private val supperTime = sdfTime.parse("22:30:00").time

    override fun convert(helper: BaseViewHolder, item: ScheduleBean) {

//        Log.e(tags, "convert: item.timeOff======${item.timeOff}")
        if (item.timeOff == 0L && item.timeDay == stampToday) {
//            helper.itemView.visibility = View.GONE
//            helper.itemView.linear_item.visibility = View.GONE
        } else {
            helper.itemView.linear_item.visibility = View.VISIBLE
//            helper.itemView.visibility = View.VISIBLE
        }
        if (helper.adapterPosition % 2 == 0) {
            helper.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            helper.itemView.setBackgroundColor(Color.WHITE)
        }

        //节假日加班
        if (item.multiple > 1) {
            helper.itemView.setBackgroundColor(Color.parseColor("#7CB342"))
        }
        //请假、调修
        if (item.hours < 0) {
            helper.itemView.setBackgroundColor(Color.parseColor("#EF9A9A"))
        }
        helper.itemView.txt_item_date.text = sdfDay.format(Date(item.timeDay))
        helper.itemView.txt_item_on.text = if (item.timeOn == 0L) {
            "N/A"
        } else {
            sdfTime.format(Date(item.timeOn))
        }
        helper.itemView.txt_item_off.text = if (item.timeOff == 0L) {
            "N/A"
        } else {
            sdfTime.format(Date(item.timeOff))
        }

        //将当天下班时间格式化成和日期无关的时间，然后获取从00：00：00到当前时间的总毫秒数
        val offTime = sdfTime.parse(sdfTime.format(item.timeOff))
        if (offTime!!.time > supperTime) {
            helper.itemView.txt_item_off.setTextColor(Color.parseColor("#E53935"))
        } else {
            helper.itemView.txt_item_off.setTextColor(Color.parseColor("#5E5E5E"))
        }

//        helper.itemView.txt_item_overtime.text = item.hours.times(item.multiple).toString()
        helper.itemView.txt_item_overtime.text = item.hours.toString()

    }
}
