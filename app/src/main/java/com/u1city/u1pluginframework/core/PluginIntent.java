package com.u1city.u1pluginframework.core;

import android.content.Intent;
import android.net.Uri;

/**
 * plugin intent
 * Created by wuzr on 2016/12/2.
 */
public class PluginIntent extends Intent{
    public static final int FLAG_LAUNCH_PLUGIN = 1;
    public static final int FLAG_LAUNCH_ACTUAL_ACTIVITY = 2;
    //默认为插件apk的文件名
    private String pluginName;
    private String pluginCompnentName;
    private String pluginAction;
    private String pluginType;
    private Uri pluginData;
    private int pluginFlag;

    public Class<?> getPluginComponentClazz() {
        return pluginComponentClazz;
    }

    public void setPluginComponentClazz(Class<?> pluginComponentClazz) {
        this.pluginComponentClazz = pluginComponentClazz;
    }

    private Class<?> pluginComponentClazz;

    public void addPluginFlag(int flag){
        pluginFlag |= flag;
    }

    public boolean hasFlag(int flag){
        return (pluginFlag&flag) != 0;
    }

    public void setPluginAction(String pluginAction) {
        this.pluginAction = pluginAction;
    }

    public String getPluginAction() {
        return pluginAction;
    }

    public void setPluginCompnentName(String pluginCompnentName) {
        this.pluginCompnentName = pluginCompnentName;
    }

    public String getPluginCompnentName() {
        return pluginCompnentName;
    }

    public void setPluginData(Uri pluginData) {
        this.pluginData = pluginData;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginType() {
        return pluginType;
    }

    public Uri getPluginData() {
        return pluginData;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }
}
