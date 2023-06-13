package com.xck.persistentQueue.v2;

import cn.hutool.log.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    private static Log log;

    public static List<String> getFilePathList(String filePath) {
        List<String> list = new ArrayList<String>();
        File root = new File(filePath);
        if (root.exists()) {
            File[] files = root.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    list.add(f.getPath());
                }
            }
        }
        return list;
    }

    public static void buildPathIfNeeded(File f) {
        if (f != null) {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
        }
    }

    public static <T> void writeObj(String finalFilePath, T obj) {
        File file = new File(finalFilePath);
        buildPathIfNeeded(file);
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e1) {
            }
            log.error("writeJson data=" + obj, e);
        }
    }

    public static <T> T readObj(String filePath) {
        File file = new File(filePath);
        if (file != null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                return  (T) ois.readObject();
            } catch (Exception e) {
                log.error("readJson", e);
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static <T> void writeJson(String tempFilePath, String finalFilePath, List<T> list) {
        File file = new File(tempFilePath);
        buildPathIfNeeded(file);
        ObjectOutputStream oos = null;
        try {
            String json = JSONObject.toJSONString(list);
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(json);
            oos.flush();
            oos.close();

            if (!file.renameTo(new File(finalFilePath))) {
                log.warn("rename " + tempFilePath + " to " + finalFilePath + " fail");
            }
        } catch (Exception e) {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e1) {
            }
            log.error("writeJson data=" + list, e);
        }
    }

    public static <T> List<T> readJson(String filePath, boolean isDel, Class dataType) {
        File file = new File(filePath);
        List<T> list = new ArrayList<>();
        if (file != null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                String s = (String) ois.readObject();
                list = JSON.parseArray(s, dataType);
            } catch (Exception e) {
                log.error("readJson", e);
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (isDel) {
                        file.delete();
                    }
                } catch (Exception e2) {
                }
            }
        }
        return list;
    }
}
