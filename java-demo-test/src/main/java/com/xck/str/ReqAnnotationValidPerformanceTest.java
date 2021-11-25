package com.xck.str;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * 请求参数注解校验性能测试
 *
 * @author xuchengkun
 * @date 2021/11/15 10:46
 **/
public class ReqAnnotationValidPerformanceTest {

    public static void main(String[] args) throws Exception{
        long total = 0L;
        int cycle = 20000;

        for (int i = 0; i < cycle; i++) {
            SendReq sendReq = randomRightReq();
            long start = System.nanoTime();
//            ValidFieldHandler.handle(sendReq); //5642纳秒=0.005642ms
            validBlankAndLen(sendReq); //553纳秒=0.000553ms
            total += (System.nanoTime() - start);
        }

        System.out.println(total/cycle);
    }

    public static SendReq randomRightReq(){
        SendReq rightReq = new SendReq();
        rightReq.param1 = randomString(32, 1);
        rightReq.param2 = randomString(11, 1);
        rightReq.param3 = 1;
        rightReq.param4 = 1;
        rightReq.param5 = randomString(32, 1);
        rightReq.param6 = randomString(21, 1);
        rightReq.param7 = randomString(21, 1);
        rightReq.param8 = randomString(512, 1);
        rightReq.param9 = randomString(32, 1);
        rightReq.param10 = randomString(32, 1);
        rightReq.param11 = (int)(System.currentTimeMillis()/1000);
        return rightReq;
    }

    /**
     * 随机生成字符串
     * @param len 生成字符串的长度
     * @param type 默认英文+数字；1-中文+数字
     * @return
     */
    public static String randomString(int len, int type){
        if(1 == type){
            return RandomStringUtils.random(len, 0x4e00, 0x9fa5, true,true);
        }else {
            return RandomStringUtils.random(len, true,true);
        }
    }

    public static void validBlankAndLen(SendReq sendReq){
        if (StringUtils.isBlank(sendReq.param1) || sendReq.param1.length() > 32) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param2) || sendReq.param2.length() > 11) {
            return;
        }
        if (sendReq.param3 == null) {
            return;
        }
        if (sendReq.param4 == null) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param5) || sendReq.param5.length() > 32) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param6) || sendReq.param6.length() > 21) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param7) || sendReq.param7.length() > 21) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param8) || sendReq.param8.length() > 512) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param9) || sendReq.param9.length() > 32) {
            return;
        }
        if (StringUtils.isBlank(sendReq.param10) || sendReq.param10.length() > 32) {
            return;
        }
    }



    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface ValidField {

        /**
         * 如果字段是String类型，则这里表示允许的长度，0表示不校验。
         * 如果>0则表示要校验是否为空
         */
        int maxlen() default 0;

        /**
         * 如果字段是整型类，则这里设置最大值/最小值，0表示不校验
         */
        int maxValue() default 0;
        int minValue() default 0;

        /**
         * 是否允许出现null的情况
         */
        boolean isNull() default false;
    }

    public static class ValidFieldHandler {

        public static void handle(Object o) throws IllegalAccessException,ValidFieldException {
            if (o == null) return;

            Class clzz = o.getClass();
            for (Field field : clzz.getDeclaredFields()) {
                field.setAccessible(true);
                ValidField validField = field.getAnnotation(ValidField.class);
                if (validField == null) continue;
                Object value = field.get(o);
                if (value == null) {
                    if (!validField.isNull()) {
                        throw new ValidFieldException("参数["+field.getName()+"]不可为空!");
                    }
                    continue;
                }
                Class fieldClazz = field.getType();
                if (Integer.class.equals(fieldClazz)) {
                    if (validField.minValue() != 0 && (Integer)value < validField.minValue()) {
                        throw new ValidFieldException("参数["+field.getName()+"]小于最小值"+validField.minValue());
                    }

                    if (validField.maxValue() != 0 && (Integer)value > validField.maxValue()) {
                        throw new ValidFieldException("参数["+field.getName()+"]大于最大值>"+validField.maxValue());
                    }
                }else if (String.class.equals(fieldClazz)) {
                    if (!validField.isNull() && StringUtils.isBlank((String)value)){
                        throw new ValidFieldException("参数["+field.getName()+"]不可为空!");
                    }

                    if (validField.maxlen() != 0 && ((String)value).length() > validField.maxlen()) {
                        throw new ValidFieldException("参数["+field.getName()+"]长度超过"+validField.maxlen()+"位!");
                    }
                }
            }
        }
    }

    public static class SendReq {

        @ValidField(maxlen = 32)
        private String param1;

        @ValidField(maxlen = 11)
        private String param2;

        @ValidField(minValue = 1)
        private Integer param3;

        @ValidField
        private Integer param4;

        @ValidField(maxlen = 32)
        private String param5;

        @ValidField(maxlen = 21)
        private String param6;

        @ValidField(maxlen = 21)
        private String param7;

        @ValidField(maxlen = 512, isNull = true)
        private String param8;

        @ValidField(maxlen = 32)
        private String param9;

        @ValidField(maxlen = 32)
        private String param10;

        @ValidField
        private Integer param11;

        public String getParam1() {
            return param1;
        }

        public void setParam1(String param1) {
            this.param1 = param1;
        }

        public String getParam2() {
            return param2;
        }

        public void setParam2(String param2) {
            this.param2 = param2;
        }

        public Integer getParam3() {
            return param3;
        }

        public void setParam3(Integer param3) {
            this.param3 = param3;
        }

        public Integer getParam4() {
            return param4;
        }

        public void setParam4(Integer param4) {
            this.param4 = param4;
        }

        public String getParam5() {
            return param5;
        }

        public void setParam5(String param5) {
            this.param5 = param5;
        }

        public String getParam6() {
            return param6;
        }

        public void setParam6(String param6) {
            this.param6 = param6;
        }

        public String getParam7() {
            return param7;
        }

        public void setParam7(String param7) {
            this.param7 = param7;
        }

        public String getParam8() {
            return param8;
        }

        public void setParam8(String param8) {
            this.param8 = param8;
        }

        public String getParam9() {
            return param9;
        }

        public void setParam9(String param9) {
            this.param9 = param9;
        }

        public String getParam10() {
            return param10;
        }

        public void setParam10(String param10) {
            this.param10 = param10;
        }

        public Integer getParam11() {
            return param11;
        }

        public void setParam11(Integer param11) {
            this.param11 = param11;
        }
    }

    public static class ValidFieldException extends RuntimeException{

        public ValidFieldException(String s) {
            super(s);
        }
    }
}
