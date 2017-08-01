package com.u1city.u1pluginframework.core;

import android.content.Intent;
import android.os.Parcel;

/**
 * plugin intent
 * Created by wuzr on 2016/12/2.
 */
public class PluginIntent extends Intent {
    public static final String KEY_PLUGIN_ACTION = "key_plugin_action";
    public static final int FLAG_LAUNCH_PLUGIN = 1;
    public static final int FLAG_LAUNCH_ACTUAL = 1<<1;
    //默认为插件apk的文件名
    private String pluginName;
    private String pluginComponentName;
    private int pluginFlag;
    private String pluginAction;

    public PluginIntent(Intent intent){
        super(intent);
    }

    public PluginIntent(){
        super();
    }

    public PluginIntent(Parcel in){
        readFromParcel(in);
    }

    public Class<?> getPluginComponentClazz() {
        return pluginComponentClazz;
    }

    public void setPluginAction(String pluginAction) {
        this.pluginAction = pluginAction;
    }

    public String getPluginAction() {
        return pluginAction;
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
//        out.writeString(pluginAction);
//        out.writeInt(pluginFlag);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
//        pluginName = in.readString();
//        pluginComponentName = in.readString();
//        pluginAction = in.readString();
//        pluginFlag = in.readInt();
    }

//    public static final Creator<PluginIntent> CREATOR
//            = new Creator<PluginIntent>() {
//        public PluginIntent createFromParcel(Parcel source) {
//            return new PluginIntent(source);
//        }
//        public PluginIntent[] newArray(int size) {
//            return new PluginIntent[size];
//        }
//    };
}
