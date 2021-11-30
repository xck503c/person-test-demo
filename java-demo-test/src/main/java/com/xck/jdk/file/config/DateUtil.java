package com.xck.jdk.file.config;

import cn.hutool.core.util.StrUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间类工具
 *
 * @author xuchengkun
 * @date 2021/11/10 13:18
 **/
public class DateUtil {

    private static Map<String, ThreadLocal<DateFormat>> dateFormatMap = new HashMap<>();

    public static String yyyyMMdd = "yyyyMMdd";
    public static String yyyyMM = "yyyyMM";
    public static String yyyyMMddHHmmss1 = "yyyyMMddHHmmss";
    public static String yyyyMMddHHmmss2 = "yyyy-MM-dd HH:mm:ss";
    public static String HHmm1 = "HH:mm";

    static {
        List<String> list = new ArrayList<>();
        list.add(yyyyMMdd);
        list.add(yyyyMM);
        list.add(yyyyMMddHHmmss1);
        list.add(yyyyMMddHHmmss2);
        list.add(HHmm1);
        for (final String date : list) {
            dateFormatMap.put(date, new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    return new SimpleDateFormat(date);
                }
            });
        }
    }

    public static DateFormat getDateFormat(String format) {
        if (StrUtil.isBlank(format) || !dateFormatMap.containsKey(format)) {
            return null;
        }
        return dateFormatMap.get(format).get();
    }

    public static String curTime(String format) {
        DateFormat dateFormat = getDateFormat(format);
        if (dateFormat == null) {
            return "";
        }

        return dateFormat.format(new Date());
    }

    /**
     * 校验日期格式
     *
     * @param timeStamp
     * @param format
     * @return
     */
    public static boolean validTimeStamp(String timeStamp, String format) {
        if (StrUtil.isBlank(timeStamp)) {
            return false;
        }

        DateFormat dateFormat = getDateFormat(format);
        if (dateFormat == null) {
            return false;
        }

        if (timeStamp.length() != format.length()) {
            return false;
        }

        try {
            dateFormat.parse(timeStamp);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取指定偏移格式的时间
     *
     * @param offset
     * @return
     */
    public static String getDayTimeWithOffset(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -offset); //得到前n天
        Date date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        return df.format(date);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        return cn.hutool.core.date.DateUtil.isSameDay(date1, date2);
    }
}
