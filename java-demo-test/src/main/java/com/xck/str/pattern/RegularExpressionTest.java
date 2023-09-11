package com.xck.str.pattern;

import java.util.regex.Pattern;

public class RegularExpressionTest {

    public static void main(String[] args) {
        test1();
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
}
