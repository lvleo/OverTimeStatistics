package com.leo.overtime.statistics

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cn.qqtheme.framework.picker.DatePicker
import com.leo.overtime.statistics.adapter.ScheduleAdapter
import com.leo.overtime.statistics.bean.ScheduleBean
import kotlinx.android.synthetic.main.activity_history_schedules.*
import org.litepal.LitePal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 * 查看历史考勤数据
 */
class HistorySchedulesActivity : AppCompatActivity() {

    private val TAG = HistorySchedulesActivity::class.java.simpleName

    private val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    //当天日期时间戳（唯一性），当天0点0分0秒的总毫秒数
    private var stampToday by Delegates.notNull<Long>()

    private var stampMonth by Delegates.notNull<Long>()
    private var stampLastMonth by Delegates.notNull<Long>()

    private var data = mutableListOf<ScheduleBean>()
    private val adapter = ScheduleAdapter(data)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_schedules)

        setSupportActionBar(findViewById(R.id.toolbar))

        //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题.
        //否则显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        //给左上角图标的左边加上一个返回的图标,对应ActionBar.DISPLAY_HOME_AS_UP
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = ""


        val todayInfo = sdfDay.format(Date())
        stampToday = sdfDay.parse(todayInfo)!!.time
        Log.d(TAG, "onCreate: stampToday======$stampToday")

//        val stampMonthStr = sdfMonth.format(Date())
//        stampMonth = sdfMonth.parse(stampMonthStr)!!.time
//////        Log.e(TAG, "onCreate: stampMonthStr======${stampMonthStr}")
////        Log.d(TAG, "onCreate: stampMonth======$stampMonth")

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.MONTH, month - 1)

        val stampLastMonthStr = sdfMonth.format(calendar.time)
        Log.e(TAG, "onCreate: stampLastMonthStr==${stampLastMonthStr}")

        txt_toolbar_title.text = stampLastMonthStr

        stampLastMonth = sdfMonth.parse(stampLastMonthStr)!!.time
        Log.d(TAG, "onCreate: stampLastMonth==$stampLastMonth")


        txt_toolbar_select_month.setOnClickListener {
            onYearMonthPicker()
        }

        recycler_history_all.adapter = adapter

//        getAllHistoryData()

        getHistoryMonthData(stampLastMonth.toString())

    }


    /**
     * 获取所有上班数据
     */
    private fun getAllHistoryData() {
        val scheduleMonths = LitePal
//            .order("id desc")
            .order("timeDay desc")
//            .where("timeMonth = ?", stampMonth.toString())
            .find(ScheduleBean::class.java)
//        val scheduleMonths = LitePal.where("timeMonth = ? and timeDay < ?", stampMonth.toString(), stampToday.toString())
//                .find(ScheduleBean::class.java)
        Log.d(TAG, "getAllHistoryData: Months.size======${scheduleMonths.size}")

        var totalOfMonth = 0.0
        scheduleMonths.forEach {
            val totalDay = it.hours
//            * it.multiple //节假日工资和加班时长另算

//            val totalDay = it.hours.times(it.multiple)
            totalOfMonth += totalDay
        }
        Log.d(TAG, "getAllHistoryData: totalOfMonth=====${totalOfMonth}")
        txt_over_time_month.text = "当月加班：$totalOfMonth 小时"

        data.clear()

        //1.加载当月所有数据
//        data.addAll(scheduleMonths)

        //2.去除当天的数据
//        scheduleMonths.filter { it.timeDay != stampToday }.forEach {
//            data.add(it)
//        }

        //3.去除当天还未打下班卡的数据
        scheduleMonths.filter { !(it.timeOff == 0L && it.timeDay == stampToday) }.forEach {
            data.add(it)
        }

        adapter.notifyDataSetChanged()

    }

    /**
     * 获取历史月份上班数据
     */
    private fun getHistoryMonthData(monthMillisecondStr: String) {
        val scheduleMonths = LitePal
//            .order("id desc")
            .order("timeDay desc")
            .where("timeMonth = ?", monthMillisecondStr)
            .find(ScheduleBean::class.java)
//        val scheduleMonths = LitePal.where("timeMonth = ? and timeDay < ?", stampMonth.toString(), stampToday.toString())
//                .find(ScheduleBean::class.java)
        Log.d(TAG, "getHistoryMonthData: Months.size======${scheduleMonths.size}")

        var totalOfMonth = 0.0
        scheduleMonths.forEach {
            val totalDay = it.hours
//            * it.multiple //节假日工资和加班时长另算
//            val totalDay = it.hours.times(it.multiple)
            totalOfMonth += totalDay
        }
        Log.d(TAG, "getHistoryMonthData: totalOfMonth=====${totalOfMonth}")
        txt_over_time_month.text = "当月加班：$totalOfMonth 小时"

        data.clear()

        //1.加载当月所有数据
//        data.addAll(scheduleMonths)

        //2.去除当天的数据
//        scheduleMonths.filter { it.timeDay != stampToday }.forEach {
//            data.add(it)
//        }

        //3.去除当天还未打下班卡的数据
        scheduleMonths.filter { !(it.timeOff == 0L && it.timeDay == stampToday) }.forEach {
            data.add(it)
        }

        adapter.notifyDataSetChanged()

    }

    /**
     * 显示年月选择器
     */
    private fun onYearMonthPicker() {
        val mCalendar = Calendar.getInstance()
        val mYear = mCalendar[Calendar.YEAR]
        val mMonth = mCalendar[Calendar.MONTH]

        Log.e(TAG, "onYearMonthPicker: ======${txt_toolbar_title.text}")
        val lastMonthYear = txt_toolbar_title.text.split("-")
        Log.e(TAG, "onYearMonthPicker: ======${lastMonthYear[0].toInt()}")
        Log.e(TAG, "onYearMonthPicker: ======${lastMonthYear[1].toInt()}")

        val datePicker = DatePicker(this, DatePicker.YEAR_MONTH)
        datePicker.setUseWeight(true)
        datePicker.setTextSize(20)
        datePicker.setRangeStart(mYear - 2, 1)
        datePicker.setRangeEnd(mYear, mMonth + 1)
        datePicker.setSelectedItem(lastMonthYear[0].toInt(), lastMonthYear[1].toInt())

        datePicker.setOnDatePickListener(DatePicker.OnYearMonthPickListener { year, month ->
            Log.e(TAG, "onYearMonthPicker: year======${year} + month =$month")
            val newCal = Calendar.getInstance();
            newCal.set(Calendar.YEAR, year.toInt())
            newCal.set(Calendar.MONTH, month.toInt() - 1)
            val yearMonthStr = sdfMonth.format(newCal.time)
            val yearMonthTime = sdfMonth.parse(yearMonthStr)!!.time
            Log.d(TAG, "onYearMonthPicker: yearMonthTime======$yearMonthTime")

            txt_toolbar_title.text = yearMonthStr

            getHistoryMonthData(yearMonthTime.toString())

        })
        datePicker.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
//                onBackPressed()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
//        NavUtils.navigateUpFromSameTask(this)
        Log.e(TAG, "onBackPressed: ======")
        super.onBackPressed()
    }

}