package com.u1city.u1pluginframework.core.error;

import com.u1city.u1pluginframework.core.PluginIntent;

/**
 * Created by wuzr on 2017/7/21.
 * 调用{@link com.u1city.u1pluginframework.core.pm.PackageManager#findPluginService(PluginIntent)}
 * 没有找到匹配的serviceInfo时抛出此异常
 */

public class PluginServiceNotFindException extends Exception{
    public PluginServiceNotFindException(String msg){
        super(msg);
    }
}
