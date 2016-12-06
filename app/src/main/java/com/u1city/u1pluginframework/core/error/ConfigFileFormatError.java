package com.u1city.u1pluginframework.core.error;

import com.u1city.u1pluginframework.core.pk.PluginApk;

/**
 * 调用{@link com.u1city.u1pluginframework.core.pk.ConfigParser#parse(PluginApk)}解析
 * plugin.xml配置文件时发现配置格式错误的时抛出
 * Created by wuzr on 2016/12/6.
 */
public class ConfigFileFormatError extends Error{
    public ConfigFileFormatError(){
        super("配置文件格式错误");
    }
}
