package com.xck.jdk.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarTest {

    private static SimpleDateFormat format
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println(format.format(new Date(getCurrentDate(
                new Date(getCurrentDate(new Date(), 1)), 2))));
    }

    public static boolean getRepeatTime(long frist_time, int filter_cycle) {
        int filter_cycle_day = filter_cycle / 86400;
        if (filter_cycle_day < 1) {
            return betweenHours(frist_time, filter_cycle);
        } else {
            long curren_time = getCurrentDate(new Date(frist_time), filter_cycle_day);
            return System.currentTimeMillis() <= curren_time;
        }
    }

    private static boolean betweenHours(long frist_time, int filter_cycle) {
        long future_time = frist_time + (long)filter_cycle * 1000L;
        long currentDateTime = getCurrentDate(new Date(frist_time), 1);
        return future_time <= currentDateTime;
    }

    private static long getCurrentDate(Date currentDate, int day_num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(11, 23);
        calendar.add(5, day_num - 1);
        calendar.set(12, 59);
        calendar.set(13, 59);
        calendar.set(14, 0);
        return calendar.getTime().getTime();
    }

    public static void get(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate(10, 12,25, 13, 11));
        System.out.println(format.format(calendar.getTime()));
        calendar.set(Calendar.DATE, 5);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        System.out.println(format.format(calendar.getTime()));
    }

    public static Date getDate(int date, int hour, int min, int sec, int milli){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        calendar.set(Calendar.MILLISECOND, milli);
        return calendar.getTime();
    }
}
