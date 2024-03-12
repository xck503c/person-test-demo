package com.xck.str.pattern;

import java.util.regex.Pattern;

public class RegularExpressionTest {

    public static void main(String[] args) throws Exception{
//        test1();
        test3();

        System.out.println("系列居家护理套组★XXX积分参与挑战赢正装礼品★XX积分参与积分捐赠，帮助兔唇宝宝！速戳vsurl.cn/XNAeFi 回".length());
        System.out.println(("系列居家护理套组★XXX积分参与挑战赢正装礼品★XX积分参与积分捐赠，帮助兔唇宝宝！速戳vsurl.cn/XNAeFi 回").getBytes("UnicodeBigUnmarked").length);
    }

    public static boolean isMatch(String regular, String input) {
        Pattern pattern = Pattern.compile(regular);
        return pattern.matcher(input).find();
    }

    /**
     * 以详询开头的，后面跟非95开头的数字的，认为是命中
     */
    public static void test1() {
        String regular1 = "详询(?!95\\d*)";
        String input1 = "感受到详询0598xsgf";
        String input2 = "感受到详询9598xsgf";
        String input3 = "感受到详询:9598xsgf";
        String input4 = "感受到详询:9598xsgf，感受到详询9598xsgf";
        String input5 = "详询感受到9598x";
        System.out.println(isMatch(regular1, input1));
        System.out.println(isMatch(regular1, input2));
        System.out.println(isMatch(regular1, input3));
        System.out.println(isMatch(regular1, input4));
        System.out.println(isMatch(regular1, input5));
    }

    public static void test2() {
        String regular1 = "(?<![0-9]{1})([0-35-8][0-9]{6})";
        String input1 = "你递四方速递发顺丰";
        String input2 = "范德萨发的96582524很官方很官方";
        String input3 = "967";
        String input4 = "范德萨范德萨发8975154646546的方式发生地方";
        String input5 = "【滴水贷】袁伟贷款持续逾期且拒绝沟通，拟将启动民事诉讼程序。请2小时内还款或回电4008388103申请，已还忽略";
        System.out.println(isMatch(regular1, input1));
        System.out.println(isMatch(regular1, input2));
        System.out.println(isMatch(regular1, input3));
        System.out.println(isMatch(regular1, input4));
        System.out.println(isMatch(regular1, input5));
    }

    public static void test3() {
        String regular1 = "([0-9]{7})";
        String input1 = "1234567第三方";
        String input2 = "a123456b";
        String input3 = "a123456bfdgfdf1234567";
        String input4 = "sfds46546354455424fds";
        System.out.println(isMatch(regular1, input1));
        System.out.println(isMatch(regular1, input2));
        System.out.println(isMatch(regular1, input3));
        System.out.println(isMatch(regular1, input3));
        System.out.println(isMatch(regular1, input4));
    }
}
