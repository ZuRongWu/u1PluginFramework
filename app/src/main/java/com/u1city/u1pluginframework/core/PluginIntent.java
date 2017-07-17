package com.u1city.u1pluginframework.core;

import android.content.Intent;
import android.os.Parcel;

/**
 * plugin intent
 * Created by wuzr on 2016/12/2.
 */
public class PluginIntent extends Intent {
    public static final int FLAG_LAUNCH_PLUGIN = 1;
    public static final int FLAG_LAUNCH_ACTUAL_ACTIVITY = 1<<1;
    //默认为插件apk的文件名
    private String pluginName;
    private String pluginComponentName;
    private int pluginFlag;

    public Class<?> getPluginComponentClazz() {
        return pluginComponentClazz;
    }

    public void setPluginComponentClazz(Class<?> pluginComponentClazz) {
        this.pluginComponentClazz = pluginComponentClazz;
    }

    private Class<?> pluginComponentClazz;

    public void addPluginFlag(int flag) {
        pluginFlag |= flag;
    }

    public boolean hasFlag(int flag) {
        return (pluginFlag & flag) != 0;
    }


    public void setPluginComponentName(String pluginComponentName) {
        this.pluginComponentName = pluginComponentName;
    }

    public String getPluginComponentName() {
        return pluginComponentName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
//        out.writeString(pluginName);
//        out.writeString(pluginComponentName);
//        out.writeInt(pluginFlag);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
//        pluginName = in.readString();
//        pluginComponentName = in.readString();
//        pluginFlag = in.readInt();
    }
}
