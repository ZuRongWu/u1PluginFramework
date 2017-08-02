package com.u1city.u1pluginframework.core;

import android.content.res.Resources;

import com.u1city.u1pluginframework.core.pm.PluginApk;

/**
 * Created by wuzr on 2017/7/31.
 * 描述插件特有的上下文环境
 */

public interface PluginContext {
    void setApk(PluginApk apk);

    /**
     * 获取当前插件依赖的插件的resources对象
     * @param plugin 当前插件依赖的插件名称
     * @return plugin描述的插件的resources对象
     */
    Resources getResources(String plugin);

    String getPluginName();
}
