package com.leo.overtime.statistics.bean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class ScheduleBeanJava extends LitePalSupport {

    private long id;
//    @Column(unique = true)
    private long timeDay;
    private long timeMonth;
    private long timeYear;
    private long timeOn;
    private long timeOff;
//    private long timeOvertime;

    //当天有效加班 （0.5小时累计,不满0.5不计入）
    private double hours;
    // 节假日3倍，平时1倍
    private double multiple;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimeDay() {
        return timeDay;
    }

    public void setTimeDay(long timeDay) {
        this.timeDay = timeDay;
    }

    public long getTimeMonth() {
        return timeMonth;
    }

    public void setTimeMonth(long timeMonth) {
        this.timeMonth = timeMonth;
    }

    public long getTimeYear() {
        return timeYear;
    }

    public void setTimeYear(long timeYear) {
        this.timeYear = timeYear;
    }

    public long getTimeOn() {
        return timeOn;
    }

    public void setTimeOn(long timeOn) {
        this.timeOn = timeOn;
    }

    public long getTimeOff() {
        return timeOff;
    }

    public void setTimeOff(long timeOff) {
        this.timeOff = timeOff;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public double getMultiple() {
        return multiple;
    }

    public void setMultiple(double multiple) {
        this.multiple = multiple;
    }
}
