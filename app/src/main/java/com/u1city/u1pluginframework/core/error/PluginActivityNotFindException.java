package com.u1city.u1pluginframework.core.error;

import com.u1city.u1pluginframework.core.PluginIntent;

/**
 * 调用{@link com.u1city.u1pluginframework.core.pk.PackageManager#findPluginActivity(PluginIntent)}
 * 没找到对应的PluginActivity时抛出此异常
 * Created by wuzr on 2016/12/2.
 */
public class PluginActivityNotFindException extends Exception{
    public PluginActivityNotFindException(String activityName){
        super("没有找到activity：" + activityName);
    }
}
