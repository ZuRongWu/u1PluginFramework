package com.u1city.u1pluginframework.core.error;

/**
 * Created by wuzr on 2017/7/17.
 * 更新插件异常
 */

public class UpdatePluginException extends Exception{
    public UpdatePluginException(String msg){
        super("更新插件异常：" + msg);
    }
}
