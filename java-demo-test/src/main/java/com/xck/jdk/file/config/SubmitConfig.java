package com.xck.jdk.file.config;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.NetUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.setting.Setting;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuchengkun
 * @date 2021/11/25 10:55
 **/
public class SubmitConfig implements SystemConfig{

    List<SubmitConfigItem> configItems;

    /**
     * 用于文件和压缩包密码的加解密密码
     */
    public static String password;

    static {
        String host = NetUtil.getLocalhostStr();
        Assert.notBlank(host);
        password = MD5.create().digestHex(host, Charset.forName("UTF-8"));
    }

    @Override
    public SubmitConfig newConfig(String path) throws SystemConfigurationException{
        Setting setting = new Setting(path, Charset.forName("UTF-8"), true);
        String pwdFile = setting.getByGroup("config.pwd.file", "submit");
        if (StringUtils.isBlank(pwdFile)) {
            throw new SystemConfigurationException("config.file.pwd is blank");
        }

        Setting pwdSetting = new Setting(pwdFile, Charset.forName("UTF-8"), true);

        String names = setting.getByGroup("config.submit.name", "submit");
        if (StringUtils.isBlank(names)) {
            throw new SystemConfigurationException("config.submit.name is blank");
        }

        List<SubmitConfigItem> list = new ArrayList<>();
        for (String name : names.split(",")) {
            if (StringUtils.isBlank(name)) continue;
            SubmitConfigItem item = new SubmitConfigItem();
            item.name(name).file(setting)
                    .userIds(setting)
                    .fileFormat(setting)
                    .filePwd(pwdSetting)
                    .objConvert(setting);
            list.add(item);
        }

        setConfigItems(list);
        return this;
    }

    public List<SubmitConfigItem> getConfigItems() {
        return configItems;
    }

    public void setConfigItems(List<SubmitConfigItem> configItems) {
        this.configItems = configItems;
    }

    @Override
    public String toString() {
        return "SubmitConfig{" +
                "configItems=" + configItems +
                '}';
    }
}
