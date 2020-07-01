package com.leo.overtime.statistics

import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cn.qqtheme.framework.picker.NumberPicker
import com.google.gson.Gson
import com.leo.overtime.statistics.adapter.ScheduleAdapter
import com.leo.overtime.statistics.bean.ScheduleBean
import com.leo.overtime.statistics.http.NetCallback
import com.leo.overtime.statistics.http.api
import com.leo.overtime.statistics.util.showToast
import kotlinx.android.synthetic.main.activity_main.btn_commute_operation
import kotlinx.android.synthetic.main.activity_main.recycler_history
import kotlinx.android.synthetic.main.activity_main.txt_on_off_time
import kotlinx.android.synthetic.main.activity_main.txt_over_time_effective
import kotlinx.android.synthetic.main.activity_main.txt_over_time_month
import kotlinx.android.synthetic.main.activity_main.txt_over_time_total
import kotlinx.android.synthetic.main.activity_main_scrollview.*
import org.litepal.LitePal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val sdfAll = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val sdfHMS = SimpleDateFormat("HH小时mm分", Locale.getDefault())

    //工作时间 8 小时，中午1小时休息，晚上0.5小时晚饭，共计需要上满 9.5小时，之后才开始算加班
    private var leastMilliseconds = (9.5 * 60 * 60 * 1000).toLong()

    //当天6点之前都算上一天，即当天最早是6点00分00秒之后才能打当天的上班卡
    private var todayStartMilliseconds = (6 * 60 * 60 * 1000).toLong()

    //当天日期数据，包含周几
    private lateinit var todayInfo: String

//    //如果是周末, 则所有时间都是加班
//    private var isWeekend = false

    //是否是休息日（包含周末和 非节假日当天其他休息日），如果是则所有时间都是加班
    private var isOffDay = false

    //是否是国家节假日
    private var isHoliday = false

    //当天日期时间戳（唯一性），当天0点0分0秒的总毫秒数
    private var stampToday by Delegates.notNull<Long>()
    private var stampYesterday by Delegates.notNull<Long>()
    private var stampMonth by Delegates.notNull<Long>()
    private var stampLastMonth by Delegates.notNull<Long>()
    private var stampYear by Delegates.notNull<Long>()

    //数据库存储的当天打卡数据实体
    private var scheduleToday: ScheduleBean? = null

    //是否已经打了上班卡
    private var hasOn = true


    private var data = mutableListOf<ScheduleBean>()
    private val adapter =
        ScheduleAdapter(data)

    private var scheduleMonths = mutableListOf<ScheduleBean>()
    private var scheduleLastMonths = mutableListOf<ScheduleBean>()

    /**
     * 时间变化广播
     */
    private val receiverTime = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val time = sdfAll.format(Date())
            Log.i(TAG, "onReceive     : time==============$time")
//            val hour = Calendar.getInstance().get(Calendar.HOUR)
//            val minute = Calendar.getInstance().get(Calendar.MINUTE)
//            Log.e(tags, "onReceive: minute====$minute")
//            if (hour != 0 && minute % 20 == 0) {
//                Log.d(tags, "-------------每隔20分钟关闭-------------")
//            }

            if (hasOn) {
                btn_commute_operation.text = "打下班卡"
                updateSchedule(false)
            } else {
                btn_commute_operation.text = "打上班卡"
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_scrollview)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar!!.setDisplayShowTitleEnabled(false)
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        sdfHMS.timeZone = TimeZone.getTimeZone("GMT+00:00")

//        initParams()

        recycler_history.adapter = adapter


        txt_toolbar_end.setOnClickListener {

            showLeaveDateSelector()

//            var mCalendar = Calendar.getInstance()
//            val dialog = TimePickerDialog(
//                this,
//                OnTimeSetListener { timePicker, i, i1 ->
//                    mCalendar.set(Calendar.HOUR, i)
//                    mCalendar.set(Calendar.MINUTE, i1)
//                    val format = SimpleDateFormat("yyyy年MM月dd日HH:mm")
//                    showToast("" + format.format(mCalendar.getTime()))
//                }, mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE), true
//            )
//            dialog.show()


        }

        btn_commute_operation.setOnClickListener {
            Log.i(TAG, "OnClick, hasOn~~~~~~$hasOn")
            commuteOperation()
        }

        txt_over_time_month.setOnClickListener {

            data.clear()
            //去除当天还未打下班卡的数据
            scheduleMonths.filter { !(it.timeOff == 0L && it.timeDay == stampToday) }.forEach {
                data.add(it)
            }
            adapter.notifyDataSetChanged()

        }

        txt_over_time_last_month.setOnClickListener {

            data.clear()
//            //去除当天还未打下班卡的数据
//            scheduleLastMonths.filter { !(it.timeOff == 0L && it.timeDay == stampToday) }.forEach {
//                data.add(it)
//            }
            data.addAll(scheduleLastMonths)
            adapter.notifyDataSetChanged()

        }

        // 绑定 时间变化广播器
        registerReceiver(receiverTime, IntentFilter(Intent.ACTION_TIME_TICK))

    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume    : ======onResume======")

        initParams()

        Log.d(TAG, "onResume    : stampToday==========$stampToday")
//        var testTime = SimpleDateFormat(
//            "yyyy-MM-dd HH:mm:ss",
//            Locale.getDefault()
//        ).parse("2020-04-18 13:36:00")
//
//        Log.e(tags, "onResume: testTime======${testTime}")
//        Log.e(tags, "onResume: testTime======${testTime.time}")

        getTodayData()

        getHistoryData()

//        SingletonTest.printHi()

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ------onPause------")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ~~~~~~onStop~~~~~~")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiverTime)
    }

    /**
     * 初始化参数
     */
    private fun initParams() {
//        val calendar = Calendar.getInstance()
//        calendar.time = Date()
//        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
//        Log.d(tags, "initParams: dayOfWeek======${dayOfWeek}")
////        isWeekend = dayOfWeek == 1 || dayOfWeek == 7
//        isOffDay = dayOfWeek == 1 || dayOfWeek == 7

        val todayInfo = sdfDay.format(Date())
        stampToday = sdfDay.parse(todayInfo)!!.time
        Log.d(TAG, "initParams: stampToday======$stampToday")

//        Log.d(tags, "initParams: todayInfo=======$todayInfo")
//        todayInfo = "20200425"

//        getDayStatus(todayInfo.replace("-", ""))

        stampYesterday = stampToday - 24 * 60 * 60 * 1000
        Log.d(TAG, "initParams: stampYesterday==$stampYesterday")

        val stampMonthStr = sdfMonth.format(Date())
        stampMonth = sdfMonth.parse(stampMonthStr)!!.time
//        Log.e(TAG, "initParams: stampMonthStr======${stampMonthStr}")
        Log.d(TAG, "initParams: stampMonth======$stampMonth")


        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.MONTH, month - 1)

        val stampLastMonthStr = sdfMonth.format(calendar.time)
        stampLastMonth = sdfMonth.parse(stampLastMonthStr)!!.time
//        Log.e(TAG, "initParams: stampLastMonthStr==${stampLastMonthStr}")
        Log.d(TAG, "initParams: stampLastMonth==$stampLastMonth")

    }

    /**
     * 获取当天是否是休息日、节假日
     */
    private fun getDayStatus(dateStr: String) {
        api.getDayStatus(dateStr).enqueue(NetCallback {
            Log.e(TAG, "getDayStatus  : ${dateStr}，it======${it}")
            // it为0:工作日 (包含节假日调休,即周六周日上班);
            // it为1:休息日 (包含非周末且非节假日当天)
            // it为2:法定节假日 (3倍工资,即节假日当天)
            when (it) {
                0 -> {
                    isOffDay = false
                    isHoliday = false
                }
                1 -> {
                    isOffDay = true
                    isHoliday = false
                }
                2 -> {
                    isOffDay = true
                    isHoliday = true
                }
            }

//            if (hasOn) {
//                btn_commute_operation.text = "打下班卡"
//                updateSchedule(false)
//            } else {
//                btn_commute_operation.text = "打上班卡"
//            }

        })
    }

    /**
     * 获取当日加班数据
     */
    private fun getTodayData() {
        //        LitePal.where("date = ?", todayStamp.toString()).findAsync(ScheduleBeanJava::class.java)
        //            .listen { list ->
        //                Log.e("Leo", "findAsync, size = " + list.size)
        //                hasOn = list.size != 0
        //                Log.e("Leo", "findAsync, hasOn ~~~ $hasOn")
        //            }

        val currentTime = Date().time
        Log.d(TAG, "getTodayData: currentTime=========${currentTime}")
        Log.d(TAG, "getTodayData: stampToday==========${stampToday}")

        var todayInfo = sdfDay.format(Date())

        if (currentTime > stampToday + todayStartMilliseconds) {
            Log.i(TAG, "getTodayData: ======可以打今天的上班卡======")
            scheduleToday = LitePal.where("timeDay = ?", stampToday.toString())
                .findFirst(ScheduleBean::class.java)
        } else {
            Log.e(TAG, "getTodayData: -----不可以打今天的上班卡------")
            scheduleToday = LitePal.where("timeDay = ?", stampYesterday.toString())
                .findFirst(ScheduleBean::class.java)

            todayInfo = sdfDay.format(stampYesterday)
        }

        Log.d(TAG, "getTodayData: todayInfo=======----$todayInfo")
        getDayStatus(todayInfo.replace("-", ""))

//        scheduleToday = LitePal.where("timeDay = ?", stampToday.toString())
//            .findFirst(ScheduleBean::class.java)

        hasOn = scheduleToday != null
        Log.d(TAG, "getTodayData: hasOn===========----$hasOn")
        if (hasOn) {
            btn_commute_operation.text = "打下班卡"
            updateSchedule(false)
        } else {
            btn_commute_operation.text = "打上班卡"

            txt_on_off_time.text = ""
            txt_over_time_total.text = ""
            txt_over_time_effective.text = ""

        }
    }

    /**
     * 获取昨日和当月加班数据
     */
    private fun getHistoryData() {
//        //显示昨天有效加班时长
//        val scheduleYesterday = LitePal.where("timeDay = ?", stampYesterday.toString())
//            .findFirst(ScheduleBean::class.java)
//        Log.d(TAG, "getHistoryData: yesterdayBean======${scheduleYesterday}")
//        if (scheduleYesterday != null) {
//            txt_over_time_yesterday.text = "昨天加班：" + "${scheduleYesterday.hours} 小时"
//        } else {
//            txt_over_time_yesterday.text = "昨天加班：0.0 小时"
//        }

        //显示当月有效加班时长
        scheduleMonths = LitePal
//            .order("id desc")
            .order("timeDay desc")
            .where("timeMonth = ?", stampMonth.toString()).find(ScheduleBean::class.java)
//        val scheduleMonths = LitePal.where("timeMonth = ? and timeDay < ?", stampMonth.toString(), stampToday.toString())
//                .find(ScheduleBean::class.java)
        Log.d(TAG, "getHistoryData: Months.size======${scheduleMonths.size}")

        var totalOfMonth = 0.0
        scheduleMonths.forEach {
            val totalDay = it.hours
//            * it.multiple //节假日工资和加班时长另算

//            val totalDay = it.hours.times(it.multiple)
            totalOfMonth += totalDay
        }
        Log.d(TAG, "getHistoryData: totalOfMonth=====${totalOfMonth}")
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


        //显示上月有效加班时长
        scheduleLastMonths =
            LitePal.order("id desc").where("timeMonth = ?", stampLastMonth.toString())
                .find(ScheduleBean::class.java)

        var totalOfLastMonth = 0.0
        scheduleLastMonths.forEach {
            val totalDay = it.hours
//            * it.multiple //节假日工资和加班时长另算
            totalOfLastMonth += totalDay
        }
        Log.i(TAG, "getHistoryData: totalOfLastMonth=${totalOfLastMonth}")
        txt_over_time_last_month.text = "上月加班：$totalOfLastMonth 小时"

    }

    /**
     * @param needSave 是否需要保存更新当前实体数据
     */
    private fun updateSchedule(needSave: Boolean) {
//        Log.d(TAG, "updateSchedule: today=============${Date(scheduleToday!!.timeDay)}")

        val timeOn = scheduleToday!!.timeOn
        var timeOff = scheduleToday!!.timeOff

        val offStr: String
        //代表已经打了下班卡
        if (timeOff > 0) {
            btn_commute_operation.text = "更新下班卡"
            offStr = sdfTime.format(Date(timeOff))
        } else {
            offStr = "N/A"
            timeOff = Date().time
        }

        txt_on_off_time.text = "考勤时间：${sdfTime.format(Date(timeOn))} ~ $offStr"

        //            val dateFormat = SimpleDateFormat("H小时mm分ss秒", Locale.getDefault())
        //            dateFormat.timeZone = TimeZone.getTimeZone("GMT+00:00")

//        Log.d(TAG, "updateSchedule: timeOff===========${timeOff}")
//        Log.d(TAG, "updateSchedule: timeOn============${timeOn}")

        val offToOnTime = timeOff - timeOn

        Log.d(TAG, "updateSchedule: offToOnTime=======${offToOnTime}")

//        val totalMilliseconds = if (isWeekend || isHoliday) {
        val totalMilliseconds = if (isOffDay || isHoliday) {
            offToOnTime
        } else {
            offToOnTime - leastMilliseconds
        }

        Log.d(TAG, "updateSchedule: totalMilliseconds=${totalMilliseconds}")

        //已加班多少个 0。5小时 半小时
        var halfHourCount = 0L
        if (totalMilliseconds > 0) {
            txt_over_time_total.text = "今日已加班：" + sdfHMS.format(Date(totalMilliseconds))
            halfHourCount = (totalMilliseconds / 1000 / 60) / 30
//            var actualTime1 = (29999 / 1000) / 30
//            Log.d(tags, "onCreate: actualTime1======${actualTime1}")
        } else {
            txt_over_time_total.text = "今日已加班：" + sdfHMS.format(Date(0))
        }

        Log.i(TAG, "updateSchedule: halfHourCount=====${halfHourCount}")
        txt_over_time_effective.text = "有效加班：" + "${halfHourCount * 0.5} 小时"
        //保存今天有效加班时长
        scheduleToday!!.hours = halfHourCount * 0.5
        Log.d(TAG, "updateSchedule: hours=============${scheduleToday!!.hours}")

        if (needSave) {
//            scheduleToday!!.save()
            scheduleToday!!.saveAsync().listen { success ->
                Log.i(TAG, "updateSchedule: 是否保存成功========${success}")
                if (success) {
                    getHistoryData()
                }
            }
        }

    }

    /**
     * 上下班打卡操作
     */
    private fun commuteOperation() {
        if (hasOn) {

            if (isHoliday) {
                scheduleToday!!.multiple = 3.0
            } else {
                scheduleToday!!.multiple = 1.0
            }

            val hasOff = scheduleToday!!.timeOff != 0L
            Log.e(TAG, "commuteOperation: hasOff======${hasOff}")

            if (hasOff) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("温馨提醒")
                builder.setMessage("已经打过下班卡，确定要更新下班时间吗？")
                builder.setNegativeButton("取消", null)

                //                    builder.setNegativeButton("取消") { dialog, which -> }

                builder.setPositiveButton("确定") { _, _ ->
                    scheduleToday!!.timeOff = Date().time
                    scheduleToday!!.saveAsync().listen { success ->
                        if (success) {
                            showToast("更新成功")
                            updateSchedule(true)
                        } else {
                            showToast("更新失败")
                        }
                    }
                }

                builder.show()

            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("温馨提醒")
                builder.setMessage("确定打下班卡吗？")
                builder.setNegativeButton("取消", null)
                builder.setPositiveButton("确定") { _, _ ->
                    scheduleToday!!.timeOff = Date().time
                    scheduleToday!!.saveAsync().listen { success ->
                        if (success) {
                            showToast("下班打卡成功")
                            updateSchedule(true)
                        } else {
                            showToast("下班打卡失败")
                        }
                    }
                }
                builder.show()
            }

        } else {

            scheduleToday = ScheduleBean()
            scheduleToday!!.timeOn = Date().time
            scheduleToday!!.timeDay = stampToday

            if (isHoliday) {
                scheduleToday!!.multiple = 3.0
            } else {
                scheduleToday!!.multiple = 1.0
            }

            val stampMonthStr = sdfMonth.format(Date())
            scheduleToday!!.timeMonth = sdfMonth.parse(stampMonthStr)!!.time

            val stampYearStr = sdfYear.format(Date())
            scheduleToday!!.timeYear = sdfYear.parse(stampYearStr)!!.time

            scheduleToday!!.saveAsync().listen { success ->
                if (success) {
                    hasOn = true
                    showToast("上班打卡成功")
                    btn_commute_operation.text = "打下班卡"
                    updateSchedule(false)
                } else {
                    showToast("上班打卡失败")
                }
            }

        }
    }


    /**
     * 显示请假时间选择器
     */
    private fun showLeaveDateSelector() {
        val mCalendar = Calendar.getInstance()
        val mYear = mCalendar[Calendar.YEAR]
        val mMonth = mCalendar[Calendar.MONTH]
        val mDay = mCalendar[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(
            this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                val data = (month + 1).toString() + "月-" + dayOfMonth + "日"
                Log.e(TAG, "onCreate: data======${data}")

                val newCal = Calendar.getInstance();
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, mMonth)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)
                Log.e(TAG, "onCreate: newCal.date======${newCal.time}")
                Log.e(TAG, "onCreate: newCal.time======${newCal.time.time}")

                val picker = NumberPicker(this)
                //                    picker.setWidth(picker.getScreenWidthPixels() / 2)
                picker.setCycleDisable(false)
                picker.setDividerVisible(false)
                picker.setOffset(3) //偏移量

                picker.setRange(0.5, 8.0, 0.5) //数字范围

                picker.setTextSize(20)

                picker.setLabel("小时")
                picker.setOnNumberPickListener(object : NumberPicker.OnNumberPickListener() {
                    override fun onNumberPicked(index: Int, item: Number?) {
                        Log.e(TAG, "onNumberPicked: index======${index}")
                        Log.e(TAG, "onNumberPicked: item=======${item}")
                        showToast("index=" + index + ", item=" + item.toString())

                        val dateOfTime = newCal.time.time

                        val scheduleLeave = ScheduleBean()
                        scheduleLeave.timeOn = 0
                        scheduleLeave.timeOff = 0
                        scheduleLeave.timeDay = dateOfTime

                        scheduleLeave.multiple = 1.0
                        scheduleLeave.hours = -item!!.toDouble()

                        val stampMonthStr = sdfMonth.format(newCal.time)
                        scheduleLeave.timeMonth = sdfMonth.parse(stampMonthStr)!!.time

                        val stampYearStr = sdfYear.format(newCal.time)
                        scheduleLeave.timeYear = sdfYear.parse(stampYearStr)!!.time

                        Log.e(
                            TAG,
                            "onNumberPicked: scheduleLeave======${Gson().toJson(scheduleLeave)}"
                        )

                        scheduleLeave.saveAsync().listen { success ->
                            if (success) {
                                showToast("调休成功")
                                getHistoryData()

                            } else {
                                showToast("调休失败，请重试")
                            }
                        }

                    }
                })

                picker.show()

            },
            mYear, mMonth, mDay
        )

        datePickerDialog.show()
    }


}
