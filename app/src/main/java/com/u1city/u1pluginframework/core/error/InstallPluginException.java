package com.u1city.u1pluginframework.core.error;

/**
 * Created by wuzr on 2017/7/17.
 * 安装插件异常
 */

public class InstallPluginException extends Exception{
    public InstallPluginException(String msg){
        super("安装插件异常：" + msg);
    }
}
