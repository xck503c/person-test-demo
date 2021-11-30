package com.xck.jdk.file.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;

/**
 * @author xuchengkun
 * @date 2021/10/26 13:25
 **/
public class MyFileUtil {

    public static void mkParentDir(String fileName) {
        File file = FileUtil.file(fileName);
        if (!file.getParentFile().exists()) {
            System.out.println("父目录不存在, 需要创建=" + file.getParentFile().getAbsolutePath());
            FileUtil.mkParentDirs(fileName);
        }
    }

    public static void ifExistDel(String fileName) {
        if (FileUtil.exist(fileName)) {
            FileUtil.del(fileName);
        }
    }

    /**
     * 指定密码并压缩文件
     *
     * @param srcPath
     * @param dstPath
     * @return
     */
    public static boolean zip(String srcPath, String dstPath, String password) {
        try {
            if (!FileUtil.exist(srcPath)) {
                return false;
            }
            ZipParameters parameters = new ZipParameters();
            if (StrUtil.isNotBlank(password)) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                parameters.setPassword(password.toCharArray());
            }
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            File srcFile = new File(srcPath);
            ZipFile destFile = new ZipFile(dstPath);
            if (srcFile.isDirectory()) {
                destFile.addFolder(srcFile, parameters);
            } else {
                destFile.addFile(srcFile, parameters);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
