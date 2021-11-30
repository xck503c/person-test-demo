package com.xck.jdk.file.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.Setting;
import com.xck.jdk.file.converter.EncryptUtil;
import com.xck.jdk.file.pipe.Pipe;
import com.xck.jdk.file.converter.ObjectConvertFactory;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xuchengkun
 * @date 2021/11/25 10:55
 **/
public class SubmitConfigItem {

    private String file;
    private String name;
    private Set<String> userIds;

    private String fileFormat;
    private String filePwd;
    private String zipPwd;
    private String pwdIsEncode;
    private Pipe objConverter;

    public SubmitConfigItem name(String _name) {
        this.name = _name;
        return this;
    }

    public SubmitConfigItem file(Setting setting) throws SystemConfigurationException {
        String key = "config.appender.file";
        String file = setting.getByGroup(key, getName());
        if (StringUtils.isBlank(file)) {
            throw new SystemConfigurationException(key + " is blank");
        }

        if (!FileUtil.exist(file)) {
            System.out.println("create dir " + file);
            FileUtil.mkdir(file);
        }

        setFile(file);
        return this;
    }

    public SubmitConfigItem userIds(Setting setting) throws SystemConfigurationException {
        String key = "config.appender.userIds";
        String userIds = setting.getByGroup(key, getName());
        if (StringUtils.isBlank(userIds)) {
            throw new SystemConfigurationException(key + " is blank");
        }

        HashSet<String> userSet = new HashSet<>();
        for (String userId : userIds.split(",")) {
            if (StringUtils.isBlank(userId)) continue;
            userSet.add(userId);
        }

        setUserIds(userSet);
        return this;
    }

    public SubmitConfigItem fileFormat(Setting setting) throws SystemConfigurationException {
        String key = "config.appender.file.format";

        String format = setting.getByGroup(key, getName());
        if (StringUtils.isBlank(format)) {
            throw new SystemConfigurationException(key + " is blank");
        }
        if ("txt".equals(format)) {
            setFileFormat("txt");
            return this;
        }

        if ("csv".equals(format)) {
            setFileFormat("csv");
            return this;
        }

        throw new SystemConfigurationException(key + " is not txt or csv");
    }

    public SubmitConfigItem filePwd(Setting setting) throws SystemConfigurationException{
        String filePwdKey = "config.appender.file.pwd";
        String zipPwdKey = "config.appender.zip.pwd";
        String pwdIsEncodeKey = "config.appender.pwd.isEncode";

        String name = getName();
        String filePwd = setting.getStr(filePwdKey, name, "");
        String zipPwd = setting.getStr(zipPwdKey, name, "");
        int pwdIsEncode = setting.getInt(pwdIsEncodeKey, name, 1);

        //csv默认文件不加密
        if ("csv".equals(getFileFormat())) {
            setFilePwd("");
        } else {
            setFilePwd(filePwd);
        }
        setZipPwd(zipPwd);
        setPwdIsEncode(pwdIsEncode + "");

        try {
            if ("1".equals(getPwdIsEncode())) {
                this.filePwd = EncryptUtil.deCodeAES(filePwd, SubmitConfig.password);
                this.zipPwd = EncryptUtil.deCodeAES(zipPwd, SubmitConfig.password);
            } else {
                setting.set(getName(), filePwdKey, EncryptUtil.encodeAES(filePwd, SubmitConfig.password));
                setting.set(getName(), zipPwdKey, EncryptUtil.encodeAES(zipPwd, SubmitConfig.password));
                setting.set(getName(), pwdIsEncodeKey, "1");
                setting.store(setting.getSettingPath());
            }
        } catch (Exception e) {
            throw new SystemConfigurationException(e);
        }

        return this;
    }

    public SubmitConfigItem objConvert(Setting setting) throws SystemConfigurationException{
        if ("csv".equals(getFileFormat())){
            objConverter = ObjectConvertFactory.objConverter("default", "");
            return this;
        }

        String convertKey = "config.appender.obj.convert";

        String convert = setting.getStr(convertKey, getName(), "default");
        Pipe _objConverter = ObjectConvertFactory.objConverter(convert, getFilePwd());
        if (_objConverter == null) {
            throw new SystemConfigurationException(convertKey + " is error");
        }
        setObjConverter(_objConverter);

        return this;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public boolean isUsersContains(String userId) {
        if (CollectionUtil.isEmpty(userIds)) return false;

        return userIds.contains(userId);
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getFilePwd() {
        return filePwd;
    }

    public void setFilePwd(String filePwd) {
        this.filePwd = filePwd;
    }

    public String getZipPwd() {
        return zipPwd;
    }

    public void setZipPwd(String zipPwd) {
        this.zipPwd = zipPwd;
    }

    public String getPwdIsEncode() {
        return pwdIsEncode;
    }

    public void setPwdIsEncode(String pwdIsEncode) {
        this.pwdIsEncode = pwdIsEncode;
    }

    public Pipe getObjConverter() {
        return objConverter;
    }

    public void setObjConverter(Pipe objConverter) {
        this.objConverter = objConverter;
    }

    @Override
    public String toString() {
        return "SubmitConfigItem{" +
                "file='" + file + '\'' +
                ", name='" + name + '\'' +
                ", userIds=" + userIds +
                ", fileFormat='" + fileFormat + '\'' +
                ", filePwd='" + filePwd + '\'' +
                ", zipPwd='" + zipPwd + '\'' +
                ", pwdIsEncode='" + pwdIsEncode + '\'' +
                ", objConverter=" + objConverter +
                '}';
    }
}
