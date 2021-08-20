package com.xck.str;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class StringTest {

    public static void main(String[] args) throws Exception{
//        System.out.println("UNDELIV".getBytes("ISO8859_1").length);
//        System.out.println("DELIVRD".getBytes("ISO8859_1").length);

//        TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
//        TimeZone.setDefault(tz);
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");//设置日期格式
////        ParsePosition pos = new ParsePosition(0);
//        Date now =new Date();
//        String tCurrentdate = df.format(now);
//        Date tCurrentdates = df.parse(tCurrentdate);
//        System.out.println(tCurrentdate);
//        System.out.println(tCurrentdates);

//        System.out.println(System.currentTimeMillis());
//        System.out.println( new SimpleDateFormat("yyyyMMddHHmmss").parse("2021060110555").getTime());
//        System.out.println(new SimpleDateFormat("yyyyMMddHHmmss").parse("2021060110555").getTime()
//                - new SimpleDateFormat("yyyyMMddHHmmss").parse("20210601105559").getTime());

        System.out.println("36.110.62.178".matches("36\\.110\\.62\\.178"));
        System.out.println("36.110.62.178".matches("36\\.110\\.168\\.35"));
    }
    public static void a(Integer i){

    }

    public static void HHmmss(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        System.out.println(format.format(new Date()));
    }

    public static void yyyyMMddHHmmss(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(format.format(new Date(System.currentTimeMillis() - 621*24*60*60*1000L)));
    }

    public static void percentStr(){
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        System.out.println(nf.format(0.3455));
    }

    public static void doubleFormatStr(){
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println(df.format(789.46846841));
    }
}
