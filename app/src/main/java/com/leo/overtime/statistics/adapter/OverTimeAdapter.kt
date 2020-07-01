package com.leo.overtime.statistics.adapter

import android.graphics.Color
import android.util.Log
import com.leo.overtime.statistics.R
import com.leo.overtime.statistics.bean.ScheduleBean
import kotlinx.android.synthetic.main.item_history_overtime.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

@Deprecated("弃用", ReplaceWith("ScheduleAdapter"))
class OverTimeAdapter(data: List<ScheduleBean>) : BaseAdapter<ScheduleBean>(data) {

    private val tags = OverTimeAdapter::class.java.simpleName

    private val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())


    private var stampToday by Delegates.notNull<Long>()

    override fun getLayout(): Int {
        stampToday = sdfDay.parse(sdfDay.format(Date()))!!.time
        return R.layout.item_history_overtime
    }

    override fun bindData(helper: Helper, position: Int, item: ScheduleBean) {
        Log.e(tags, "bindData: item======${item.timeDay}")

        Log.e(tags, "bindData: helper.adapterPosition======${helper.adapterPosition}")
        Log.e(tags, "bindData: position-===================${position}")

        if (position % 2 == 0) {
            helper.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            helper.itemView.setBackgroundColor(Color.WHITE)
        }

        helper.itemView.txt_item_date.text = sdfDay.format(Date(item.timeDay))
        helper.itemView.txt_item_on.text = sdfTime.format(Date(item.timeOn))
        helper.itemView.txt_item_off.text = if (item.timeOff == 0L) {
            "N/A"
        } else {
            sdfTime.format(Date(item.timeOff))
        }
        helper.itemView.txt_item_overtime.text = item.hours.times(item.multiple).toString()

    }

}