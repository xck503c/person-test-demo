package com.xck.jdk.file.config;

/**
 * 程序配置
 *
 * @author xuchengkun
 * @date 2021/11/26 09:08
 **/
public interface SystemConfig {

    SystemConfig newConfig(String configPath) throws SystemConfigurationException;
}
