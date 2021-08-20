package com.xck.other.zip;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;

public class DecryptionZip {

    public static void main(String[] args) throws Exception{
//        UnZip unZip = new UnZip();
//        unZip.extract("D:\\BaiduNetdiskDownload\\office2010 64位软件安装包\\office2010 64位软件安装包\\Office2010(64位).zip"
//                , "3j", "D:\\BaiduNetdiskDownload\\office2010 64位软件安装包\\office2010 64位软件安装包\\Office2010(64位)");
        DictionarySeek.generate(50);
    }

    public static class UnZip {
        private final int BUFF_SIZE = 4096;

//        /*
//        获取ZIP文件中的文件名和目录名
//        */
//        public void getEntryNames(String zipFilePath, String password){
//            List<String> entryList = new ArrayList<>();
//            ZipFile zf;
//            try {
//                zf = new ZipFile(zipFilePath);
//                zf.setFileNameCharset("gbk");//默认UTF8，如果压缩包中的文件名是GBK会出现乱码
//                if(zf.isEncrypted()){
//                    zf.setPassword(password);//设置压缩密码
//                }
//                for(Object obj : zf.getFileHeaders()){
//                    FileHeader fileHeader = (FileHeader)obj;
//                    String fileName = fileHeader.getFileName();//文件名会带上层级目录信息
//                    entryList.add(fileName);
//                }
//            } catch (ZipException e) {
//                e.printStackTrace();
//            }
//            return entryList;
//        }

        /*
        将ZIP包中的文件解压到指定目录
        */
        public boolean extract(String zipFilePath, String password, String destDirPath) {
            InputStream is = null;
            OutputStream os = null;
            ZipFile zf;
            File destDir = new File(destDirPath);
            if(!destDir.exists()){
                destDir.mkdirs();
            }
            try {
                zf = new ZipFile(zipFilePath);
                zf.setFileNameCharset("gbk");
                if (zf.isEncrypted()) {
                    zf.setPassword(password);
                }

                for (Object obj : zf.getFileHeaders()) {
                    FileHeader fileHeader = (FileHeader) obj;
                    File destFile = new File(destDirPath + "/" + fileHeader.getFileName());
                    if(destFile.exists()){
                        continue;
                    }
                    if(fileHeader.isDirectory()){
                        destFile.mkdirs();
                        continue;
                    }

                    if(!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();//创建目录
                    }
                    is = zf.getInputStream(fileHeader);
                    os = new FileOutputStream(destFile);
                    int readLen = -1;
                    byte[] buff = new byte[BUFF_SIZE];
                    while ((readLen = is.read(buff)) != -1) {
                        os.write(buff, 0, readLen);
                    }
                }
                return true;
            } catch (Exception e) {
//                e.printStackTrace();
                return false;
            } finally {
                //关闭资源
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                }

                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ioe) {
                }
                if(destDir.exists()){
                    System.out.println(destDir.delete());
                }
            }
        }
    }

    /**
     * 利用Java实现字母(大小写)+数字+字符的穷举，可用于密码爆破等
     * 如果需要其他的字符，直接接到字符数组中即可
     * 如果只需要
     *  1.数字
     *  2.字母
     *  3.字符
     *  4.数字+字母
     *  5.字母+字符
     *  6.数字+字符
     *  拆分fullCharSource数组即可
     * @author 冰河
     *
     */
    public static class DictionarySeek {

        //密码可能会包含的字符集合
        private static char[] fullCharSource = { '1','2','3','4','5','6','7','8','9','0',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',  'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '{', '}', '|', ':', '"', '<', '>', '?', ';', '\'', ',', '.', '/', '-', '=', '`'};
        //将可能的密码集合长度
        private static int fullCharLength = fullCharSource.length;

        /**
         * 穷举打印输出，可以将打印输出的文件形成字典
         * @param maxLength：生成的字符串的最大长度
         */
        public static void generate(int maxLength) throws Exception{
            //计数器，多线程时可以对其加锁，当然得先转换成Integer类型。
            int counter = 0;
            StringBuilder buider = new StringBuilder();
            while (buider.toString().length() <= maxLength) {
                buider = new StringBuilder(maxLength*2);
                int _counter = counter;
                //10进制转换成26进制
                while (_counter >= fullCharLength) {
                    //获得低位
                    buider.insert(0, fullCharSource[_counter % fullCharLength]);
                    _counter = _counter / fullCharLength;
                    //精髓所在，处理进制体系中只有10没有01的问题，在穷举里面是可以存在01的
                    _counter--;
                }
                //最高位
                buider.insert(0,fullCharSource[_counter]);
                counter++;
                boolean result = new UnZip().extract("D:\\BaiduNetdiskDownload\\office2010 64位软件安装包\\office2010 64位软件安装包\\Office2010(64位).zip"
                        , buider.toString(), "D:\\BaiduNetdiskDownload\\office2010 64位软件安装包\\office2010 64位软件安装包\\Office2010(64位)");
                if(result){
                    System.out.println("success " + buider.toString());
                    return;
                }else {
                    System.out.println("失败" + counter + ", pwd: " + buider.toString());
                }
                Thread.sleep(100);
            }
        }
    }
}