package com.xck.jdk.file.config;

import cn.hutool.core.io.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ZipPwdDecode {

    private static String path = "D:\\BaiduNetdiskDownload\\123456789.zip";
    private static String output = "D:\\BaiduNetdiskDownload";

    static List<String> list = new ArrayList<>();
    private static long trySize = 0;
    private static long time = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {

        for (int i = 1; i <= 6; i++) {
            System.out.println("尝试长度: " + i);
            if (gene("", i)) {
                break;
            }
        }

        if (list.size() > 1000) {
            FileUtil.appendLines(list, output + "\\pwd.txt", Charset.forName("UTF-8"));
            list.clear();
        }
    }

    public static boolean gene(String prefix, int remainLen) {
        if (remainLen == 0) {
            ++trySize;
            if (System.currentTimeMillis() - time > 60000L) {
                System.out.println("trySize=" + trySize);
                time = System.currentTimeMillis();
            }

            list.add(prefix);
            if (list.size() > 1000) {
                FileUtil.appendLines(list, output + "\\pwd.txt", Charset.forName("UTF-8"));
                list.clear();
            }
//            if (decode(prefix)) {
//                System.out.println("密码:" + prefix);
//                System.exit(0);
//                return true;
//            }
                return false;
        } else {
            for (char c = '0'; c <= 'z'; c++) { // 遍历数字和字母
                if (Character.isLetterOrDigit(c)) { // 判断是否是数字或字母
                    if (gene(prefix + c, remainLen - 1)) {
                        return true;
                    }
                }
            }
            String[] carr = new String[] {"@","#","$","!","~","*","%"};
            for (char c = '0'; c <= 'z'; c++) { // 遍历数字和字母
                if (Character.isLetterOrDigit(c)) { // 判断是否是数字或字母
                    if (gene(prefix + c, remainLen - 1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean decode(String password) {
        try {
            ZipFile zipFile = new ZipFile(path);
            zipFile.setFileNameCharset("UTF-8");
            zipFile.setPassword(password);
            zipFile.extractAll(output);
            return true;
        } catch (ZipException e) {
        }
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        return false;
    }
}
